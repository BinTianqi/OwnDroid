#!/system/bin/sh
BASEDIR=$(dirname "$0")
DEX="$BASEDIR"/rish_shizuku.dex
[ -z "$RISH_APPLICATION_ID" ] && export RISH_APPLICATION_ID="com.bintianqi.owndroid"
/system/bin/app_process -Djava.class.path="$DEX" /system/bin --nice-name=rish rikka.shizuku.shell.ShizukuShellLoader "$@"
