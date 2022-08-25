package rnfive.htfu.cyclingcomputer.strava.runnable;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.preference.PreferenceManager;
import rn5.djs.stravalib.authentication.api.AuthenticationAPI;
import rn5.djs.stravalib.authentication.model.AuthenticationResponse;
import rn5.djs.stravalib.authentication.model.AuthenticationType;
import rn5.djs.stravalib.common.api.StravaConfig;
import rn5.djs.stravalib.common.model.Token;
import rn5.djs.stravalib.exception.StravaAPIException;
import rn5.djs.stravalib.exception.StravaUnauthorizedException;
import rnfive.htfu.cyclingcomputer.MainActivity;

public class Runnable_StravaAuth implements Runnable {

    private static final String TAG = Runnable_StravaAuth.class.getSimpleName();
    private static final int clientId = 24797;
    private static final String clientSecret = "ab7df8c0eecb22a599600530c40b6067a2b74efa";

    private final AuthenticationType type;
    private final Context context;
    private final String inString;

    public Runnable_StravaAuth(Context context, AuthenticationType type, String inString) {
        this.context = context;
        this.type = type;
        this.inString = inString;
    }

    @Override
    public void run() {
        run(context, type, inString);
    }

    public static void run(Context context, AuthenticationType type, String inValue) {
        StravaConfig config = StravaConfig.auth()
                .debug()
                .build();
        AuthenticationAPI api = new AuthenticationAPI(config);
        AuthenticationResponse result = null;

        try {
            switch (type) {
                case AUTHENTICATE:
                    result = api.getToken()
                            .forClientId(clientId)
                            .withClientSecret(clientSecret)
                            .withCode(inValue)
                            .withGrantType("authorization_code")
                            .execute();
                    break;
                case REFRESH_TOKEN:
                    result = api.refreshToken()
                            .forClientId(clientId)
                            .withClientSecret(clientSecret)
                            .withRefreshToken(inValue)
                            .withGrantType("refresh_token")
                            .execute();
                    break;
                case DEAUTHORIZE:
                    result = api.deauthorize()
                            .execute();
                    break;

            }
        } catch (StravaAPIException | StravaUnauthorizedException e) {
            Log.e(TAG, "Strava Error: " + e.getMessage());
            return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        AuthenticationResponse finalResult = result;
        handler.post(() -> handleResponse(type, finalResult, context));
    }

    private static void handleResponse(AuthenticationType type, AuthenticationResponse response, Context context) {
        if (response != null) {
            switch (type) {
                case AUTHENTICATE:
                    MainActivity.token = new Token()
                            .withTokenType(response.getTokenType())
                            .withAccessToken(response.getAccessToken())
                            .withRefreshToken(response.getRefreshToken())
                            .expiresAt(response.getExpiresAt())
                            .withUsername(response.getAthlete().getUsername())
                            .withFirstName(response.getAthlete().getFirstName())
                            .withLastName(response.getAthlete().getLastName())
                            .withPath(MainActivity.filePathProfile);
                    MainActivity.token.save();
                    MainActivity.toastListener.onToast("Logged into Strava as " + MainActivity.token.getUsername());
                    break;
                case REFRESH_TOKEN:
                    if (MainActivity.token != null) {
                        MainActivity.token.setAccessToken(response.getAccessToken());
                        MainActivity.token.setRefreshToken(response.getRefreshToken());
                        MainActivity.token.setExpirationDate(response.getExpiresAt());
                        MainActivity.token.save();
                        //MainActivity.toastListener.onToast("Logged into Strava as " + MainActivity.token.getUsername());
                    }
                    break;
            }
        } else {
            if (type == AuthenticationType.DEAUTHORIZE) {
                MainActivity.token.delete();
                MainActivity.token = null;
                MainActivity.toastListener.onToast("Logged out of Strava.");
            }
        }
        if (context != null) {
            SharedPreferences mainPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = mainPrefs.edit();
            editor.putString("KEY_STRAVA_LOGIN", (MainActivity.token != null ? MainActivity.token.getUsername() : ""));
            editor.apply();
        }
    }
}
