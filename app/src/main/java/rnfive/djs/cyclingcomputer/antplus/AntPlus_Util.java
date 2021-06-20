package rnfive.djs.cyclingcomputer.antplus;


import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;

import static rnfive.djs.cyclingcomputer.define.StaticVariables.bDebug;

public class AntPlus_Util {
    AntPlus_Util() {}

    public static String getResultCode(RequestAccessResult resultCode, String device) {

        String toastMsg;
        switch (resultCode) {
            case SUCCESS:
                toastMsg = device + " Connected";
                break;
            case CHANNEL_NOT_AVAILABLE:
                toastMsg = (bDebug?device + " : Channel Not Available":"");
                break;
            case ADAPTER_NOT_DETECTED:
                toastMsg = "ANT Adapter not Available. Built-in ANT hardware or external adapter required.";
                break;
            case BAD_PARAMS:
                toastMsg = device + " : Bad request parameters.";
                break;
            case OTHER_FAILURE:
                toastMsg = device + " : RequestAccess failed. See logcat for details.";
                break;
            case DEPENDENCY_NOT_INSTALLED:
                toastMsg = "ANT Dependency not installed.";
                /*
                Intent startStore = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id="
                                + AntPlusHeartRatePcc.getMissingDependencyPackageName()));
                startStore.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                getStaticContext().startActivity(startStore);
                */
                /*
                AlertDialog.Builder adlgBldr = new AlertDialog.Builder(getStaticContext());
                adlgBldr.setTitle("Missing Dependency");
                adlgBldr.setMessage("The required service\n\""
                        + AntPlusHeartRatePcc.getMissingDependencyName()
                        + "\"\n was not found. You need to install the ANT+ Plugins service or you may need to updatePreference your existing version if you already have it. Do you want to launch the Play Store to get it?");
                adlgBldr.setCancelable(true);
                adlgBldr.setPositiveButton("Go to Store", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent startStore = new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("market://details?id="
                                        + AntPlusHeartRatePcc.getMissingDependencyPackageName()));
                        startStore.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        getStaticContext().startActivity(startStore);
                    }
                });
                adlgBldr.setNegativeButton("Cancel", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                final AlertDialog waitDialog = adlgBldr.create();
                waitDialog.show();
                */
                break;
            case USER_CANCELLED:
                toastMsg = device + " : Search cancelled.";
                break;
            case UNRECOGNIZED:
                toastMsg = device + " : Failed: UNRECOGNIZED. PluginLib Upgrade Required?";
                break;
            default:
                toastMsg = device + " : Unrecognized result: " + resultCode.toString();
                break;
        }
        return toastMsg;
    }
}
