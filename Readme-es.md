[English](Readme.md) | [简体中文](Readme-zh_CN.md) | [繁體中文](Readme-zh_TW.md) | [日本語](Readme-ja.md)

# OwnDroid

Usa la API DevicePolicyManager de Android para gestionar tu dispositivo.

## Descarga

- [Repositorio F-Droid de IzzyOnDroid](https://apt.izzysoft.de/fdroid/index/apk/com.bintianqi.owndroid)
- [Versiones publicadas en GitHub](https://github.com/BinTianqi/OwnDroid/releases)

> [!NOTE]
> Si usas ColorOS, descarga la versión testkey desde las versiones publicadas en GitHub

## Funciones

- Sistema: desactivar la cámara, desactivar las capturas de pantalla, silenciar el volumen general, desactivar la señal USB, modo de bloqueo de tareas, borrar datos...
- Red: añadir/modificar/eliminar redes Wi-Fi, estadísticas de red, registro de red...
- Aplicaciones: suspender/ocultar aplicaciones, bloquear la desinstalación de aplicaciones, conceder/revocar permisos, borrar el almacenamiento de aplicaciones, instalar/desinstalar aplicaciones...
- Restricciones de usuario: desactivar los SMS, desactivar las llamadas salientes, desactivar el Bluetooth, desactivar el NFC, desactivar la transferencia de archivos por USB, desactivar la instalación/desinstalación de aplicaciones...
- Usuarios: información del usuario, crear/iniciar/cambiar/detener/eliminar usuarios...
- Contraseña y pantalla de bloqueo: restablecer la contraseña, configurar el tiempo de espera de la pantalla...

## Modos de funcionamiento

- Propietario del dispositivo (recomendado)

  Métodos de activación:
  - Shizuku
  - Dhizuku
  - Root
  - Comando de ADB shell `dpm set-device-owner com.bintianqi.owndroid/.Receiver`
- [Dhizuku](https://github.com/iamr0s/Dhizuku)
- Perfil de trabajo

## Preguntas frecuentes

### Ya hay cuentas en el dispositivo

```text
java.lang.IllegalStateException: Not allowed to set the device owner because there are already some accounts on the device
```

Desde la v8.4, OwnDroid intenta saltarse esta restricción automáticamente al activar el propietario del dispositivo con Shizuku o root.

Si sigues viendo este error, puedes probar a congelar las aplicaciones a las que pertenecen las cuentas o a eliminar esas cuentas.

### Ya hay varios usuarios en el dispositivo

```text
java.lang.IllegalStateException: Not allowed to set the device owner because there are already several users on the device
```

Solución: elimina los usuarios secundarios, incluidos el perfil de trabajo, el espacio privado y la clonación de aplicaciones.

### Ya hay un propietario del dispositivo

```text
java.lang.IllegalStateException: Trying to set the device owner (com.bintianqi.owndroid/.Receiver), but device owner (xxx) is already set.
```

En un dispositivo solo puede haber un propietario del dispositivo. Desactiva primero el propietario del dispositivo actual.

### MIUI y HyperOS

```text
java.lang.SecurityException: Neither user 2000 nor current process has android.permission.MANAGE_DEVICE_ADMINS.
```

Soluciones:
- Activa `USB debugging (Security setting)` (Depuración USB (ajustes de seguridad)) en las opciones para desarrolladores.
- O ejecuta el comando de activación en una shell con root.

### ColorOS

```text
java.lang.IllegalStateException: Unexpected @ProvisioningPreCondition
```

Solución: usa la versión testkey de OwnDroid.

Las versiones testkey y firmada solo se diferencian en la firma. No hay ninguna diferencia de funcionamiento entre ellas.

### Samsung

```text
user limit reached
```

Samsung limita la función de varios usuarios de Android. De momento no hay solución.

### Crear un perfil de trabajo o un usuario

En la mayoría de los dispositivos, el sistema no permite crear un perfil de trabajo cuando hay un propietario del dispositivo,
porque el sistema añade la restricción de usuario `no_add_managed_profile` al establecer un propietario del dispositivo.
El propietario del dispositivo no puede modificar las restricciones de usuario que establece el sistema, pero si tu dispositivo tiene root, puedes desactivar esta restricción ejecutando los siguientes comandos en adb shell.
Nota: el propietario del dispositivo y el propietario del perfil de trabajo no pueden ser la misma aplicación; si lo son, el propietario del dispositivo perderá sus privilegios al reiniciar.

```shell
pm set-user-restriction no_add_user 0
pm set-user-restriction no_add_managed_profile 0
pm set-user-restriction no_add_private_profile 0
pm set-user-restriction no_add_clone_profile 0
```

Saltarse estas restricciones es responsabilidad tuya. Puede provocar comportamientos inesperados; por ejemplo, el sistema puede eliminar sin avisar el usuario que hayas creado al reiniciar.

Algunos sistemas desactivan la opción de añadir usuarios en los ajustes de Android en cuanto se establece un propietario del dispositivo.
En ese caso tienes que crear los usuarios en OwnDroid. O, si tienes root, ejecuta el comando anterior en adb shell para quitar esa restricción.

## Para usuarios avanzados

### API

OwnDroid ofrece una API basada en Intents. Tienes que configurar la clave de la API en los ajustes y activar la API. Los números entre paréntesis indican la versión mínima de Android necesaria.

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

[Restricciones de usuario disponibles](https://developer.android.com/reference/android/os/UserManager#constants_1)

## Compilación

Puedes compilar OwnDroid con Gradle desde la línea de comandos.

```shell
# Use testkey for signing (default)
./gradlew build
# Use your custom .jks key for signing
./gradlew build -PStoreFile="/path/to/your/jks/file" -PStorePassword="YOUR_KEYSTORE_PASSWORD" -PKeyPassword="YOUR_KEY_PASSWORD" -PKeyAlias="YOUR_KEY_ALIAS"
```

(En Windows, usa `./gradlew.bat`)

## Contribuir

¡Las contribuciones son bienvenidas!

1. Haz un [fork](https://github.com/BinTianqi/OwnDroid/fork) de este repositorio (desmarca «Copy the master branch only», que viene marcado por defecto)
2. Clona tu propio repositorio con git (usa la rama `dev`)
3. Haz los cambios
4. Ejecuta la aplicación en tu dispositivo o en un emulador para probar los cambios (obligatorio si contribuyes con código, opcional si contribuyes con traducciones)
5. Haz commit de los cambios y súbelos (push)
6. Abre una pull request en este repositorio

## Licencia

[License.md](LICENSE.md)

> Copyright (C)  2026  BinTianqi
>
> This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
>
> This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
>
> You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
