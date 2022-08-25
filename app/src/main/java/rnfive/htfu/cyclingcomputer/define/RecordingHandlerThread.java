package rnfive.htfu.cyclingcomputer.define;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

public class RecordingHandlerThread extends HandlerThread {
    Handler handler;

    public RecordingHandlerThread(String name) {
        super(name);
    }

    @Override
    protected void onLooperPrepared() {
        handler = new Handler(getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                // process incoming messages here
                // this will run in non-ui/background thread
            }
        };
    }
}
