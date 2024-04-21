apksigner sign --key testkey.pk8 --cert testkey.x509.pem ../app/build/outputs/apk/release/app-release-unsigned.apk
mv ../app/build/outputs/apk/release/app-release-unsigned.apk ./app-release-testkey.apk
