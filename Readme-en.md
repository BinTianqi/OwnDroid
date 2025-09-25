[日本語](Readme-ja.md) | [简体中文](Readme.md)

# OwnDroid

Use Android's DevicePolicyManager API to manage your device.

## Download

- [IzzyOnDroid F-Droid Repository](https://apt.izzysoft.de/fdroid/index/apk/com.bintianqi.owndroid)
- [Releases on GitHub](https://github.com/BinTianqi/OwnDroid/releases)

> [!NOTE]
> ColorOS users should download testkey version from releases on GitHub

## Features

- System: disable camera, disable screenshot, master volume mute, disable USB signal, lock task mode, wipe data...
- Network: add/modify/delete Wi-Fi, network stats, network logging...
- Applications: suspend/hide app, block app uninstallation, grant/revoke permissions, clear app storage, install/uninstall app...
- User restriction: disable SMS, disable outgoing call, disable bluetooth, disable NFC, disable USB file transfer, disable app installing/uninstalling...
- Users: user information, create/start/switch/stop/delete user...
- Password and keyguard: reset password, set screen timeout...

## Working modes

- Device owner (recommended)

  Activating methods:
  - Shizuku
  - Dhizuku
  - Root
  - ADB shell command `dpm set-device-owner com.bintianqi.owndroid/.Receiver`
- [Dhizuku](https://github.com/iamr0s/Dhizuku)
- Work profile

## FAQ

### Already some accounts on the device

```text
java.lang.IllegalStateException: Not allowed to set the device owner because there are already some accounts on the device
```

Solutions:
- Freeze apps who hold those accounts.
- Delete these accounts.

### Already several users on the device

```text
java.lang.IllegalStateException: Not allowed to set the device owner because there are already several users on the device
```

Solutions:
- Delete secondary users.

> [!NOTE]
> Some systems have features such as app cloning and children space, which are usually users.

#### Device owner is already set

```text
java.lang.IllegalStateException: Trying to set the device owner (com.bintianqi.owndroid/.Receiver), but device owner (xxx) is already set.
```

Only 1 device owner can exist on a device. Please deactivate the existing device owner first.

### MIUI & HyperOS

```text
java.lang.SecurityException: Neither user 2000 nor current process has android.permission.MANAGE_DEVICE_ADMINS.
```

Solutions:
- Enable `USB debugging (Security setting)` in developer options.
- Or execute activating command in root shell.

### ColorOS

```text
java.lang.IllegalStateException: Unexpected @ProvisioningPreCondition
```

Solution: Use OwnDroid testkey version

### Samsung

```text
user limit reached
```

Samsung restricts Android's multiple users feature. There is currently no solution.

## API

OwnDroid provides an API based on Intent and BroadcastReceiver.

| ID                       | Extras                 | Minimum Android version |
|--------------------------|------------------------|:-----------------------:|
| `HIDE`                   | `package`              |                         |
| `UNHIDE`                 | `package`              |                         |
| `SUSPEND`                | `package`              |            7            |
| `UNSUSPEND`              | `package`              |            7            |
| `ADD_USER_RESTRICTION`   | `restriction`          |                         |
| `CLEAR_USER_RESTRICTION` | `restriction`          |                         |
| `SET_PERMISSION_DEFAULT` | `package` `permission` |            6            |
| `SET_PERMISSION_GRANTED` | `package` `permission` |            6            |
| `SET_PERMISSION_DENIED`  | `package` `permission` |            6            |
| `SET_CAMERA_DISABLED`    |                        |                         |
| `SET_CAMERA_ENABLED`     |                        |                         |
| `SET_USB_DISABLED`       |                        |           12            |
| `SET_USB_ENABLED`        |                        |           12            |
| `LOCK`                   |                        |                         |
| `REBOOT`                 |                        |            7            |

[Available user restrictions](https://developer.android.com/reference/android/os/UserManager#constants_1)

```shell
# An example of hiding app in ADB shell
am broadcast -a com.bintianqi.owndroid.action.HIDE -n com.bintianqi.owndroid/.ApiReceiver --es key abcdefg --es package com.example.app
```

```kotlin
// An example of hiding app in Kotlin
val intent = Intent("com.bintianqi.owndroid.action.HIDE")
    .setComponent(ComponentName("com.bintianqi.owndroid", "com.bintianqi.owndroid.ApiReceiver"))
    .putExtra("key", "abcdefg")
    .putExtra("package", "com.example.app")
context.sendBroadcast(intent)
```

## Build

You can use Gradle in command line to build OwnDroid.
```shell
# Use testkey for signing (default)
./gradlew build
# Use your custom .jks key for signing
./gradlew build -PStoreFile="/path/to/your/jks/file" -PStorePassword="YOUR_KEYSTORE_PASSWORD" -PKeyPassword="YOUR_KEY_PASSWORD" -PKeyAlias="YOUR_KEY_ALIAS"
```
(Use `./gradlew.bat` instead on Windows)

## License

[License.md](LICENSE.md)

> Copyright (C)  2024  BinTianqi
>
> This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
>
> This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
>
> You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
