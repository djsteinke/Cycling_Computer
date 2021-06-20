package rnfive.djs.cyclingcomputer;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import lombok.Getter;
import lombok.Setter;
import rnfive.djs.cyclingcomputer.define.DataFields;
import rnfive.djs.cyclingcomputer.define.Display;
import rnfive.djs.cyclingcomputer.define.FieldDef;
import rnfive.djs.cyclingcomputer.define.enums.DataFieldName;

import static rnfive.djs.cyclingcomputer.MainActivity.activityDataFields;
import static rnfive.djs.cyclingcomputer.MainActivity.preferences;
import static rnfive.djs.cyclingcomputer.define.StaticVariables.bMetric;
import static rnfive.djs.cyclingcomputer.define.StaticVariables.getThemeColor;

@Getter
@Setter
public class Activity_DataFieldPrefs extends AppCompatActivity {

    private static final String TAG = Activity_DataFieldPrefs.class.getSimpleName();
    private LinearLayout mainLayout;
    private int mHeight;
    private int mWidth;
    private Rect mViewRect;
    private int[] buttonIds;
    private List<Integer> buttonId;
    private int half_dp;
    private String s_type;
    private final int[][] dataFieldNames = DataFieldName.getIntArray();
    private static final int[][] fieldDef = new FieldDef().toArray();

    public static final String DATA_FIELD_KEY = "rnfive.djs.cyclingcomputer.Activity_DataFieldPrefs.bundle";
    public static final String DATA_FIELD_TYPE_KEY = "rnfive.djs.cyclingcomputer.Activity_DataFieldPrefs.tokenType";

    private ColorStateList colorStateList = new ColorStateList(
            new int[][]{

                    new int[]{-android.R.attr.state_enabled}, //disabled
                    new int[]{android.R.attr.state_enabled} //enabled
            },
            new int[] {

                    R.attr.colorOnPrimary, //disabled
                    R.attr.colorSecondary //enabled

            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_field_prefs);

        mainLayout = findViewById(R.id.data_field_prefs_main);
        ScrollView scrollView = findViewById(R.id.data_field_prefs_scroll_view);
        buttonId = new ArrayList<>();

        mViewRect = new Rect();

        buttonIds = new int[activityDataFields.length];
        half_dp = Display.getPxFromDp(0.5f);

        Intent i = getIntent();
        Bundle args = i.getBundleExtra(DATA_FIELD_KEY);
        if (args != null)
            s_type = (String) args.getSerializable(DATA_FIELD_TYPE_KEY);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(actionBar.getTitle() + " - " + s_type);
            actionBar.show();
        }

