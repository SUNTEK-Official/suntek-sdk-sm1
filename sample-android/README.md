# SUNTEK SM1 Sample Android Application (`SUNTEKSDKDemo`)

Multi-module Android Studio test suite (`:SUNTEKSDKTest`, `:SystemLib`, `:zxing`) pre-linked against `SUNTEKSDKInterface_202607201428_V0.0.345_345_release.aar`.

- Configure your own local signing keystore in `SUNTEKSDKTest/build.gradle` if producing release-signed test APKs.
- Configure payment server endpoints at runtime inside the test UI rather than embedding production hosts.
