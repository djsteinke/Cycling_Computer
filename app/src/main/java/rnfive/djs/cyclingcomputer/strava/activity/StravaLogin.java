package rnfive.djs.cyclingcomputer.strava.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import rnfive.djs.cyclingcomputer.R;
import rnfive.djs.cyclingcomputer.strava.runnable.Runnable_StravaAuth;
import rn5.djs.stravalib.authentication.model.AuthenticationType;
import rnfive.djs.cyclingcomputer.MainActivity;

public class StravaLogin extends AppCompatActivity {

    private WebView webView;
    private static final String redirectUri = "http://localhost";
    private Context context;

    protected  void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_strava_login);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setTitle(getString(R.string.strava_auth));

        context = this;
        webView = findViewById(R.id.login_webview);
        webView.getSettings().setJavaScriptEnabled(true);
        setWebViewClient();

        Uri uri = Uri.parse("https://www.strava.com/oauth/mobile/authorize")
                .buildUpon()
                .appendQueryParameter("client_id", "24797")
                .appendQueryParameter("redirect_uri", redirectUri)
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("approval_prompt", "auto")
                .appendQueryParameter("scope", "activity:write,activity:read_all")
                .build();

        webView.loadUrl(uri.toString());
    }

    private void setWebViewClient() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrl(Uri.parse(url)) || super.shouldOverrideUrlLoading(view, url);
            }

            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                final Uri uri = request.getUrl();
                return handleUrl(uri) || super.shouldOverrideUrlLoading(view, request);
            }

            private boolean handleUrl(Uri uri) {
                if (uri.toString().startsWith(redirectUri)) {
                    String code = uri.getQueryParameter("code");
                    String error = uri.getQueryParameter("error");
                    if (error != null && !error.isEmpty())
                        MainActivity.toastListener.onToast("Strava Authorization failed. Reason[" + error + "]");
                    return makeResult(code);
                }
                return false;
            }

            private boolean makeResult(String code) {
                if (code != null && !code.isEmpty()) {
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    executor.execute(new Runnable_StravaAuth(context, AuthenticationType.AUTHENTICATE, code));

                    Intent result = new Intent();
                    result.putExtra(MainActivity.stravaCode, code);
                    setResult(RESULT_OK, result);
                    finish();
                    return true;
                }
                finish();
                return false;
            }

        });
    }


}
