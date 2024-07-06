/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.apksig.internal.apk;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class AndroidBinXmlParser {
    public static final int EVENT_START_DOCUMENT = 1;
    public static final int EVENT_END_DOCUMENT = 2;
    public static final int EVENT_START_ELEMENT = 3;
    public static final int EVENT_END_ELEMENT = 4;
    public static final int VALUE_TYPE_UNSUPPORTED = 0;
    public static final int VALUE_TYPE_STRING = 1;
    public static final int VALUE_TYPE_INT = 2;
    public static final int VALUE_TYPE_REFERENCE = 3;
    public static final int VALUE_TYPE_BOOLEAN = 4;
    private static final long NO_NAMESPACE = 0xffffffffL;
    private final ByteBuffer mXml;
    private StringPool mStringPool;
    private ResourceMap mResourceMap;
    private int mDepth;
    private int mCurrentEvent = EVENT_START_DOCUMENT;
    private String mCurrentElementName;
    private String mCurrentElementNamespace;
    private int mCurrentElementAttributeCount;
    private List<Attribute> mCurrentElementAttributes;
    private ByteBuffer mCurrentElementAttributesContents;
    private int mCurrentElementAttrSizeBytes;

    public AndroidBinXmlParser(ByteBuffer xml) throws XmlParserException {
        xml.order(ByteOrder.LITTLE_ENDIAN);
        Chunk resXmlChunk = null;
        while (xml.hasRemaining()) {
            Chunk chunk = Chunk.get(xml);
            if (chunk == null) {
                break;
            }
            if (chunk.getType() == Chunk.TYPE_RES_XML) {
                resXmlChunk = chunk;
                break;
            }
        }
        if (resXmlChunk == null) {
            throw new XmlParserException("No XML chunk in file");
        }
        mXml = resXmlChunk.getContents();
    }

    public int getDepth() {
        return mDepth;
    }

    public int getEventType() {
        return mCurrentEvent;
    }

    public String getName() {
        if ((mCurrentEvent != EVENT_START_ELEMENT) && (mCurrentEvent != EVENT_END_ELEMENT)) {
            return null;
        }
        return mCurrentElementName;
    }

    public String getNamespace() {
        if ((mCurrentEvent != EVENT_START_ELEMENT) && (mCurrentEvent != EVENT_END_ELEMENT)) {
            return null;
        }
        return mCurrentElementNamespace;
    }

    public int getAttributeCount() {
        if (mCurrentEvent != EVENT_START_ELEMENT) {
            return -1;
        }
        return mCurrentElementAttributeCount;
    }

    public int getAttributeNameResourceId(int index) throws XmlParserException {
        return getAttribute(index).getNameResourceId();
    }

    public String getAttributeName(int index) throws XmlParserException {
        return getAttribute(index).getName();
    }

    public String getAttributeNamespace(int index) throws XmlParserException {
        return getAttribute(index).getNamespace();
    }

    public int getAttributeValueType(int index) throws XmlParserException {
        int type = getAttribute(index).getValueType();
        switch (type) {
            case Attribute.TYPE_STRING:
                return VALUE_TYPE_STRING;
            case Attribute.TYPE_INT_DEC:
            case Attribute.TYPE_INT_HEX:
                return VALUE_TYPE_INT;
            case Attribute.TYPE_REFERENCE:
                return VALUE_TYPE_REFERENCE;
            case Attribute.TYPE_INT_BOOLEAN:
                return VALUE_TYPE_BOOLEAN;
            default:
                return VALUE_TYPE_UNSUPPORTED;
        }
    }

    public int getAttributeIntValue(int index) throws XmlParserException {
        return getAttribute(index).getIntValue();
    }

    public boolean getAttributeBooleanValue(int index) throws XmlParserException {
        return getAttribute(index).getBooleanValue();
    }

    public String getAttributeStringValue(int index) throws XmlParserException {
        return getAttribute(index).getStringValue();
    }
    private Attribute getAttribute(int index) {
        if (mCurrentEvent != EVENT_START_ELEMENT) {
            throw new IndexOutOfBoundsException("Current event not a START_ELEMENT");
        }
        if (index < 0) {
            throw new IndexOutOfBoundsException("index must be >= 0");
        }
        if (index >= mCurrentElementAttributeCount) {
            throw new IndexOutOfBoundsException(
                    "index must be <= attr count (" + mCurrentElementAttributeCount + ")");
        }
        parseCurrentElementAttributesIfNotParsed();
        return mCurrentElementAttributes.get(index);
    }

    public int next() throws XmlParserException {
        if (mCurrentEvent == EVENT_END_ELEMENT) {
            mDepth--;
        }
        while (mXml.hasRemaining()) {
            Chunk chunk = Chunk.get(mXml);
            if (chunk == null) {
                break;
            }
            switch (chunk.getType()) {
                case Chunk.TYPE_STRING_POOL:
                    if (mStringPool != null) {
                        throw new XmlParserException("Multiple string pools not supported");
                    }
                    mStringPool = new StringPool(chunk);
                    break;
                case Chunk.RES_XML_TYPE_START_ELEMENT:
                {
                    if (mStringPool == null) {
                        throw new XmlParserException(
                                "Named element encountered before string pool");
                    }
                    ByteBuffer contents = chunk.getContents();
                    if (contents.remaining() < 20) {
                        throw new XmlParserException(
                                "Start element chunk too short. Need at least 20 bytes. Available: "
                                        + contents.remaining() + " bytes");
                    }
                    long nsId = getUnsignedInt32(contents);
                    long nameId = getUnsignedInt32(contents);
                    int attrStartOffset = getUnsignedInt16(contents);
                    int attrSizeBytes = getUnsignedInt16(contents);
                    int attrCount = getUnsignedInt16(contents);
                    long attrEndOffset = attrStartOffset + ((long) attrCount) * attrSizeBytes;
                    contents.position(0);
                    if (attrStartOffset > contents.remaining()) {
                        throw new XmlParserException(
                                "Attributes start offset out of bounds: " + attrStartOffset
                                        + ", max: " + contents.remaining());
                    }
                    if (attrEndOffset > contents.remaining()) {
                        throw new XmlParserException(
                                "Attributes end offset out of bounds: " + attrEndOffset
                                        + ", max: " + contents.remaining());
                    }
                    mCurrentElementName = mStringPool.getString(nameId);
                    mCurrentElementNamespace =
                            (nsId == NO_NAMESPACE) ? "" : mStringPool.getString(nsId);
                    mCurrentElementAttributeCount = attrCount;
                    mCurrentElementAttributes = null;
                    mCurrentElementAttrSizeBytes = attrSizeBytes;
                    mCurrentElementAttributesContents =
                            sliceFromTo(contents, attrStartOffset, attrEndOffset);
                    mDepth++;
                    mCurrentEvent = EVENT_START_ELEMENT;
                    return mCurrentEvent;
                }
                case Chunk.RES_XML_TYPE_END_ELEMENT:
                {
                    if (mStringPool == null) {
                        throw new XmlParserException(
                                "Named element encountered before string pool");
                    }
                    ByteBuffer contents = chunk.getContents();
                    if (contents.remaining() < 8) {
                        throw new XmlParserException(
                                "End element chunk too short. Need at least 8 bytes. Available: "
                                        + contents.remaining() + " bytes");
                    }
                    long nsId = getUnsignedInt32(contents);
                    long nameId = getUnsignedInt32(contents);
                    mCurrentElementName = mStringPool.getString(nameId);
                    mCurrentElementNamespace =
                            (nsId == NO_NAMESPACE) ? "" : mStringPool.getString(nsId);
                    mCurrentEvent = EVENT_END_ELEMENT;
                    mCurrentElementAttributes = null;
                    mCurrentElementAttributesContents = null;
                    return mCurrentEvent;
                }
                case Chunk.RES_XML_TYPE_RESOURCE_MAP:
                    if (mResourceMap != null) {
                        throw new XmlParserException("Multiple resource maps not supported");
                    }
                    mResourceMap = new ResourceMap(chunk);
                    break;
                default:
                    break;
            }
        }
        mCurrentEvent = EVENT_END_DOCUMENT;
        return mCurrentEvent;
    }
    private void parseCurrentElementAttributesIfNotParsed() {
        if (mCurrentElementAttributes != null) {
            return;
        }
        mCurrentElementAttributes = new ArrayList<>(mCurrentElementAttributeCount);
        for (int i = 0; i < mCurrentElementAttributeCount; i++) {
            int startPosition = i * mCurrentElementAttrSizeBytes;
            ByteBuffer attr =
                    sliceFromTo(
                            mCurrentElementAttributesContents,
                            startPosition,
                            startPosition + mCurrentElementAttrSizeBytes);
            long nsId = getUnsignedInt32(attr);
            long nameId = getUnsignedInt32(attr);
            attr.position(attr.position() + 7); // skip ignored fields
            int valueType = getUnsignedInt8(attr);
            long valueData = getUnsignedInt32(attr);
            mCurrentElementAttributes.add(
                    new Attribute(
                            nsId,
                            nameId,
                            valueType,
                            (int) valueData,
                            mStringPool,
                            mResourceMap));
        }
    }
    private static class Attribute {
        private static final int TYPE_REFERENCE = 1;
        private static final int TYPE_STRING = 3;
        private static final int TYPE_INT_DEC = 0x10;
        private static final int TYPE_INT_HEX = 0x11;
        private static final int TYPE_INT_BOOLEAN = 0x12;
        private final long mNsId;
        private final long mNameId;
        private final int mValueType;
        private final int mValueData;
        private final StringPool mStringPool;
        private final ResourceMap mResourceMap;
        private Attribute(
                long nsId,
                long nameId,
                int valueType,
                int valueData,
                StringPool stringPool,
                ResourceMap resourceMap) {
            mNsId = nsId;
            mNameId = nameId;
            mValueType = valueType;
            mValueData = valueData;
            mStringPool = stringPool;
            mResourceMap = resourceMap;
        }
        public int getNameResourceId() {
            return (mResourceMap != null) ? mResourceMap.getResourceId(mNameId) : 0;
        }
        public String getName() throws XmlParserException {
            return mStringPool.getString(mNameId);
        }
        public String getNamespace() throws XmlParserException {
            return (mNsId != NO_NAMESPACE) ? mStringPool.getString(mNsId) : "";
        }
        public int getValueType() {
            return mValueType;
        }
        public int getIntValue() throws XmlParserException {
            switch (mValueType) {
                case TYPE_REFERENCE:
                case TYPE_INT_DEC:
                case TYPE_INT_HEX:
                case TYPE_INT_BOOLEAN:
                    return mValueData;
                default:
                    throw new XmlParserException("Cannot coerce to int: value type " + mValueType);
            }
        }
        public boolean getBooleanValue() throws XmlParserException {
            //noinspection SwitchStatementWithTooFewBranches
            switch (mValueType) {
                case TYPE_INT_BOOLEAN:
                    return mValueData != 0;
                default:
                    throw new XmlParserException(
                            "Cannot coerce to boolean: value type " + mValueType);
            }
        }
        public String getStringValue() throws XmlParserException {
            switch (mValueType) {
                case TYPE_STRING:
                    return mStringPool.getString(mValueData & 0xffffffffL);
                case TYPE_INT_DEC:
                    return Integer.toString(mValueData);
                case TYPE_INT_HEX:
                    return "0x" + Integer.toHexString(mValueData);
                case TYPE_INT_BOOLEAN:
                    return Boolean.toString(mValueData != 0);
                case TYPE_REFERENCE:
                    return "@" + Integer.toHexString(mValueData);
                default:
                    throw new XmlParserException(
                            "Cannot coerce to string: value type " + mValueType);
            }
        }
    }

    private static class Chunk {
        public static final int TYPE_STRING_POOL = 1;
        public static final int TYPE_RES_XML = 3;
        public static final int RES_XML_TYPE_START_ELEMENT = 0x0102;
        public static final int RES_XML_TYPE_END_ELEMENT = 0x0103;
        public static final int RES_XML_TYPE_RESOURCE_MAP = 0x0180;
        static final int HEADER_MIN_SIZE_BYTES = 8;
        private final int mType;
        private final ByteBuffer mHeader;
        private final ByteBuffer mContents;
        public Chunk(int type, ByteBuffer header, ByteBuffer contents) {
            mType = type;
            mHeader = header;
            mContents = contents;
        }
        public ByteBuffer getContents() {
            ByteBuffer result = mContents.slice();
            result.order(mContents.order());
            return result;
        }
        public ByteBuffer getHeader() {
            ByteBuffer result = mHeader.slice();
            result.order(mHeader.order());
            return result;
        }
        public int getType() {
            return mType;
        }

        public static Chunk get(ByteBuffer input) throws XmlParserException {
            if (input.remaining() < HEADER_MIN_SIZE_BYTES) {
                // Android ignores the last chunk if its header is too big to fit into the file
                input.position(input.limit());
                return null;
            }
            int originalPosition = input.position();
            int type = getUnsignedInt16(input);
            int headerSize = getUnsignedInt16(input);
            long chunkSize = getUnsignedInt32(input);
            long chunkRemaining = chunkSize - 8;
            if (chunkRemaining > input.remaining()) {
                input.position(input.limit());
                return null;
            }
            if (headerSize < HEADER_MIN_SIZE_BYTES) {
                throw new XmlParserException(
                        "Malformed chunk: header too short: " + headerSize + " bytes");
            } else if (headerSize > chunkSize) {
                throw new XmlParserException(
                        "Malformed chunk: header too long: " + headerSize + " bytes. Chunk size: "
                                + chunkSize + " bytes");
            }
            int contentStartPosition = originalPosition + headerSize;
            long chunkEndPosition = originalPosition + chunkSize;
            Chunk chunk =
                    new Chunk(
                            type,
                            sliceFromTo(input, originalPosition, contentStartPosition),
                            sliceFromTo(input, contentStartPosition, chunkEndPosition));
            input.position((int) chunkEndPosition);
            return chunk;
        }
    }

    private static class StringPool {
        private static final int FLAG_UTF8 = 1 << 8;
        private final ByteBuffer mChunkContents;
        private final ByteBuffer mStringsSection;
        private final int mStringCount;
        private final boolean mUtf8Encoded;
        private final Map<Integer, String> mCachedStrings = new HashMap<>();

        public StringPool(Chunk chunk) throws XmlParserException {
            ByteBuffer header = chunk.getHeader();
            int headerSizeBytes = header.remaining();
            header.position(Chunk.HEADER_MIN_SIZE_BYTES);
            if (header.remaining() < 20) {
                throw new XmlParserException(
                        "XML chunk's header too short. Required at least 20 bytes. Available: "
                                + header.remaining() + " bytes");
            }
            long stringCount = getUnsignedInt32(header);
            if (stringCount > Integer.MAX_VALUE) {
                throw new XmlParserException("Too many strings: " + stringCount);
            }
            mStringCount = (int) stringCount;
            long styleCount = getUnsignedInt32(header);
            if (styleCount > Integer.MAX_VALUE) {
                throw new XmlParserException("Too many styles: " + styleCount);
            }
            long flags = getUnsignedInt32(header);
            long stringsStartOffset = getUnsignedInt32(header);
            long stylesStartOffset = getUnsignedInt32(header);
            ByteBuffer contents = chunk.getContents();
            if (mStringCount > 0) {
                int stringsSectionStartOffsetInContents =
                        (int) (stringsStartOffset - headerSizeBytes);
                int stringsSectionEndOffsetInContents;
                if (styleCount > 0) {
                    if (stylesStartOffset < stringsStartOffset) {
                        throw new XmlParserException(
                                "Styles offset (" + stylesStartOffset + ") < strings offset ("
                                        + stringsStartOffset + ")");
                    }
                    stringsSectionEndOffsetInContents = (int) (stylesStartOffset - headerSizeBytes);
                } else {
                    stringsSectionEndOffsetInContents = contents.remaining();
                }
                mStringsSection =
                        sliceFromTo(
                                contents,
                                stringsSectionStartOffsetInContents,
                                stringsSectionEndOffsetInContents);
            } else {
                mStringsSection = ByteBuffer.allocate(0);
            }
            mUtf8Encoded = (flags & FLAG_UTF8) != 0;
            mChunkContents = contents;
        }

        public String getString(long index) throws XmlParserException {
            if (index < 0) {
                throw new XmlParserException("Unsuported string index: " + index);
            } else if (index >= mStringCount) {
                throw new XmlParserException(
                        "Unsuported string index: " + index + ", max: " + (mStringCount - 1));
            }
            int idx = (int) index;
            String result = mCachedStrings.get(idx);
            if (result != null) {
                return result;
            }
            long offsetInStringsSection = getUnsignedInt32(mChunkContents, idx * 4);
            if (offsetInStringsSection >= mStringsSection.capacity()) {
                throw new XmlParserException(
                        "Offset of string idx " + idx + " out of bounds: " + offsetInStringsSection
                                + ", max: " + (mStringsSection.capacity() - 1));
            }
            mStringsSection.position((int) offsetInStringsSection);
            result =
                    (mUtf8Encoded)
                            ? getLengthPrefixedUtf8EncodedString(mStringsSection)
                            : getLengthPrefixedUtf16EncodedString(mStringsSection);
            mCachedStrings.put(idx, result);
            return result;
        }
        private static String getLengthPrefixedUtf16EncodedString(ByteBuffer encoded)
                throws XmlParserException {
            int lengthChars = getUnsignedInt16(encoded);
            if ((lengthChars & 0x8000) != 0) {
                lengthChars = ((lengthChars & 0x7fff) << 16) | getUnsignedInt16(encoded);
            }
            if (lengthChars > Integer.MAX_VALUE / 2) {
                throw new XmlParserException("String too long: " + lengthChars + " uint16s");
            }
            int lengthBytes = lengthChars * 2;
            byte[] arr;
            int arrOffset;
            if (encoded.hasArray()) {
                arr = encoded.array();
                arrOffset = encoded.arrayOffset() + encoded.position();
                encoded.position(encoded.position() + lengthBytes);
            } else {
                arr = new byte[lengthBytes];
                arrOffset = 0;
                encoded.get(arr);
            }
            if ((arr[arrOffset + lengthBytes] != 0)
                    || (arr[arrOffset + lengthBytes + 1] != 0)) {
                throw new XmlParserException("UTF-16 encoded form of string not NULL terminated");
            }
            return new String(arr, arrOffset, lengthBytes, StandardCharsets.UTF_16LE);
        }
        private static String getLengthPrefixedUtf8EncodedString(ByteBuffer encoded)
                throws XmlParserException {
            int lengthBytes = getUnsignedInt8(encoded);
            if ((lengthBytes & 0x80) != 0) {
                lengthBytes = ((lengthBytes & 0x7f) << 8) | getUnsignedInt8(encoded);
            }
            lengthBytes = getUnsignedInt8(encoded);
            if ((lengthBytes & 0x80) != 0) {
                lengthBytes = ((lengthBytes & 0x7f) << 8) | getUnsignedInt8(encoded);
            }
            byte[] arr;
            int arrOffset;
            if (encoded.hasArray()) {
                arr = encoded.array();
                arrOffset = encoded.arrayOffset() + encoded.position();
                encoded.position(encoded.position() + lengthBytes);
            } else {
                arr = new byte[lengthBytes];
                arrOffset = 0;
                encoded.get(arr);
            }
            if (arr[arrOffset + lengthBytes] != 0) {
                throw new XmlParserException("UTF-8 encoded form of string not NULL terminated");
            }
            return new String(arr, arrOffset, lengthBytes, StandardCharsets.UTF_8);
        }
    }

    private static class ResourceMap {
        private final ByteBuffer mChunkContents;
        private final int mEntryCount;

        public ResourceMap(Chunk chunk) throws XmlParserException {
            mChunkContents = chunk.getContents().slice();
            mChunkContents.order(chunk.getContents().order());
            // Each entry of the map is four bytes long, containing the int32 resource ID.
            mEntryCount = mChunkContents.remaining() /  4;
        }

        public int getResourceId(long index) {
            if ((index < 0) || (index >= mEntryCount)) {
                return 0;
            }
            int idx = (int) index;
            // Each entry of the map is four bytes long, containing the int32 resource ID.
            return mChunkContents.getInt(idx * 4);
        }
    }

    private static ByteBuffer sliceFromTo(ByteBuffer source, long start, long end) {
        if (start < 0) {
            throw new IllegalArgumentException("start: " + start);
        }
        if (end < start) {
            throw new IllegalArgumentException("end < start: " + end + " < " + start);
        }
        int capacity = source.capacity();
        if (end > source.capacity()) {
            throw new IllegalArgumentException("end > capacity: " + end + " > " + capacity);
        }
        return sliceFromTo(source, (int) start, (int) end);
    }

    private static ByteBuffer sliceFromTo(ByteBuffer source, int start, int end) {
        if (start < 0) {
            throw new IllegalArgumentException("start: " + start);
        }
        if (end < start) {
            throw new IllegalArgumentException("end < start: " + end + " < " + start);
        }
        int capacity = source.capacity();
        if (end > source.capacity()) {
            throw new IllegalArgumentException("end > capacity: " + end + " > " + capacity);
        }
        int originalLimit = source.limit();
        int originalPosition = source.position();
        try {
            source.position(0);
            source.limit(end);
            source.position(start);
            ByteBuffer result = source.slice();
            result.order(source.order());
            return result;
        } finally {
            source.position(0);
            source.limit(originalLimit);
            source.position(originalPosition);
        }
    }
    private static int getUnsignedInt8(ByteBuffer buffer) {
        return buffer.get() & 0xff;
    }
    private static int getUnsignedInt16(ByteBuffer buffer) {
        return buffer.getShort() & 0xffff;
    }
    private static long getUnsignedInt32(ByteBuffer buffer) {
        return buffer.getInt() & 0xffffffffL;
    }
    private static long getUnsignedInt32(ByteBuffer buffer, int position) {
        return buffer.getInt(position) & 0xffffffffL;
    }

    public static class XmlParserException extends Exception {
        private static final long serialVersionUID = 1L;
        public XmlParserException(String message) {
            super(message);
        }
        public XmlParserException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
