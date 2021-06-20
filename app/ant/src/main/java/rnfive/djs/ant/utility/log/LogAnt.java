package rnfive.djs.ant.utility.log;

import android.content.Context;
import android.util.Log;

public class LogAnt {
    public static final int DEBUG_LEVEL = 0;
    public static final int DEBUG_LEVEL_DEFAULT;
    private static int sDebugLevel;
    private static final String TAG;
    protected static final String PREFS_FILE = "ant_plugins_logging";
    protected static final String PREFS_DEBUG_LEVEL = "debug_level";
    private static String sVersion;

    public LogAnt() {
    }

    public static void setDebugLevel(int level, Context context) {
        Log.w(TAG, "setDebugLevel is disabled as of API level 24, request special debug pluginLib to get more detailed logs");
    }

    public static void setDebugLevel(LogAnt.DebugLevel level, Context context) {
        int levelInt = level.ordinal();
        setDebugLevel(levelInt, context);
    }

    public static int getDebugLevel(Context context) {
        return sDebugLevel;
    }

    public static int getDebugLevel() {
        return sDebugLevel;
    }

    public static void setVersion(String version) {
        sVersion = version + ": ";
    }

    public static final void e(String tag, String msg) {
        if (sDebugLevel >= LogAnt.DebugLevel.ERROR.ordinal()) {
            Log.e(tag, sVersion + msg);
        }

    }

    public static final void e(String tag, String msg, Throwable th) {
        if (sDebugLevel >= LogAnt.DebugLevel.ERROR.ordinal()) {
            Log.e(tag, sVersion + msg, th);
        }

    }

    public static final void w(String tag, String msg) {
        if (sDebugLevel >= LogAnt.DebugLevel.WARNING.ordinal()) {
            Log.w(tag, sVersion + msg);
        }

    }

    public static final void w(String tag, String msg, Throwable th) {
        if (sDebugLevel >= LogAnt.DebugLevel.WARNING.ordinal()) {
            Log.w(tag, sVersion + msg, th);
        }

    }

    public static final void i(String tag, String msg) {
        if (sDebugLevel >= LogAnt.DebugLevel.INFO.ordinal()) {
            Log.i(tag, sVersion + msg);
        }

    }

    public static final void d(String tag, String msg) {
        if (sDebugLevel >= LogAnt.DebugLevel.DEBUG.ordinal()) {
            Log.d(tag, sVersion + msg);
        }

    }

    public static final void v(String tag, String msg) {
        if (sDebugLevel >= LogAnt.DebugLevel.VERBOSE.ordinal()) {
            Log.v(tag, sVersion + msg);
        }

    }

    static {
        DEBUG_LEVEL_DEFAULT = LogAnt.DebugLevel.WARNING.ordinal();
        sDebugLevel = DEBUG_LEVEL_DEFAULT;
        TAG = LogAnt.class.getSimpleName();
        sVersion = "v.NTST: ";
    }

    public static enum DebugLevel {
        NONE,
        ERROR,
        WARNING,
        INFO,
        DEBUG,
        VERBOSE;

        private DebugLevel() {
        }
    }
}
