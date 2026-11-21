#!/system/bin/sh
# rootforAEmulator su 代理（由应用写入系统 su 路径）
# 放行规则：uid 0/1000/2000，或 uid 在下方 ALLOW_UIDS 中

SELF="$0"
case "$SELF" in
  */*) ;;
  *) SELF=$(command -v "$SELF" 2>/dev/null) ;;
esac

REAL="$SELF.rfae-real"
BASE=/data/local/rfae
LOG="$BASE/access.log"
ALLOW_UIDS="@UIDS@"

if [ ! -x "$REAL" ]; then
  echo "rootforAEmulator: 未找到原始 su（$REAL），请在模拟器 Root 终端中恢复" >&2
  exit 126
fi

CALLER_UID=$(awk '/^Uid:/{print $2; exit}' /proc/$PPID/status 2>/dev/null)
[ -z "$CALLER_UID" ] && CALLER_UID=$(stat -c %u /proc/$PPID 2>/dev/null)
[ -z "$CALLER_UID" ] && CALLER_UID=99999
CALLER_CMD=$(tr '\0' ' ' < /proc/$PPID/cmdline 2>/dev/null | cut -c1-160)
NOW=$(date '+%Y-%m-%d %H:%M:%S')

GRANT=0
case "$CALLER_UID" in
  0|1000|2000) GRANT=1 ;;
esac
if [ "$GRANT" = 0 ]; then
  case " $ALLOW_UIDS " in
    *" $CALLER_UID "*) GRANT=1 ;;
  esac
fi

if [ "$GRANT" = 0 ]; then
  echo "rootforAEmulator: 已拒绝 uid=$CALLER_UID ($CALLER_CMD) 的 root 请求" >&2
  echo "$NOW DENY  uid=$CALLER_UID cmd=$CALLER_CMD" >> "$LOG" 2>/dev/null
  exit 1
fi

echo "$NOW ALLOW uid=$CALLER_UID cmd=$CALLER_CMD" >> "$LOG" 2>/dev/null
exec "$REAL" "$@"
