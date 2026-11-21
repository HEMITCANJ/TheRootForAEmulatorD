#!/system/bin/sh
# rootforAEmulator 部署脚本（由应用通过 root shell 执行）
# 占位符：@BASE@ 数据目录，@WRAPPER@ 应用缓存中的代理脚本路径

BASE=@BASE@
SRC=@WRAPPER@

mkdir -p "$BASE" 2>/dev/null
chmod 755 "$BASE" 2>/dev/null
touch "$BASE/access.log" 2>/dev/null
chmod 666 "$BASE/access.log" 2>/dev/null

SU_PATH=$(command -v su 2>/dev/null)
if [ -z "$SU_PATH" ]; then
  echo "ERR=su-not-found"
  exit 21
fi
SU_PATH=$(readlink -f "$SU_PATH" 2>/dev/null || echo "$SU_PATH")

case "$SU_PATH" in
  *magisk*|/sbin/*|/debug_ramdisk/*)
    echo "ERR=daemon-su-unsupported:$SU_PATH"
    exit 22
    ;;
esac

REAL="$SU_PATH.rfae-real"
SU_DIR=$(dirname "$SU_PATH")
CTX=$(ls -Z "$SU_PATH" 2>/dev/null | awk '{print $1}')

# 先尝试让系统分区可写
if ! touch "$SU_DIR/.rfae_w" 2>/dev/null; then
  mount -o rw,remount /system 2>/dev/null
  mount -o rw,remount / 2>/dev/null
fi
if ! touch "$SU_DIR/.rfae_w" 2>/dev/null; then
  echo "ERR=system-read-only:$SU_DIR"
  exit 24
fi
rm -f "$SU_DIR/.rfae_w" 2>/dev/null

# 备份原始 su（保留 setuid 位等权限）
if [ ! -f "$REAL" ]; then
  cp -p "$SU_PATH" "$REAL" 2>/dev/null
  if [ ! -f "$REAL" ]; then
    echo "ERR=backup-failed:$SU_PATH"
    exit 23
  fi
fi
MODE=$(stat -c %a "$REAL" 2>/dev/null)
[ -n "$MODE" ] && chmod "$MODE" "$REAL" 2>/dev/null
[ -n "$CTX" ] && chcon "$CTX" "$REAL" 2>/dev/null

if [ ! -f "$SRC" ]; then
  echo "ERR=wrapper-missing:$SRC"
  exit 25
fi

cp -f "$SRC" "$SU_PATH" 2>/dev/null
if [ $? -ne 0 ]; then
  echo "ERR=write-failed:$SU_PATH"
  exit 26
fi
chmod 755 "$SU_PATH" 2>/dev/null
[ -n "$CTX" ] && chcon "$CTX" "$SU_PATH" 2>/dev/null
restorecon "$SU_PATH" 2>/dev/null

if "$REAL" -c id >/dev/null 2>&1; then
  echo "REAL_OK=1"
else
  echo "REAL_OK=0"
fi

echo "SU=$SU_PATH"
echo "REAL=$REAL"
echo "MODE=$MODE"
echo "DEPLOY_OK"
exit 0
