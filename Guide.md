# 使用指南

欢迎来到Android owner使用教程

在这里了解各个功能所需的权限、兼容的安卓版本和注意事项

## 目录


1. [权限](#权限)

2. [系统](#系统)

3. [网络](#网络)

4. [工作资料](#工作资料)

5. [应用管理](#应用管理)

6. [用户限制](#用户限制)

7. [用户管理](#用户管理)

8. [密码与锁屏](#密码与锁屏)

## 权限

### Device admin

权限最小

#### 激活

- 安卓设置中激活（你可以在此应用中跳转到安卓设置的激活界面）
- ADB命令

ADB激活命令：
```shell
adb shell dpm set-active-admin com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver
```

一个设备可以同时存在多个Device admin。
#### 停用

- 此应用的”权限“界面中停用
- 安卓设置中停用
- ~~ADB命令停用~~（参考Device owner的ADB命令停用）

### Profile owner

权限中等

如无特别说明，Profile owner包括主用户、工作资料和受管理用户中的Profile owner

#### 激活

- 使用ADB激活（不推荐，如果能使用ADB，建议激活Device owner），只能有一个Profile owner
- 创建工作资料，此应用会成为工作资料中的Profile owner，只能有一个Profile owner
- 成为Device owner后创建并管理用户，此应用会成为新用户的Profile owner，每个用户各有一个Profile owner

ADB激活命令：

```shell
adb shell dpm set-profile-owner com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver
```

#### 停用

主用户：”权限“界面中停用

工作资料：”设备控制“界面中的”清除数据“，会删除工作资料

受管理的用户：删除用户即可

### Device owner

权限最高

#### 激活

- 使用ADB激活
- 恢复出厂设置并开机后，使用NFC发送这个app的下载链接（没试过）
- 使用Root权限往/data/system里面放一个xml文件（没试过）

ADB激活命令：

```shell
adb shell dpm set-device-owner com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver
```

ADB激活有一定局限性

激活前必须删除所有用户（user），否则会报错。你可以使用下面这条ADB命令查看已有的用户

```shell
adb shell pm list users
```

激活前也要删除所有账号（account），否则会报错。你可以使用下面这条ADB命令查看已有的账号

```shell
adb shell dumpsys account
```

上面两个是安卓系统的限制，此外，还有设备生产商的限制

MIUI：需要在开发者选项中打开”USB调试（安全设置）“

ColorOS：完全不支持Device owner

#### 停用

- 恢复出厂设置（这是官方推荐的做法）
- 在“权限”页面停用（推荐）
- ADB命令停用（麻烦）

ADB命令停用十分麻烦，你需要修改AndroidManifest.xml并自己编译项目。

你需要把AndroidManifest.xml中第39行的`android:testOnly="false"`的值改为true

由于签名校验，如果你已经安装了release，那这个方法没用

然后，使用这条ADB命令停用

```shell
adb shell dpm remove-active-admin com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver
```

使用这条命令也可以停用Device admin和Profile owner

以上三种方法停用Device owner都会同时停用Device admin

### 设备唯一标识码

需API31或以上

需要先设置[组织ID](#组织ID)，同一个组织ID的设备唯一标识码相同，恢复出厂设置不变

### 组织名称

需API26或以上

需要Device owner或Profile owner

设置组织名称后会在一些地方显示此设备归xxx所有

### 不受控制的账号类型

需Device owner或Profile owner

作用未知

### 锁屏信息

需API24或以上

需要Device owner或Profile owner

在锁屏界面上显示的一段简短的消息

### 提供支持的长/短消息

需API24或以上

如果用户试图使用被挂起的应用或被禁用的功能，会显示提供支持的短消息

提供支持的长消息不知道有啥用

### 转移所有权

需要Device owner或Profile owner

需要API28或以上

转移设备所有权到另外一个Device owner或Profile owner

目标应用必须是Device admin且支持被转移所有权

## 系统

### 禁用相机

需要的权限：Device owner或Profile owner

### 禁止截屏

需要的权限：Device owner或Profile owner

也会禁止AOSP自带的录屏

### 隐藏状态栏

需要的权限：Device owner或附属用户的Profile owner

需要API34或以上

状态栏仍会显示时间和系统图标，但是通知图标会隐藏，并且状态栏不能下拉

### 自动设置时间

需要的权限：Device owner或由组织拥有的工作资料的Profile owner

需要API30或以上

### 自动设置时区

需要的权限：Device owner或由组织拥有的工作资料的Profile owner

需要API30或以上

### 自动设置时间（弃用）

需要的权限：Device owner或Profile owner

自从API30开始弃用，在API30或以上的设备上，此功能不会显示

### 全局静音

需要的权限：Device owner或Profile owner

### 备份服务

需要的权限：Device owner或Profile owner

需要API26或以上

### 禁止蓝牙分享联系人

需要的权限：Device owner或Profile owner

需要API23或以上

### 通用标准模式

需要的权限：Device owner

需要API30或以上

### USB信号

需要的权限：Device owner或由组织拥有的工作资料的Profile owner

需要API31或以上

有的设备不支持

### 锁屏方式

禁用和启用锁屏方式，需要无锁屏密码

需要的权限：Device owner或附属用户的Profile owner

需要API28或以上

### 立即锁屏

选项：重新输入密码（需API26或以上）

需要的权限：Device admin

无论勾不勾选这个选项，解锁时都需要重新输入密码

### 请求错误报告

需要API24或以上

需要的权限：Device owner

### 重启

需要API24或以上

需要的权限：Device owner

### 修改时间

输入从Epoch(1970/1/1 00:00:00 UTC)到你想设置的时间(毫秒)

需要的权限：Device owner或由组织拥有的工作资料的Profile owner

### 权限策略

当应用请求权限时执行的操作

- 默认（由用户决定）
- 自动允许
- 自动拒绝

需要的权限：Device owner或Profile owner

### MTE策略

MTE: Memory Tagging Extension（内存标记拓展）[安卓开发者：MTE](https://developer.android.google.cn/ndk/guides/arm-mte?hl=zh-cn)

需要安卓14和Armv9

需要的权限：Device owner

选项：

- 由用户决定
- 开启
- 关闭

### 附近App共享&附近通知共享

需要的权限：Device owner或Profile owner

需要API31或以上

选项：

- 由用户决定
- 启用
- 禁用
- 在足够安全时启用

### 锁定任务模式

需要的权限：Device owner

需要API28或以上

选项：

- 禁用全部
- 自定义
  - 允许状态栏信息
  - 允许通知
  - 允许返回主屏幕（启动器需在白名单内）
  - 允许打开后台应用概览
  - 允许全局行为（比如长按电源键的对话框）
  - 允许锁屏（如果不允许，即使有密码也不会锁屏）
  - 阻止启动未允许的应用（阻止白名单应用启动非白名单应用，需要API30）

### Ca证书

需要的权限：Device owner或Profile owner

需要先选择一个证书（一般是*.0）

- 查看这个证书是否已安装
- 安装这个证书
- 卸载这个证书（如果已安装）

或者清除所有用户证书

### 安全日志

需要的权限：Device owner

需要API24或以上

### 系统更新策略

需要的权限：Device owner

查看待安装的更新是否安全补丁

系统更新策略：

- 准备好后立即更新
- 在某段时间里更新
- 延迟30天
- 默认（由用户决定）

### 恢复出厂设置

**谨慎使用**

需要的权限：Device admin

选项：

- 默认

- 清除外部存储
- 清除受保护的数据（仅Device owner）
- 清除eUICC
- 静默清除（不会向用户显示原因）

方法：

- WipeData
- WipeDevice（需要API34或以上，需要Device owner或由组织拥有的工作资料的Profile owner）

恢复出厂原因：需要API28或以上，只有WipeData能用

如果在受管理用户中使用，会清除那个用户的数据并跳转到主用户

如果在工作资料中使用，会删除工作资料

API34或以上将不能在系统用户中使用WipeData，如果要恢复出厂设置，请使用WipeDevice

## 网络

这个页面需要API24或以上才能进入

### 查看Wi-Fi Mac地址

需要的权限：Device owner或由组织拥有的工作资料的Profile owner

需要API24或以上

得到的是设备真实的Wi-Fi Mac地址，不是连接Wi-Fi时随机生成的Mac地址

### 优先网络服务

需要的权限：Device owner

需要API33或以上

功能开发中

### Wi-Fi锁定

需要的权限：Device owner或由组织拥有的工作资料的Profile owner

需要API30或以上

作用未知

### 要求最小Wi-Fi安全等级

需要的权限：Device owner或由组织拥有的工作资料的Profile owner

需要API33或以上

选项：

- 开放
- WEP、WPA(2)-PSK
- WPA-EAP
- WPA3-192bit

用户不能连接低于要求的安全等级的Wi-Fi，如果已经连接了这样的Wi-Fi，会立即断开

### Wi-Fi SSID策略

需要的权限：Device owner或由组织拥有的工作资料的Profile owner

需要API33或以上

模式：

- 黑名单
- 白名单

设置用户能连接/不能连接的Wi-Fi

### 私人DNS

需要的权限：Device owner

需要API29或以上

可以将私人DNS模式设为自动

也可以设置私人DNS主机名

### 网络日志记录

需要的权限：Device owner或工作资料的Profile owner

需要API26或以上

功能开发中

### Wi-Fi密钥对

需要的权限：Device owner或Profile owner

需要API31或以上

可以添加或移除Wi-Fi密钥对

作用未知

### APN设置

需要的权限：Device owner

需要API28或以上

有懂这个的大佬吗？

## 工作资料

工作资料是一种特殊的用户，使用`adb shell pm list user`命令可以看到工作资料，工作资料的默认用户名是“工作资料”或“Work Profile”

### 创建工作资料

设备上不能有Device owner或Profile owner

一个设备只能有一个工作资料

选项：

- 跳过加密（需要API24或以上，没有实际作用）

创建后会跳转到工作资料中的Android owner，请立即按照指引激活工作资料

创建后工作资料中的Android owner会成为Profile owner

在WearOS上创建工作资料会导致SystemUI停止运行一次。WearOS原生的启动器不支持工作资料，你需要使用第三方启动器（比如微软桌面）。你可以通过[ADB命令移除工作资料](#删除工作资料)

此外，不要作死给工作资料重置密码，不然你连输入密码的地方都没有

（只在原生WearOS4(AVD)上测试过）

### 由组织拥有的工作资料

需要API30或以上

成为由组织拥有的工作资料会多很多权限

前提条件：Android owner是工作资料中的Profile owner

首先，你需要在工作资料中的Android owner的“用户管理”页面中查看UserID

然后使用ADB命令成为由组织拥有的工作资料

```shell
adb shell
dpm mark-profile-owner-on-organization-owned-device --user USER_ID com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver
```

把命令中的USER_ID替换为你的UserID

出现“Success”即为成功

### 挂起个人应用

需要的权限：由组织拥有的工作资料的Profile owner

### 资料最长关闭时间

需要的权限：工作资料的Profile owner

用户可以关闭工作资料，如果关闭工作资料的时间超过了在这里设置的时间，会停用个人应用

设置的时间不能小于72小时

### 跨资料Intent过滤器

需要的权限：工作资料的Profile owner

默认情况下，工作资料中的应用不能打开个人应用，个人应用也不可以打开工作资料中的应用

添加Intent过滤器以允许这些行为

### 组织ID

需要的权限：工作资料的Profile owner

需要API31或以上

组织ID长度需在6~64个字符之间

设置组织ID后才能在“权限”页面查看设备唯一标识码，不同的组织ID会有不同的设备唯一标识码

### 删除工作资料

你可以使用 [恢复出厂设置](#恢复出厂设置) 来删除工作资料

如果你的工作资料不是由组织拥有的，你可以打开安卓设置->安全->更多安全设置->设备管理器->带工作资料图标的Android owner->移除工作资料（非原生用户自己找）

你也可以使用ADB命令移除工作资料（把USER_ID替换为工作资料的UserID）

```shell
adb shell pm remove-user USER_ID
```

## 应用管理

如果是工作资料，只能管理工作资料中的应用

如果是受管理的用户，只能管理那个用户中的应用

除了安装应用，所有的操作都需要应用的包名，你可以通过ADB命令查看已安装应用的包名

```shell
adb shell pm list packages
```

### 应用详情

跳转到应用详情，不需要任何权限

### 挂起

需要的权限：Device owner或Profile owner

需要API24或以上

挂起应用，应用图标变为灰色，打开时会提示被IT管理员限制

### 隐藏

需要的权限：Device owner或Profile owner

深度隐藏。`pm list packages`都看不到隐藏的应用。无法卸载隐藏的应用，因为找不到。你一定要记得隐藏的应用的包名，否则你只能去`/data/app`找被隐藏的应用

### VPN常开

需要的权限：Device owner或Profile owner

需要API24或以上

需要应用支持VPN常开

### 防卸载

需要的权限：Device owner或Profile owner

用户无法在应用详情卸载应用，也不能使用`adb uninstall`和`pm uninstall`命令卸载

参考：用户限制->应用->卸载应用

### 禁止用户控制

需要的权限：Device owner或Profile owner

需要API30或以上

用户无法清除这些应用的存储空间和缓存，但是可以在这里清除

### 权限管理

需要的权限：Device owner或Profile owner

使用这个ADB命令查看系统支持的所有权限

```shell
adb shell pm list permissions
```

权限有三种状态：

- 允许
- 拒绝
- 由用户决定

设为允许或拒绝后用户无法在应用管理修改权限，用户也不能通过`pm grant`和`pm revoke`修改权限

从允许或拒绝改为由用户决定会保持当前的允许、拒绝状态

在API31或以上，Profile owner不能再修改传感器相关权限

### 跨资料应用

需要的权限：工作资料的Profile owner

需要API30或以上

设置跨工作资料的应用，需要目标应用支持跨资料（比如GBoard）

### 跨资料微件

需要的权限：工作资料的Profile owner

设置跨工作资料的桌面小部件提供者

### 凭据管理策略

需要的权限：Device owner

需要API34或以上

作用未知

### 许可的无障碍应用&输入法

需要的权限：Device owner或Profile owner

设置许可的无障碍应用和输入法

### 清除应用存储

需要的权限：Device owner或Profile owner

需要API28或以上

清除应用的存储空间

### 默认拨号应用

需要的权限：Device owner或Profile owner

需要API34或以上

### 卸载应用

- 静默卸载（需要Device owner，否则没反应）
- 请求卸载

### 安装应用

- 静默安装（需要Device owner，否则没反应）
- 请求安装

## 用户限制

需要的权限：Device owner或Profile owner

Profile owner无法禁用部分功能，工作资料中部分功能无效，wearOS部分功能无效

功能前面的数字代表最少需要的API等级

打开开关就是禁用对应的功能，默认情况下所有开关都是关闭的

### 网络与互联网

- 配置移动数据
- 配置Wi-Fi
- (24) 数据漫游
- (34) 使用2G(GSM)
- (34) 超宽频段无线电
- (33) 添加Wi-Fi配置
- (33) 修改Wi-Fi状态
- (33) Wi-Fi直连
- (33) Wi-Fi共享
- (33) 分享设备管理器配置的Wi-Fi
- (23) 重置网络
- 配置网络共享
- 配置VPN
- (29) 配置私人DNS（仍可以在网络中设置[私人DNS](#私人DNS)）
- (28) 飞行模式
- 配置小区广播
- 短信
- 拨出电话

### 更多连接

- (26) 蓝牙
- (26) 蓝牙共享
- 分享位置
- (28) 配置位置信息
- (22) Beam发送
- USB文件传输
- 挂载物理媒体（TF卡、U盘等）
- (28) 打印

### 应用

- 安装应用
- (29) 安装未知来源应用（全局）
- 安装未知来源应用
- 卸载应用
- 控制应用
- (34) 修改默认APP

### 媒体

- (28) 调整亮度
- (28) 修改屏幕超时（仍可以在密码与锁屏中设置[屏幕超时](#最大屏幕超时时间)）
- (28) 息屏显示 (AMOLED)
- 调整音量
- 取消麦克风静音
- (31) 切换摄像头使用权限
- (31) 切换麦克风使用权限

### 用户

- 添加用户（仍可以在用户管理中[创建用户](#创建并管理用户)）
- 移除用户（仍可以在用户管理中[移除用户](#用户操作)）
- (28) 切换用户
- (24) 修改用户头像
- 跨用户复制（包括工作资料）
- (28) 分享至工作应用
- (28) 个人和工作密码相同

### 其他

- (26) 自动填充服务
- 配置凭据
- (29) 内容捕获（作用未知）
- (29) 内容建议（作用未知）
- 创建窗口（比如Toast）
- (24) 更换壁纸
- (34) 启用设备管理器（设备管理器就是Device admin）
- (23) 娱乐（仅谷歌商店里的应用）
- 修改账号设置
- (28) 修改语言
- (28) 修改日期、时间
- (28) 系统报错对话框
- 恢复出厂设置
- (23) 安全模式（开机时通过特定按键组合进入的模式）
- 调试功能

## 用户管理

用户（user）不是账号（account）

使用ADB查看所有用户：

```shell
adb shell pm list users
```

用户名前面的数字就是UserID

### 用户信息

用户已解锁：你能看到这个的时候一定解锁了

支持多用户：系统是否支持多用户。WearOS即使写着支持多用户，但不一定支持

系统用户：UserID为0的用户（需API23）

管理员用户：可以创建、删除用户。一个设备可以有多个管理员用户（需API34）

无头系统用户：~~头被砍掉了~~ 系统用户运行着系统服务，但是没有分配给任何人使用，也不能切换到系统用户（需API31）

可以登出：功能未知，无论什么用户都不能登出

临时用户：临时用户登出后或重启后会被删除（需API28）

附属用户：详见[附属用户ID](#附属用户ID)

UserID：不是UID。系统用户的UserID为0，其他用户（包括工作资料）的UserID从10开始计算

序列号：每个用户都不同的序列号

### 用户操作

- 登出当前用户（需要是附属用户的Profile owner，需API28，如果是无头系统用户模式，会切换到前台用户）
- 在后台启动用户（需Device owner和API28）
- 切换至用户（需Device owner）
- 停止用户（需Device owner和API28）
- 移除用户（需Device owner）

### 创建并管理用户

创建一个受管理用户，新用户的头像右下方会有公文包标志

需要Device owner和API24

选项：

- 跳过创建用户向导（切换到新用户之后的向导）
- 临时用户（需API28）
- 启用所有系统应用（有些系统应用在新用户中是默认不启用的，比如谷歌手机上的YouTube）

创建后，Android owner会成为受管理用户中的Profile owner

这个功能在WearOS上使用会导致SystemUI停止运行一次，过几秒恢复正常。创建用户实际上成功了，回到Android owner后能看到新用户的序列号，`pm list users`也能看到新用户。如果切换到新用户，SystemUI无法使用，表现为黑屏（可以用ADB命令启动别的应用）。如果黑屏无法使用，ADB执行下面这个命令（把USER_ID替换成受管理用户的用户序列号）

```shell
adb shell pm remove-user --set-ephemeral-if-in-use USER_ID
```

新用户会被设为临时用户，重启后临时用户会被删除并切换到主用户

（原生WearOS4(AVD)会出现这个问题，其他版本不知道有没有这个问题）

### 使用Intent创建用户

不需要任何权限，但也没啥用，建议Device owner创建并管理用户

可能会导致Android owner停止运行，但是停止运行后没log，所以不知道为什么无法创建

### 附属用户ID

需要Device owner或Profile owner（工作资料中的Profile owner虽然也能设置，但是没有实际作用）

附属用户ID是一个列表，列表中可以有多个不相同的ID，不考虑顺序

当Device owner创建并管理用户时，新的用户不是附属用户。Device owner设置和受管理用户完全相同的附属用户ID后，受管理用户成为附属于Device owner的用户

Device owner无论在何时都是附属于设备的用户

你可以在用户管理->[用户信息](#用户信息)查看当前用户是否附属用户

### 用户名

修改当前用户的用户名

需要Device owner或Profile owner

### 用户会话开始/结束消息

用户会话开始消息：切换到非系统用户时的消息

用户会话结束消息：切换回系统用户时的消息

## 密码与锁屏

**谨慎使用**

**警告！**手表不支持带字母的密码，也不支持超过4位的PIN码！如果你设置了这样的密码（或密码复杂度要求），你将无法解锁你的手表！

### 密码信息

当前密码复杂度：参考要求密码复杂度

密码达到要求：当前密码复杂度是否达到了要求的密码复杂度

密码已错误次数：你能看到这个数字的时候，这个数字一定是0

个人与工作应用密码一致：需要是工作资料的Profile owner

### 密码重置令牌

需要Device owner或Profile owner

需API26或以上

操作：

- 清除令牌
- 设置令牌（如果无锁屏密码，会自动激活令牌）
- 激活令牌（如果有锁屏密码，会跳转到设置中，要求输入锁屏密码）

暂不支持自己输入令牌

### 修改密码

需要4位或以上密码

选项：

- 开机时不要求输入密码（ **危险！** 一旦设置，只能通过恢复出厂设置来取消）
- 不允许其他设备管理员重置密码直至用户输入一次密码

方法：

- 使用令牌重置密码（需API26或以上）
- 重置密码（弃用）（API24之前，Device admin可使用。API24或以上，Device admin只能在没有密码时设置密码，Device owner和Profile owner仍可以在用户解锁设备后更改密码。API26或以上弃用）

### 最大密码错误次数

需要Device owner

输入密码错误次数达到限制后会恢复出厂设置（前提是Android owner有权限恢复出厂设置）

### 密码失效超时时间

需要Device owner

一个密码使用的时间超过这个时间后会要求用户重新设置密码

### 密码历史记录长度

需要Device owner

用户无法输入历史记录内相同的密码

不知道有啥用

### 最大屏幕超时时间

需要Device owner

如果设备无用户交互的时间达到屏幕超时时间就会锁定屏幕，默认是0（由用户决定），在这里设置屏幕超时时间后，用户只能设置比这个短的屏幕超时时间

### 密码复杂度要求

需要API31或以上

需要Device owner或Profile owner

有4个密码复杂度等级：

1. 无（允许不设密码）
2. 低（允许图案和连续性）
3. 中（无连续性，至少4位）
4. 高（无连续性，至少6位）

连续性：密码重复（6666）或密码递增递减（4321、2468）

要求设置新密码：跳转到设置中要求设置新密码，用户也可以选择不设置

### 锁屏功能

需要Device owner（Profile owner或许也可以）

模式：

- 允许全部
- 禁用全部
- 自定义

自定义的项目：

- 禁用小工具（API21或以上弃用，Android owner的最小兼容API版本21，所以这个功能没用）
- 禁用相机
- 禁用通知（不知道是否包含音乐播放器）
- 禁用未经编辑的通知（作用未知）
- 禁用可信代理
- 禁用指纹解锁
- 禁止远程输入（弃用）（需要API24，作用未知）
- 禁用人脸解锁（需API28或以上）
- 禁用虹膜解锁（需API28或以上）（真的有人用得到吗？）
- 禁用生物识别（包括指纹面部虹膜，需API28或以上）
- 禁用快捷方式（需API34或以上）

### 密码质量要求

需要Device owner或Profile owner

API31及以上弃用，请使用[密码复杂度要求](#密码复杂度要求)

质量要求：

- 无要求（默认）
- 需要密码或图案，不管复杂度
- 至少一个字母
- 至少一个数字
- 数字字母各至少一个
- 生物识别（弱）
- 复杂数字（无连续性）
- 自定义（现在不支持，以后也不会支持，因为这已经弃用了）
