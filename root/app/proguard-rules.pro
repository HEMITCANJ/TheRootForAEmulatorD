# rootforAEmulator release rules.
# R8 minification/obfuscation is enabled; default proguard-android-optimize.txt
# rules cover Compose/Kotlin. Manifest components (.MainActivity) are kept
# automatically via AAPT-generated keep rules. Shell scripts in assets only use
# the su binary and /data/data paths, never Java class names, so no extra
# -keep rules are needed.
