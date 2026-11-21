#!/system/bin/sh
# rootforAEmulator 恢复脚本：还原原始 su 并移除代理备份

SU_PATH=$(command -v su 2>/dev/null)
if [ -z "$SU_PATH" ]; then
  echo "ERR=su-not-found"
  exit 40
fi
SU_PATH=$(readlink -f "$SU_PATH" 2>/dev/null || echo "$SU_PATH")
REAL="$SU_PATH.rfae-real"

if [ ! -f "$REAL" ]; then
  echo "ERR=no-backup:$SU_PATH"
  exit 41
fi

MODE=$(stat -c %a "$REAL" 2>/dev/null)

if ! touch "$(dirname "$SU_PATH")/.rfae_w" 2>/dev/null; then
  mount -o rw,remount /system 2>/dev/null
  mount -o rw,remount / 2>/dev/null
fi
rm -f "$(dirname "$SU_PATH")/.rfae_w" 2>/dev/null

cp -p "$REAL" "$SU_PATH" 2>/dev/null
if [ $? -ne 0 ]; then
  echo "ERR=restore-failed:$SU_PATH"
  exit 42
fi
[ -n "$MODE" ] && chmod "$MODE" "$SU_PATH" 2>/dev/null
restorecon "$SU_PATH" 2>/dev/null
rm -f "$REAL" 2>/dev/null

echo "SU=$SU_PATH"
if "$SU_PATH" -c id >/dev/null 2>&1; then
  echo "RESTORE_OK=1"
else
  echo "RESTORE_OK=0"
fi
echo "RESTORE_DONE"
exit 0
