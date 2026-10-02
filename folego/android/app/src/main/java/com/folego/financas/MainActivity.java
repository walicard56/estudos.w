package com.folego.financas;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.webkit.WebViewAssetLoader;

import org.json.JSONObject;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Tela única do Fôlego: um WebView que carrega o app web embutido em assets/.
 *
 * Os arquivos são servidos por https://appassets.androidplatform.net (WebViewAssetLoader),
 * o que dá ao app uma origem fixa e segura — o localStorage, onde ficam os dados,
 * persiste entre aberturas e atualizações. Nada é carregado da internet.
 */
public class MainActivity extends ComponentActivity {

    private static final String HOST = "appassets.androidplatform.net";
    private static final String START_URL = "https://" + HOST + "/assets/index.html";

    private WebView webView;
    private FrameLayout root;

    private ValueCallback<Uri[]> pendingFileCallback;
    private String pendingBackupJson;

    // "Restaurar backup": seletor de arquivos do sistema
    private final ActivityResultLauncher<String> pickFile =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (pendingFileCallback != null) {
                    pendingFileCallback.onReceiveValue(uri == null ? null : new Uri[]{uri});
                    pendingFileCallback = null;
                }
            });

    // "Salvar backup": o usuário escolhe onde gravar (Downloads, Drive...)
    private final ActivityResultLauncher<String> createBackup =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("application/json"), uri -> {
                String json = pendingBackupJson;
                pendingBackupJson = null;
                if (uri == null || json == null) return;
                boolean ok = false;
                try (OutputStream out = getContentResolver().openOutputStream(uri, "wt")) {
                    if (out != null) {
                        out.write(json.getBytes(StandardCharsets.UTF_8));
                        ok = true;
                    }
                } catch (Exception ignored) {
                }
                webView.evaluateJavascript("window.folegoBackupSaved&&window.folegoBackupSaved(" + ok + ")", null);
            });

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);

        root = new FrameLayout(this);
        webView = new WebView(this);
        root.addView(webView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(root);

        int bg = getColor(R.color.bg);
        root.setBackgroundColor(bg);
        webView.setBackgroundColor(bg);

        // Mantém o conteúdo fora da barra de status, da barra de navegação e do teclado.
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout()
                    | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setMediaPlaybackRequiresUserGesture(true);
        s.setTextZoom(100); // o layout já é responsivo; evita quebrar com fonte gigante do sistema

        WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
                .setDomain(HOST)
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return loader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri url = request.getUrl();
                if (HOST.equals(url.getHost())) return false;
                // Qualquer link externo abre no navegador, nunca dentro do app.
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, url));
                } catch (Exception ignored) {
                }
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (pendingFileCallback != null) pendingFileCallback.onReceiveValue(null);
                pendingFileCallback = callback;
                try {
                    // "*/*": alguns gerenciadores não reconhecem .json como application/json
                    pickFile.launch("*/*");
                } catch (Exception e) {
                    pendingFileCallback = null;
                    return false;
                }
                return true;
            }
        });

        webView.addJavascriptInterface(new Bridge(), "FolegoAndroid");

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack(); // ex.: voltando da política de privacidade
                    return;
                }
                webView.evaluateJavascript("!!(window.folegoBack&&window.folegoBack())", handled -> {
                    if (!"true".equals(handled)) {
                        setEnabled(false);
                        getOnBackPressedDispatcher().onBackPressed();
                        setEnabled(true);
                    }
                });
            }
        });

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        }
        if (webView.getUrl() == null) {
            webView.loadUrl(START_URL);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onDestroy() {
        if (pendingFileCallback != null) {
            pendingFileCallback.onReceiveValue(null);
            pendingFileCallback = null;
        }
        root.removeView(webView);
        webView.destroy();
        super.onDestroy();
    }

    /** Métodos que o JavaScript chama via window.FolegoAndroid. */
    final class Bridge {

        /** Pinta o fundo atrás das barras do sistema e ajusta a cor dos ícones delas. */
        @JavascriptInterface
        public void setSystemBars(String color, boolean dark) {
            runOnUiThread(() -> {
                try {
                    int c = Color.parseColor(color);
                    root.setBackgroundColor(c);
                    webView.setBackgroundColor(c);
                } catch (IllegalArgumentException ignored) {
                }
                View decor = getWindow().getDecorView();
                var ctl = WindowCompat.getInsetsController(getWindow(), decor);
                ctl.setAppearanceLightStatusBars(!dark);
                ctl.setAppearanceLightNavigationBars(!dark);
            });
        }

        /** Abre o "Salvar como" do sistema com o JSON do backup. */
        @JavascriptInterface
        public void saveBackup(String json, String fileName) {
            // valida antes de abrir o seletor (evita gravar lixo)
            try {
                new JSONObject(json);
            } catch (Exception e) {
                return;
            }
            runOnUiThread(() -> {
                pendingBackupJson = json;
                try {
                    createBackup.launch(fileName);
                } catch (Exception e) {
                    pendingBackupJson = null;
                    Toast.makeText(MainActivity.this, "Não foi possível abrir o seletor de arquivos", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
