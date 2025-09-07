[English](Readme-en.md) | [日本語](Readme-ja.md)

# OwnDroid

使用安卓的设备策略管理器API管理你的设备。

## 下载

- [IzzyOnDroid F-Droid Repository](https://apt.izzysoft.de/fdroid/index/apk/com.bintianqi.owndroid)
- [Releases on GitHub](https://github.com/BinTianqi/OwnDroid/releases)

> [!NOTE]
> ColorOS用户应在GitHub上的releases下载testkey版本

## 功能

- 系统：禁用摄像头、禁止截屏、全局静音、禁用USB信号、锁定任务模式、清除数据...
- 网络：添加/修改/删除 Wi-Fi、网络统计、网络日志...
- 应用：挂起/隐藏应用、阻止应用卸载、授予/撤销权限、清除应用存储、安装/卸载应用...
- 用户限制：禁止发送短信、禁止拨出电话、禁用蓝牙、禁用NFC、禁用USB文件传输、禁止安装/卸载应用...
- 用户：用户信息、创建/启动/切换/停止/删除用户...
- 密码与锁屏：重置密码、设置屏幕超时...

## 工作模式

- Device owner（推荐）

  激活方式：
  - Shizuku
  - Dhizuku
  - Root
  - ADB shell命令 `dpm set-device-owner com.bintianqi.owndroid/.Receiver`
- [Dhizuku](https://github.com/iamr0s/Dhizuku)
- 工作资料

## FAQ

### 设备上有账号

```text
java.lang.IllegalStateException: Not allowed to set the device owner because there are already some accounts on the device
```

解决办法：
- 冻结持有这些账号的app。
- 删除这些账号。

### 设备上有多个用户

```text
java.lang.IllegalStateException: Not allowed to set the device owner because there are already several users on the device
```

解决办法：
- 删除次级用户。

> [!NOTE]
> 一些系统有应用克隆、儿童空间等功能，它们通常是用户。

#### Device owner 已存在

```text
java.lang.IllegalStateException: Trying to set the device owner (com.bintianqi.owndroid/.Receiver), but device owner (xxx) is already set.
```

一个设备只能存在一个device owner，请先停用已存在的device owner。

### MIUI & HyperOS

```text
java.lang.SecurityException: Neither user 2000 nor current process has android.permission.MANAGE_DEVICE_ADMINS.
```

解决办法： 在开发者设置中打开`USB调试（安全设置）`，或在root命令行中执行激活命令。

### ColorOS

```text
java.lang.IllegalStateException: Unexpected @ProvisioningPreCondition
```

解决办法：使用 OwnDroid testkey 版本

### 三星

```text
user limit reached
```

三星限制了多用户功能，暂无解决办法。

## API

| ID                       | Extra                  | 最小安卓版本 |
|--------------------------|------------------------|:------:|
| `HIDE`                   | `package`              |        |
| `UNHIDE`                 | `package`              |        |
| `SUSPEND`                | `package`              |   7    |
| `UNSUSPEND`              | `package`              |   7    |
| `ADD_USER_RESTRICTION`   | `restriction`          |        |
| `CLEAR_USER_RESTRICTION` | `restriction`          |        |
| `SET_PERMISSION_DEFAULT` | `package` `permission` |   6    |
| `SET_PERMISSION_GRANTED` | `package` `permission` |   6    |
| `SET_PERMISSION_DENIED`  | `package` `permission` |   6    |
| `LOCK`                   |                        |        |
| `REBOOT`                 |                        |   7    |

[可用的用户限制](https://developer.android.google.cn/reference/android/os/UserManager#constants_1)

```shell
# 一个在ADB shell中隐藏app的示例
am broadcast -a com.bintianqi.owndroid.action.HIDE -n com.bintianqi.owndroid/.ApiReceiver --es key abcdefg --es package com.example.app
```

```kotlin
// 一个在Kotlin中隐藏app的示例
val intent = Intent("com.bintianqi.owndroid.action.HIDE")
    .setComponent(ComponentName("com.bintianqi.owndroid", "com.bintianqi.owndroid.ApiReceiver"))
    .putExtra("key", "abcdefg")
    .putExtra("package", "com.example.app")
context.sendBroadcast(intent)
```

## 构建

你可以在命令行中使用Gradle以构建OwnDroid
```shell
# 使用testkey签名（默认）
./gradlew build
# 使用你的jks密钥签名
./gradlew build -PStoreFile="/path/to/your/jks/file" -PStorePassword="YOUR_KEYSTORE_PASSWORD" -PKeyPassword="YOUR_KEY_PASSWORD" -PKeyAlias="YOUR_KEY_ALIAS"
```
（在Windows系统中应使用`./gradlew.bat`)

## 许可证

[License.md](LICENSE.md)

> Copyright (C)  2024  BinTianqi
>
> This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
>
> This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
>
> You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