        ViewTreeObserver vtoPbv = scrollView.getViewTreeObserver();
        vtoPbv.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setMHeight(scrollView.getHeight());
                setMWidth(scrollView.getWidth());
                //mTop = (int)scrollView.getX();
                scrollView.getGlobalVisibleRect(getMViewRect());
                addSpinners();
                ViewTreeObserver obsPbv = scrollView.getViewTreeObserver();
                obsPbv.removeOnGlobalLayoutListener(this);
            }
        });
    }

    void addSpinners() {

        LinearLayout linearLayoutRow = new LinearLayout(this);
        LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(mWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        int lastRow = -1;
        int fieldNum = 0;
        int dataField;
        for (int[] col : fieldDef) {
            if (col[0]!=lastRow) {
                if (lastRow >= 0 )
                    mainLayout.addView(linearLayoutRow);
                linearLayoutRow = new LinearLayout(this);
                linearLayoutRow.setMinimumHeight(mHeight/12);
                linearLayoutRow.setLayoutParams(linearParams);
                linearLayoutRow.setOrientation(LinearLayout.HORIZONTAL);
            }

            String s_field; // = "Field " + String.valueOf(fieldNum+1);
            StringBuilder sb_unit = new StringBuilder();

            dataField = preferences.getBikeDataFields()[fieldNum];
            Integer[] fieldStrings = DataFields.dataFieldMap.get(dataField, DataFields.dataFieldMapDefault);
            if (fieldStrings[0] == -1 || dataField == DataFields.NONE) {
                s_field = "-";
                sb_unit.setLength(0);
            } else if (dataField == DataFields.EMPTY) {
                s_field = getString(fieldStrings[0]);
                sb_unit.setLength(0);
            } else {
                if (col[2]==2)
                    s_field = getString(fieldStrings[1]);
                else
                    s_field = getString(fieldStrings[0]);
                if (bMetric) {
                    if (fieldStrings[3] != -1) {
                        sb_unit.append(getString(fieldStrings[3]));
                        if (fieldStrings[5] != -1) {
                            sb_unit.append(sb_unit.length() > 1 ? "/" : "p");
                            sb_unit.append(getString(fieldStrings[5]));
                        }
                    }
                } else {
                    if (fieldStrings[2] != -1) {
                        sb_unit.append(getString(fieldStrings[2]));
                        if (fieldStrings[4] != -1) {
                            sb_unit.append(sb_unit.length() > 1 ? "/" : "p");
                            sb_unit.append(getString(fieldStrings[4]));
                        }
                    }
                }
            }
            String s_unit = sb_unit.toString();
            String s_value = s_field + (s_unit.isEmpty()?"":"\n(") + s_unit + (s_unit.isEmpty()?"":")");
            SpannableString spannableString = new SpannableString(s_value);
            spannableString.setSpan(new RelativeSizeSpan(0.8f), s_field.length(), s_value.length(), 0);
            Button button = new Button(this);
            button.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            button.setMinHeight(mHeight/12);
            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(mWidth/(6/col[2]), ViewGroup.LayoutParams.MATCH_PARENT);
            titleParams.topMargin = -half_dp;
            if (col[1]>0)
                titleParams.leftMargin = -half_dp;
            if (col[2]+col[1]==6)
                titleParams.rightMargin = -half_dp;
            button.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.rectangle_outline_05dp, getTheme()));
            button.setLayoutParams(titleParams);
            button.setText(spannableString);
            button.setTextColor(getThemeColor(this, R.attr.colorOnSecondary));
            button.setSingleLine(false);
            button.setTextSize(14);
            button.setId(View.generateViewId());
            buttonIds[fieldNum] = button.getId();
            buttonId.add(button.getId());
            button.setOnClickListener(onButtonClick(fieldNum));
            linearLayoutRow.addView(button);

            fieldNum ++;
            lastRow = col[0];
        }
        mainLayout.addView(linearLayoutRow);
    }

    private View.OnClickListener onButtonClick(int field) {
        return v -> popupDataFields(field);
    }

    private View.OnClickListener onRadioClick(int field) {
        return v -> clickRadioButton(v, field);
    }

    private void clickRadioButton(View v, int field) {
        boolean checked = ((Checkable) v).isChecked();
        if (checked) {
            RadioButton clickedButton = (RadioButton) v;
            Button updateButton = findViewById(buttonIds[field]);

            int dataField = clickedButton.getId();
            Log.d("RadioClick",dataField + ":" + clickedButton.getText());

            String s_field;
            String s_unit = "";

            Integer[] fieldStrings = DataFields.dataFieldMap.get(dataField, DataFields.dataFieldMapDefault);
            if (fieldStrings[0] == -1 || dataField == DataFields.NONE) {
                s_field = "-";
                s_unit = "";
            } else if (dataField == DataFields.EMPTY) {
                s_field = getString(fieldStrings[0]);
                s_unit = "";
            } else {

                if (buttonId.indexOf(updateButton.getId())>= buttonId.size()-18)
                    s_field = getString(fieldStrings[1]);
                else
                    s_field = getString(fieldStrings[0]);

                if (bMetric) {
                    if (fieldStrings[3] != -1) {
                        s_unit = getString(fieldStrings[3]);
                        if (fieldStrings[5] != -1)
                            s_unit += (s_unit.length()>1?"/":"p") + getString(fieldStrings[5]);
                    }
                } else {
                    if (fieldStrings[2] != -1) {
                        s_unit = getString(fieldStrings[2]);
                        if (fieldStrings[4] != -1)
                            s_unit += (s_unit.length()>1?"/":"p") + getString(fieldStrings[4]);
                    }
                }
            }

            String s_value = s_field + (s_unit.isEmpty()?"":"\n(") + s_unit + (s_unit.isEmpty()?"":")");
            SpannableString spannableString = new SpannableString(s_value);
            spannableString.setSpan(new RelativeSizeSpan(0.8f), s_field.length(), s_value.length(), 0);
            updateButton.setText(spannableString);

            updateKey(field,dataField);
        }
    }

    private void updateKey(int field, int dataField) {
        preferences.getBikeDataFields()[field] = dataField;
        preferences.save();
    }

    private void popupDataFields(int field) {

        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        // Inflate the custom layout/view
        if (inflater != null) {

            int dp20 = Display.getPxFromDp(20);
            LinearLayout rootLayout = new LinearLayout(inflater.getContext());
            LinearLayout.LayoutParams rootParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rootParams.setMargins(dp20, dp20, dp20, dp20);
            rootLayout.setLayoutParams(rootParams);

            View customView = inflater.inflate(R.layout.popup_data_fields, rootLayout, true);
            RadioGroup radioGroup = customView.findViewById(R.id.data_fields_radio_group);
            radioGroup.setOnCheckedChangeListener((group, checkedId) -> {

            });
            RadioGroup.LayoutParams radioButtonParams = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            /*
            for (int[] val : dataFieldNames) {
                if (val[0] == 1) {
                    TextView titleText = new TextView(this);
                    titleText.setText(getString(val[1]));
                    titleText.setTextColor(getColor(R.color.colorAccent));
                    titleText.setTextSize(14);
                    titleText.setLayoutParams(radioButtonParams);
                    radioGroup.addView(titleText);
                } else {
                    Integer[] stringIds = DataFields.dataFieldMap.get(val[1], DataFields.dataFieldMapDefault);
                    RadioButton newRadioButton = new RadioButton(this);
                    if (stringIds[0] != -1)
                        newRadioButton.setText(getString(stringIds[0]));
                    newRadioButton.setTextSize(14);
                    newRadioButton.setLayoutParams(radioButtonParams);
                    newRadioButton.setTextColor(getThemeColor(this, R.attr.colorOnSecondary));
                    newRadioButton.setPadding(dp20/4,0,0,0);
                    newRadioButton.setId(val[1]);
                    if (val[1] == preferences.getBikeDataFields()[field])
                        newRadioButton.toggle();
                    newRadioButton.setOnClickListener(onRadioClick(field));
                    radioGroup.addView(newRadioButton);
                }
            }

             */
            for (DataFieldName df : DataFieldName.toList()) {
                Log.d(TAG, "Flag[" + df.getFlag() + "] Name[" + df.getName() + "]");
                if (df.getFlag() == 1) {
                    TextView titleText = new TextView(this);
                    titleText.setText(getString(df.getName()));
                    titleText.setTextColor(getColor(R.color.colorAccent));
                    titleText.setTextSize(14);
                    titleText.setLayoutParams(radioButtonParams);
                    radioGroup.addView(titleText);
                } else {
                    Integer[] stringIds = DataFields.dataFieldMap.get(df.getName(), DataFields.dataFieldMapDefault);
                    RadioButton newRadioButton = new RadioButton(this);
                    if (stringIds[0] != -1)
                        newRadioButton.setText(getString(stringIds[0]));
                    newRadioButton.setTextSize(14);
                    newRadioButton.setLayoutParams(radioButtonParams);
                    newRadioButton.setTextColor(getThemeColor(this, R.attr.colorOnPrimary));
                    newRadioButton.setButtonTintList(colorStateList);
                    newRadioButton.setPadding(dp20/4,0,0,0);
                    newRadioButton.setId(df.getName());
                    if (df.getName() == preferences.getBikeDataFields()[field])
                        newRadioButton.toggle();
                    newRadioButton.setOnClickListener(onRadioClick(field));
                    radioGroup.addView(newRadioButton);
                }
            }

            PopupWindow popUpWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, mHeight-dp20/2, true);
            popUpWindow.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.bg_round_corner_solid_white, null));
            popUpWindow.setElevation(10.0f);
            popUpWindow.showAtLocation(mainLayout, Gravity.NO_GRAVITY, dp20/4, mViewRect.top+dp20/4);
            popUpWindow.update();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("PREFS","onResume()");
        Intent i = getIntent();
        Bundle args = i.getBundleExtra(DATA_FIELD_KEY);
        if (args != null)
            s_type = (String) args.getSerializable(DATA_FIELD_TYPE_KEY);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("PREFS","onPause()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("PREFS","onDestroy()");
    }
}

