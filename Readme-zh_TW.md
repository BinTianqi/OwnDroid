[English](Readme.md) | [简体中文](Readme-zh_CN.md) | [日本語](Readme-ja.md)

# OwnDroid

使用 Android 的 DevicePolicyManager API 管理您的裝置。

## 下載

- [IzzyOnDroid F-Droid 軟體庫](https://apt.izzysoft.de/fdroid/index/apk/com.bintianqi.owndroid)
- [GitHub 上的發行版本](https://github.com/BinTianqi/OwnDroid/releases)

> [!NOTE]
> ColorOS 使用者請從 GitHub 發行版本下載 testkey 版本

## 功能

- 系統：停用相機、停用截圖、全域音量靜音、停用 USB 訊號、鎖定工作模式、清除資料...
- 網路：新增／修改／刪除 Wi-Fi、網路統計、網路記錄...
- 應用程式：暫停／隱藏 App、封鎖 App 解除安裝、授予／撤銷權限、清除 App 儲存空間、安裝／解除安裝 App...
- 使用者限制：停用簡訊、停用撥出電話、停用藍牙、停用 NFC、停用 USB 檔案傳輸、停用 App 安裝／解除安裝...
- 使用者：使用者資訊、建立／啟動／切換／停止／刪除使用者...
- 密碼與鎖定螢幕：重設密碼、設定螢幕逾時...

## 工作模式

- 裝置擁有者（Device owner）（建議）

  啟用方式：
  - Shizuku
  - Dhizuku
  - Root
  - ADB shell 指令 `dpm set-device-owner com.bintianqi.owndroid/.Receiver`
- [Dhizuku](https://github.com/iamr0s/Dhizuku)
- 工作設定檔（Work profile）

## 常見問題

### 裝置上已有帳號

```text
java.lang.IllegalStateException: Not allowed to set the device owner because there are already some accounts on the device
```

自 v8.4 起，使用 Shizuku 或 Root 啟用裝置擁有者時，OwnDroid 會自動嘗試繞過此限制。

若仍然看到此錯誤，您可以嘗試凍結這些帳號所屬的 App，或刪除這些帳號。

### 裝置上已有多位使用者

```text
java.lang.IllegalStateException: Not allowed to set the device owner because there are already several users on the device
```

解決方法：刪除次要使用者，包括工作設定檔、私人空間與 App 複製。

### 已設定裝置擁有者

```text
java.lang.IllegalStateException: Trying to set the device owner (com.bintianqi.owndroid/.Receiver), but device owner (xxx) is already set.
```

一台裝置上只能有一個裝置擁有者。請先停用現有的裝置擁有者。

### MIUI 與 HyperOS

```text
java.lang.SecurityException: Neither user 2000 nor current process has android.permission.MANAGE_DEVICE_ADMINS.
```

解決方法：
- 在開發人員選項中啟用「USB 偵錯（安全設定）」。
- 或在 Root shell 中執行啟用指令。

### ColorOS

```text
java.lang.IllegalStateException: Unexpected @ProvisioningPreCondition
```

解決方法：使用 OwnDroid 的 testkey 版本

testkey 版本與簽署版本僅簽章不同，功能上沒有差異。

### Samsung（三星）

```text
user limit reached
```

三星限制了 Android 的多使用者功能，目前沒有解決方法。

### 建立工作設定檔／使用者

在大多數裝置上，一旦設定了裝置擁有者，系統就不允許建立工作設定檔，
因為設定裝置擁有者時，系統會加上 `no_add_managed_profile` 使用者限制。
裝置擁有者無法修改由系統設定的使用者限制，但如果您的裝置已 Root，可以在 adb shell 中執行下列指令來停用此限制。
注意：裝置擁有者與工作設定檔擁有者不能是同一個 App，否則裝置擁有者會在重新啟動時失去其權限。

```shell
pm set-user-restriction no_add_user 0
pm set-user-restriction no_add_managed_profile 0
pm set-user-restriction no_add_private_profile 0
pm set-user-restriction no_add_clone_profile 0
```

繞過這些限制的風險請自行承擔，可能會造成非預期的行為，例如系統可能在重新啟動時，默默刪除您建立的使用者。

部分系統在設定裝置擁有者後，會停用 Android 設定中的新增使用者功能。
您必須在 OwnDroid 中建立使用者；或者如果您已 Root，可在 adb shell 中執行上述指令來移除該限制。

## 進階使用者

### API

OwnDroid 提供以 Intent 為基礎的 API。您需要在設定中設定 API 金鑰並啟用 API。括號中的數字代表所需的最低 Android 版本。

- HIDE(package: String)
- UNHIDE(package: String)
- SUSPEND(package: String) (7)
- UNSUSPEND(package: String) (7)
- DISABLE_METERED_DATA(package: String) (9)
- ENABLE_METERED_DATA(package: String) (9)
- DISABLE_USER_CONTROL(package: String) (11)
- ENABLE_USER_CONTROL(package: String) (11)
- BLOCK_UNINSTALL(package: String)
- UNBLOCK_UNINSTALL(package: String)
- CLEAR_APP_STORAGE(package: String) () (9)
- ADD_USER_RESTRICTION(restriction: Boolean)
- CLEAR_USER_RESTRICTION(restriction: Boolean)
- SET_PERMISSION_DEFAULT(package: String, permission: String) (6)
- SET_PERMISSION_GRANTED(package: String, permission: String) (6)
- SET_PERMISSION_DENIED(package: String, permission: String) (6)
- SET_SCREEN_CAPTURE_DISABLED()
- SET_SCREEN_CAPTURE_ENABLED()
- SET_CAMERA_DISABLED()
- SET_CAMERA_ENABLED()
- SET_USB_DISABLED() (12)
- SET_USB_ENABLED() (12)
- SET_ALWAYS_ON_VPN(package: String, lockdown: Boolean = false) (7)
- CLEAR_ALWAYS_ON_VPN() (7)
- LOCK()
- REBOOT() (7)

```shell
# 在 ADB shell 中隱藏 App 的範例
am broadcast -a com.bintianqi.owndroid.action.HIDE -n com.bintianqi.owndroid/.ApiReceiver --es key abcdefg --es package com.example.app
```

```kotlin
// 在 Kotlin 中隱藏 App 的範例
val intent = Intent("com.bintianqi.owndroid.action.HIDE")
    .setComponent(ComponentName("com.bintianqi.owndroid", "com.bintianqi.owndroid.ApiReceiver"))
    .putExtra("key", "abcdefg")
    .putExtra("package", "com.example.app")
context.sendBroadcast(intent)
```

[可用的使用者限制](https://developer.android.com/reference/android/os/UserManager#constants_1)

## 建置

您可以在命令列中使用 Gradle 建置 OwnDroid。

```shell
# 使用 testkey 簽署（預設）
./gradlew build
# 使用您自己的 .jks 金鑰簽署
./gradlew build -PStoreFile="/path/to/your/jks/file" -PStorePassword="YOUR_KEYSTORE_PASSWORD" -PKeyPassword="YOUR_KEY_PASSWORD" -PKeyAlias="YOUR_KEY_ALIAS"
```

（在 Windows 上請改用 `./gradlew.bat`）

## 參與貢獻

歡迎貢獻！

1. [Fork](https://github.com/BinTianqi/OwnDroid/fork) 此儲存庫（取消勾選預設已選取的「Copy the master branch only」）
2. 使用 git 複製您自己的儲存庫（請使用 `dev` 分支）
3. 進行修改
4. 在您的裝置或模擬器上執行 App，測試您的修改（程式碼貢獻必須測試，翻譯貢獻則為選用）
5. 提交並推送變更
6. 在此儲存庫中開啟 Pull Request

## 授權條款

[License.md](LICENSE.md)

> Copyright (C)  2026  BinTianqi
>
> This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
>
> This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
>
> You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
