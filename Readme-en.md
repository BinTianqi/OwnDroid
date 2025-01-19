[简体中文](Readme.md)

# OwnDroid

Use Android Device owner privilege to manage your device.

## Features

- System
  - Options: disable camera, disable screenshot, master volume mute, disable USB signal...
  - Permission policy
  - _Wipe data_
  - ...
- Network
  - Minimum Wi-Fi security level
  - Always-on VPN
  - Network logging
  - ...
- Work profile
  - Create work profile
  - Suspend personal apps
  - ...
- Applications
  - Suspend/hide app
  - Block app uninstallation
  - Install/uninstall app
  - ...
- User restriction
  - Network: disable configuring mobile network, disable configuring Wi-Fi, disable SMS, disable outgoing calls...
  - Connection: disable bluetooth, disable configuring location, disable USB file transfer, disable printing...
  - Applications: disable installing/uninstalling app...
  - Users: disable adding/removing/switching user...
  - Media: disable configuring brightness, disable adjusting volume...
  - Other: disable modifying accounts, disable configuring locale, disable factory reset, disable debug features...
- User manager
  - User information
  - Start/switch/stop/delete user
  - Create user
  - ...
- Password and keyguard
  - _Reset password_
  - Require password complexity
  - Set screen timeout
  - ...

## Activate

- Shizuku (recommended)
- Execute command in adb shell: `dpm set-device-owner com.bintianqi.owndroid/.Receiver`

## FAQ

### Activating

#### Already some accounts on the device

```text
java.lang.IllegalStateException: Not allowed to set the device owner because there are already some accounts on the device
```

Solutions:
- Freeze apps who hold those accounts.
- Delete these accounts.
- Use LSPosed module [HookDPM](https://github.com/BinTianqi/HookDPM).

#### Already several users on the device

```text
java.lang.IllegalStateException: Not allowed to set the device owner because there are already several users on the device
```

Solutions:
- Delete secondary users.
- Use LSPosed module [HookDPM](https://github.com/BinTianqi/HookDPM).

> [!NOTE]
> Some systems have features such as app cloning and children space, which are usually users.

#### MIUI

```text
java.lang.SecurityException: Neither user 2000 nor current process has android.permission.MANAGE_DEVICE_ADMINS.
```

Solutions:
- Enable `USB debugging (Security setting)` in developer options.
- Execute activating command in root shell.

#### ColorOS

```text
java.lang.IllegalStateException: Unexpected @ProvisioningPreCondition
```

Solution: Use OwnDroid testkey version

## API

|    ID     | Description      | Extras                                | Minimum Android version |
|:---------:|------------------|---------------------------------------|:-----------------------:|
|   HIDE    | Hide an app      | `package`: package name of target app |                         |
|  UNHIDE   | Unhide an app    | `package`: package name of target app |                         |
|  SUSPEND  | Suspend an app   | `package`: package name of target app |            7            |
| UNSUSPEND | Unsuspend an app | `package`: package name of target app |            7            |
|   LOCK    | Lock screen      |                                       |                         |

Use this API in adb shell
```shell
am broadcast -a com.bintianqi.owndroid.action.<ID> -n com.bintianqi.owndroid/.ApiReceiver --es key <API_KEY>
# Example
am broadcast -a com.bintianqi.owndroid.action.HIDE -n com.bintianqi.owndroid/.ApiReceiver --es key abcdefg --es package com.example.app
```
If the return value is 0, the operation is successful.

## License

[License.md](LICENSE.md)

> Copyright (C)  2024  BinTianqi
>
> This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
>
> This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
>
> You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
