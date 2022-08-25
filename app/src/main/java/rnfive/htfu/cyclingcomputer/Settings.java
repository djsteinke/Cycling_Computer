package rnfive.htfu.cyclingcomputer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import rnfive.htfu.cyclingcomputer.R;
import rnfive.htfu.cyclingcomputer.strava.runnable.Runnable_StravaAuth;
import rnfive.htfu.cyclingcomputer.utils.MenuUtil;
import rn5.djs.stravalib.authentication.model.AuthenticationType;

import static rnfive.htfu.cyclingcomputer.MainActivity.toastListener;

public class Settings extends AppCompatActivity {

    public static final String stravaRedirectUri = "app://localhost/gps_cycling_computer";
    private static final String TAG = "Settings";

    protected  void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportFragmentManager().beginTransaction().replace(R.id.settings_content, new Fragment_Preferences(this)).commit();
    }

    public void authorize_strava(String code) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable_StravaAuth(this, AuthenticationType.AUTHENTICATE, code));
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null && data.toString().startsWith(stravaRedirectUri)) {
            String code = data.getQueryParameter("code");
            String error = data.getQueryParameter("error");
            if (error != null && !error.isEmpty())
                toastListener.onToast("Strava Authorization failed. Reason[" + error + "]");
            if (code != null)
                authorize_strava(code);
        }
        Log.d(TAG, "URI[" + data + "]");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        boolean result;
        result = MenuUtil.menuItemSelector(this,item,TAG);
        return result || super.onOptionsItemSelected(item);
    }
}
