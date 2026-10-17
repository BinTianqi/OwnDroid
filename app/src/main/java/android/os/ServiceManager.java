package android.os;

import androidx.annotation.Keep;

@Keep
public interface ServiceManager {
    static IBinder getService(String name) {
        throw new UnsupportedOperationException();
    }
}
