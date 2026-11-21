#!/system/bin/sh
# rootforAEmulator 状态查询

echo "id=$(id -u)"
SU=$(command -v su 2>/dev/null)
echo "SUPATH=$SU"
if [ -n "$SU" ] && [ -f "$SU.rfae-real" ]; then
  echo "BROKER=1"
else
  echo "BROKER=0"
fi
echo "STATUS_DONE"
