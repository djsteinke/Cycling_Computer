package rnfive.djs.cyclingcomputer.define;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import rnfive.djs.cyclingcomputer.define.enums.ConfirmResult;
import rnfive.djs.cyclingcomputer.define.enums.ConfirmType;
import rnfive.djs.cyclingcomputer.define.listeners.ConfirmListener;
import rnfive.djs.cyclingcomputer.define.listeners.IBoolResultListener;

import rnfive.djs.cyclingcomputer.R;
import rnfive.djs.cyclingcomputer.MainActivity;

public class Dialogs {
    static String[] vals;

    private Dialogs() {}

    public static void Confirm(Activity act, final ConfirmListener confirmListener, final ConfirmType confirmType,
                               final String title, @Nullable final String desc,
                               final String yes, final String no, final String cancel) {
        AlertDialog dialog = new AlertDialog.Builder(act).create();
        dialog.setTitle(title);
        if (desc != null && !desc.equals(""))
            dialog.setMessage(Html.fromHtml(desc));
        final EditText actName = new EditText(act);
        final EditText actDesc = new EditText(act);
        final CheckBox cbTrainer = new CheckBox(act);
        final CheckBox cbCommute = new CheckBox(act);
        final CheckBox cbPrivate = new CheckBox(act);
        if (confirmType == ConfirmType.SAVE) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            LinearLayout linearLayout = new LinearLayout(act);
            linearLayout.setLayoutParams(lp);
            linearLayout.setPadding(Display.getPxFromDp(20),0, Display.getPxFromDp(20),0);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            actName.setText(MainActivity.sName);
            actName.setHint(R.string.activity_name);
            actName.setLayoutParams(lp);
            actDesc.setText(MainActivity.sDescription);
            actDesc.setHint(R.string.activity_description);
            actDesc.setLayoutParams(lp);
            linearLayout.addView(actName);
            linearLayout.addView(actDesc);
            LinearLayout.LayoutParams checkLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            LinearLayout checkLL = new LinearLayout(act);
            checkLL.setLayoutParams(checkLP);
            checkLL.setOrientation(LinearLayout.HORIZONTAL);
            cbTrainer.setChecked(false);
            cbTrainer.setText(act.getString(R.string.trainer));
            cbTrainer.setPadding(0,0, Display.getPxFromDp(5),0);
            checkLL.addView(cbTrainer);
            cbCommute.setChecked(false);
            cbCommute.setText(act.getString(R.string.commute));
            cbCommute.setPadding(0,0, Display.getPxFromDp(5),0);
            checkLL.addView(cbCommute);
            cbPrivate.setChecked(false);
            cbPrivate.setText(act.getString(R.string.private_s));
            //checkLL.addView(cbPrivate);
            linearLayout.addView(checkLL);
            dialog.setView(linearLayout);

        }
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int buttonId) {
                        if (confirmType == ConfirmType.SAVE) {
                            vals = new String[2];
                            vals[0] = actName.getText().toString();
                            vals[1] = actDesc.getText().toString();
                            MainActivity.bTrainer = cbTrainer.isChecked();
                            MainActivity.bCommute = cbCommute.isChecked();
                            MainActivity.bPrivate = cbPrivate.isChecked();
                        }
                        confirmListener.onConfirm(confirmType, ConfirmResult.POSITIVE, vals);
                        vals = null;
                    }
                });
        switch (confirmType) {
            case SENSOR:
            case PERMISSIONS:
                break;
            default :
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, no,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int buttonId) {
                                confirmListener.onConfirm(confirmType, ConfirmResult.NEGATIVE, null);
                            }
                        });
                break;
        }
        switch (confirmType) {
            case SENSOR:
            case EXIT:
            case SAVE:
                dialog.setButton(DialogInterface.BUTTON_NEUTRAL, cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int buttonId) {
                                confirmListener.onConfirm(confirmType, ConfirmResult.NEUTRAL, null);
                            }
                        });
                break;
            default:
                break;
        }
        dialog.show();
    }

    public static void dialogYesNo(final Context context, final IBoolResultListener listener, String object) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage(object)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Feedback.tick(context);
                        listener.onResult(false, null);
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Feedback.tick(context);
                        listener.onResult(true, null);
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void dialogEnterTextYesNo(final Context context, final IBoolResultListener listener,
                                            String message, String title, String text) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        ViewGroup viewGroup = ((Activity) context).findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_enter_text, viewGroup, false);

        TextView tv = dialogView.findViewById(R.id.text_title);
        title = title + ": ";
        tv.setText(title);
        final EditText et = dialogView.findViewById(R.id.edit_text);
        et.setText(text);

        builder.setMessage(message)
                .setView(dialogView)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Feedback.tick(context);
                        listener.onResult(false, null);
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Feedback.tick(context);
                        listener.onResult(true,et.getText().toString());
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
