# Mantém os métodos chamados pelo JavaScript (window.FolegoAndroid)
-keepclassmembers class com.folego.financas.MainActivity$Bridge {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes JavascriptInterface
