# Android Owner

### 简介

使用安卓的Device Admin和Device Owner，全方位掌控你的设备。
这个应用使用Kotlin + Jetpack Compose，有不完全的质感设计3配色

记得给我个小星星，欢迎大家提交Issue

### 功能

已经适配手表，请在应用的设置里打开“Wear”
适配了一些预见式返回动画（需安卓13或14），可能不太稳定，如果有问题请向我反馈

- 设备控制
  - 禁用相机
  - 禁止截屏
  - 全局静音
  - 关闭USB信号（需设备支持）
  - 管理系统更新策略
  - 清除数据
- 管理应用
  - 隐藏应用
  - 停用应用
  - 禁止卸载应用
  - 设置许可的输入法
- 用户限制
  - 网络和互联网：禁止使用流量、WiFi、VPN、私人DNS
  - 其他连接：禁止使用蓝牙、位置信息、NFC、USB(MTP)
  - 应用：禁止安装卸载应用、禁止清除应用的存储空间
  - 显示与音量：禁止调整亮度、音量
  - 用户和工作资料：禁止添加/切换/移除用户，禁止添加/移除工作资料
  - 杂项：禁止自动填充服务、禁止调试
- 用户管理
  - 添加、启动、切换、停止、移除用户
  - 修改当前用户的名称
  - 设置切换用户时的提示
- 密码
  - 修改密码
  - 最大密码错误次数
  - 密码失效超时时间
  - 设置密码复杂度要求

### 这个应用十分危险！！！

在使用各个功能之前，请仔细阅读相应的说明。红色的按钮一定要谨慎使用！
如果操作不慎，可能会意外地丢失数据或者让你无法解锁你的设备！

### 即将加入的功能

- Managed Profile，工作资料和多用户相关
- 应用管理--安装/卸载应用
- 应用管理--包选择器（目前只能手动输入包名）
- 用户管理--用户选择器（目前只能手动输入序列号）

### 许可证

> Copyright (C)  2024  BinTianqi
>
> This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
>
> This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
>
> You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
