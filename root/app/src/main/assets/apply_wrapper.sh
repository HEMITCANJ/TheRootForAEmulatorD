#!/system/bin/sh
# rootforAEmulator 更新白名单：用新的代理脚本覆盖系统 su
# 占位符：@WRAPPER@

SRC=@WRAPPER@

SU_PATH=$(command -v su 2>/dev/null)
if [ -z "$SU_PATH" ]; then
  echo "ERR=su-not-found"
  exit 51
fi
SU_PATH=$(readlink -f "$SU_PATH" 2>/dev/null || echo "$SU_PATH")

if [ ! -f "$SU_PATH.rfae-real" ]; then
  echo "ERR=not-deployed"
  exit 52
fi
if [ ! -f "$SRC" ]; then
  echo "ERR=wrapper-missing"
  exit 53
fi

if ! touch "$(dirname "$SU_PATH")/.rfae_w" 2>/dev/null; then
  mount -o rw,remount /system 2>/dev/null
  mount -o rw,remount / 2>/dev/null
fi
rm -f "$(dirname "$SU_PATH")/.rfae_w" 2>/dev/null

cp -f "$SRC" "$SU_PATH" 2>/dev/null
if [ $? -ne 0 ]; then
  echo "ERR=write-failed:$SU_PATH"
  exit 54
fi
chmod 755 "$SU_PATH" 2>/dev/null
restorecon "$SU_PATH" 2>/dev/null

echo "SU=$SU_PATH"
echo "APPLY_OK"
exit 0
