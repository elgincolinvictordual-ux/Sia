# --- Required: the WebView JS bridge calls these methods by name. If they
# get renamed or stripped, the grades feature silently breaks.
-keepclassmembers class com.theunknown.siasgrades.MainActivity$* {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes JavascriptInterface

# --- Hardening: makes the decompiled output harder to read and the
# package/class layout less informative. None of this is reflected on or
# depended on at runtime, so it's safe to apply broadly.
-repackageclasses ''
-allowaccessmodification
-overloadaggressively
-flattenpackagehierarchy ''

# --- Keeps line numbers so the in-app debug screen and any crash report
# are actually useful for finding the bug. The real source file name is
# still replaced with a generic placeholder, so this doesn't give that
# back away - but class and method names stay obfuscated from the rules
# above, so a raw trace will show scrambled names with real line numbers.
# Match a trace to real names using the mapping.txt that this exact build
# produces (see app/build/outputs/mapping/release/mapping.txt in the
# GitHub Actions build artifacts) - save it for each release you hand out.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
