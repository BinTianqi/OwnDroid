[English](Readme-en.md) | [简体中文](Readme.md)

# OwnDroid

AndroidのDevice owner特権を使用してデバイスを管理します。

## ダウンロード

- [IzzyOnDroid F-Droid Repository](https://apt.izzysoft.de/fdroid/index/apk/com.bintianqi.owndroid)
- [Releases on GitHub](https://github.com/BinTianqi/OwnDroid/releases)

> [!NOTE]
> ColorOSユーザーはGitHubのリリースからtestkeyバージョンをダウンロードしてください

## 機能

- システム
  - オプション：カメラの無効化、スクリーンショットの無効化、マスターボリュームのミュート、USB信号の無効化...
  - 権限ポリシー
  - CA証明書の管理
  - _データの消去_
  - ...
- ネットワーク
  - Wi-Fiの追加/変更/削除
  - ネットワーク統計
  - 最小Wi-Fiセキュリティレベル
  - 常時オンVPN
  - ネットワークログ
  - ...
- ワークプロファイル
  - ワークプロファイルの作成
  - 個人アプリの一時停止
  - ...
- アプリケーション
  - アプリの一時停止/非表示
  - アンインストールのブロック
  - アプリのインストール/アンインストール
  - ...
- ユーザー制限
  - ネットワーク：モバイルネットワークの設定禁止、Wi-Fiの設定禁止、SMSの無効化、発信通話の禁止...
  - 接続：Bluetoothの無効化、位置情報の設定禁止、USBファイル転送の無効化、印刷の無効化...
  - アプリケーション：アプリのインストール/アンインストールの禁止...
  - ユーザー：ユーザーの追加/削除/切り替えの禁止...
  - メディア：明るさの調整禁止、音量の調整禁止...
  - その他：アカウントの変更禁止、言語の設定禁止、工場出荷時設定へのリセット禁止、デバッグ機能の無効化...
- ユーザー管理
  - ユーザー情報
  - ユーザーの起動/切り替え/停止/削除
  - ユーザーの作成
  - ...
- パスワードとキーロック
  - _パスワードのリセット_
  - パスワードの複雑さの要求
  - スクリーンタイムアウトの設定
  - ...

## アクティベート

- Shizuku (推奨)
- adbシェルでコマンドを実行: `dpm set-device-owner com.bintianqi.owndroid/.Receiver`

## FAQ

### アクティベート

#### デバイスに既にアカウントが存在する場合

```text
java.lang.IllegalStateException: Not allowed to set the device owner because there are already some accounts on the device
```

解決策：
- これらのアカウントを保持しているアプリを凍結します。
- これらのアカウントを削除します。
- LSPosedモジュール [HookDPM](https://github.com/BinTianqi/HookDPM) を使用します。

#### デバイスに既に複数のユーザーが存在する場合

```text
java.lang.IllegalStateException: Not allowed to set the device owner because there are already several users on the device
```

解決策：
- セカンダリユーザーを削除します。
- LSPosedモジュール [HookDPM](https://github.com/BinTianqi/HookDPM) を使用します。

> [!NOTE]
> 一部のシステムにはアプリのクローンや子供用スペースなどの機能があり、通常はユーザーとして扱われます。

#### MIUI

```text
java.lang.SecurityException: Neither user 2000 nor current process has android.permission.MANAGE_DEVICE_ADMINS.
```

解決策：
- 開発者オプションで `USBデバッグ（セキュリティ設定）` を有効にします。
- ルートシェルでアクティベートコマンドを実行します。

#### ColorOS

```text
java.lang.IllegalStateException: Unexpected @ProvisioningPreCondition
```

解決策：OwnDroid testkeyバージョンを使用します

## API

| ID                     | Extras        | 最小Androidバージョン |
|------------------------|---------------|:---------------------:|
| HIDE                   | `package`     |                       |
| UNHIDE                 | `package`     |                       |
| SUSPEND                | `package`     |           7           |
| UNSUSPEND              | `package`     |           7           |
| ADD_USER_RESTRICTION   | `restriction` |                       |
| CLEAR_USER_RESTRICTION | `restriction` |                       |
| LOCK                   |               |                       |

[利用可能なユーザー制限](https://developer.android.com/reference/android/os/UserManager#constants_1)

```shell
# ADBシェルでアプリを非表示にする例
am broadcast -a com.bintianqi.owndroid.action.HIDE -n com.bintianqi.owndroid/.ApiReceiver --es key abcdefg --es package com.example.app
```

```kotlin
// Kotlinでアプリを非表示にする例
val intent = Intent("com.bintianqi.owndroid.action.HIDE")
    .setComponent(ComponentName("com.bintianqi.owndroid", "com.bintianqi.owndroid.ApiReceiver"))
    .putExtra("key", "abcdefg")
    .putExtra("package", "com.example.app")
context.sendBroadcast(intent)
```

## ビルド

コマンドラインでGradleを使用してOwnDroidをビルドできます。
```shell
# testkeyで署名（デフォルト）
./gradlew build
# カスタム.jksキーで署名
./gradlew build -PStoreFile="/path/to/your/jks/file" -PStorePassword="YOUR_KEYSTORE_PASSWORD" -PKeyPassword="YOUR_KEY_PASSWORD" -PKeyAlias="YOUR_KEY_ALIAS"
```
（Windowsでは `./gradlew.bat` を使用してください）

## ライセンス

[License.md](LICENSE.md)

> Copyright (C)  2024  BinTianqi
>
> This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
>
> This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
>
> You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
