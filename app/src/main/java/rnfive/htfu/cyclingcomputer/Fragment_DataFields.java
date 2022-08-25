package rnfive.htfu.cyclingcomputer;

import android.content.Context;
import android.os.Bundle;
import android.text.SpannableString;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import lombok.Getter;
import lombok.Setter;
import rnfive.htfu.cyclingcomputer.define.DataFields;
import rnfive.htfu.cyclingcomputer.define.Display;
import rnfive.htfu.cyclingcomputer.define.FieldDef;
import com.dsi.ant.plugins.antplus.pcc.defines.BatteryStatus;

import java.util.regex.Pattern;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static rnfive.htfu.cyclingcomputer.MainActivity.activityDataFields;
import static rnfive.htfu.cyclingcomputer.MainActivity.black;
import static rnfive.htfu.cyclingcomputer.MainActivity.fragWidth;
import static rnfive.htfu.cyclingcomputer.MainActivity.white;
import static rnfive.htfu.cyclingcomputer.define.StaticVariables.bHrZoneColors;
import static rnfive.htfu.cyclingcomputer.define.StaticVariables.bInvert;
import static rnfive.htfu.cyclingcomputer.define.StaticVariables.bMetric;
import static rnfive.htfu.cyclingcomputer.define.StaticVariables.bPowerZoneColors;
import static rnfive.htfu.cyclingcomputer.define.StaticVariables.iAthleteFtp;
import static rnfive.htfu.cyclingcomputer.define.StaticVariables.iAthleteHrMax;

@Getter
@Setter
public class Fragment_DataFields extends Fragment {

    private static final String TAG = "Fragment_DataFields";
    private static final Pattern COMPILE = Pattern.compile("[0-9]+");
    private GridLayout gridLayout;
    private ScrollView scrollView;
    private Context context;
    private static int dataFieldsLength;
    private static final FieldDef fDef = new FieldDef();
    private static final int[][] fieldDef = fDef.toArray();
    private final View[] cellList = new View[fDef.getSize()];
    private int margin;

    public Fragment_DataFields() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        dataFieldsLength = activityDataFields.length;
        View newView = inflater.inflate(R.layout.fragment_data_fields, container, false);
        gridLayout = newView.findViewById(R.id.data_field_grid_layout);
        gridLayout.setRowCount(fDef.getSize());
        scrollView = newView.findViewById(R.id.data_field_frame_layout);
        Log.d(TAG, "addCells");
        addCells();
        return newView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        Log.d(TAG, "onAttach");
    }

    private void addCells() {
        int i = 0;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        margin = Display.getPxFromDp(0.5f);
        for (int[] def : fieldDef) {
            /*
            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
            layoutParams.columnSpec = GridLayout.spec(i$[1],i$[2], GridLayout.FILL);
            layoutParams.rowSpec = GridLayout.spec(i$[0],1, GridLayout.FILL);
            layoutParams.width = width/(6/i$[2]);
            if (cellsExistBellow(i))
                layoutParams.bottomMargin = -margin;
            if (i$[1]!=0)
                layoutParams.leftMargin = -margin;
                */
            FrameLayout rootLayout = new FrameLayout(context);
            FrameLayout.LayoutParams rootParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
            rootLayout.setLayoutParams(rootParams);

            if (inflater != null) {
                View newView = inflater.inflate(R.layout.cell_data_field, rootLayout, false);
                newView.setLayoutParams(getLayoutParams(i));
                newView.setId(View.generateViewId());
                TextView tv = newView.findViewById(R.id.cell_value);
                LinearLayout unit = newView.findViewById(R.id.cell_unit_layout);
                LinearLayout mainLL = newView.findViewById(R.id.cell_ll);
                switch (def[2]) {
                    case 6:
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 80);
                        mainLL.setPadding(mainLL.getPaddingLeft(), Display.getPxFromDp(5), mainLL.getPaddingRight(), 0);
                        unit.setPadding(0, Display.getPxFromDp(5), 0, 0);
                        break;
                    case 2:
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
                        mainLL.setPadding(mainLL.getPaddingLeft(), Display.getPxFromDp(10), mainLL.getPaddingRight(), 0);
                        unit.setPadding(0, Display.getPxFromDp(10), 0, 0);
                        break;
                    default:
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 50);
                        mainLL.setPadding(mainLL.getPaddingLeft(), Display.getPxFromDp(8), mainLL.getPaddingRight(), 0);
                        unit.setPadding(0, Display.getPxFromDp(8), 0, 0);
                        break;
                }
                gridLayout.addView(newView);
                cellList[i++] = newView;
            }
        }
    }

    private GridLayout.LayoutParams getLayoutParams(int i) {
        int[] def = fieldDef[i];
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.columnSpec = GridLayout.spec(def[1],def[2], GridLayout.FILL);
        layoutParams.rowSpec = GridLayout.spec(def[0],1, GridLayout.FILL);
        layoutParams.width = fragWidth/(6/def[2]);
        layoutParams.bottomMargin = (cellsExistBellow(i)?-margin:0);
        layoutParams.leftMargin = (def[1]!=0?-margin:0);
        return layoutParams;
    }

    private boolean cellsExistBellow(int i) {
        for (int j=i; j<dataFieldsLength;j++) {
            if (fieldDef[i][0] != fieldDef[j][0]) {
                Integer[] map = DataFields.dataFieldMap.get(activityDataFields[j], DataFields.dataFieldMapDefault);
                if (map[0] != -1 && activityDataFields[j] != DataFields.NONE) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean rowEmpty(int i) {
        int rowOrig = fieldDef[i][0];
        int j;
        int rowNext;
        boolean result = false;
        if (activityDataFields[i] == DataFields.EMPTY) {
            result = true;
        }
        if (result) {
            j = i+1;
            rowNext = fieldDef[j][0];
            while (rowOrig == rowNext && j < dataFieldsLength) {
                if (activityDataFields[j] != DataFields.EMPTY) {
                    result = false;
                    break;
                }
                j++;
                rowNext = fieldDef[j][0];
            }
        }
        if (result) {
            j = i-1;
            rowNext = fieldDef[j][0];
            while (rowOrig == rowNext && j > 0) {
                if (activityDataFields[j] != DataFields.EMPTY) {
                    result = false;
                    break;
                }
                j--;
                rowNext = fieldDef[j][0];
            }
        }
        return result;
    }

    public void setDataFields() {
        scrollView.setBackgroundColor(black);
        gridLayout.setBackgroundColor((bInvert?white:black));
        int i = 0;
        for (int field : activityDataFields) {
            setField(i++,field);
        }
    }

    private void setField(int pos, int dataField) {
        Integer[] map = DataFields.dataFieldMap.get(dataField, DataFields.dataFieldMapDefault);
        if (dataField == DataFields.NONE || map[0] == -1)
            cellList[pos].setVisibility(GONE);
        else if (dataField == DataFields.EMPTY) {
            //cellList[pos].setVisibility(View.INVISIBLE);
            if (rowEmpty(pos)) {
                cellList[pos].getLayoutParams().height = Display.getPxFromDp(20);
                cellList[pos].setBackgroundColor(black);
                GridLayout.LayoutParams layoutParams = (GridLayout.LayoutParams) cellList[pos].getLayoutParams();
                layoutParams.topMargin = margin;
                cellList[pos].setLayoutParams(layoutParams);
                LinearLayout unitLayout = cellList[pos].findViewById(R.id.cell_unit_layout);
                unitLayout.setVisibility(GONE);
                TextView title = cellList[pos].findViewById(R.id.cell_title);
                title.setTextColor(black);
            } else
                cellList[pos].setVisibility(View.INVISIBLE);
        } else {
            cellList[pos].setVisibility(VISIBLE);
            cellList[pos].setBackground(AppCompatResources.getDrawable(context, R.drawable.rectangle_outline_05dp_gray));
            cellList[pos].setLayoutParams(getLayoutParams(pos));
            TextView title = cellList[pos].findViewById(R.id.cell_title);
            title.setTextColor((bInvert?black:white));
            TextView value = cellList[pos].findViewById(R.id.cell_value);
            value.setTextColor((bInvert?black:white));
            TextView unitTop = cellList[pos].findViewById(R.id.cell_unit_top);
            unitTop.setTextColor((bInvert?black:white));
            TextView unitBottom = cellList[pos].findViewById(R.id.cell_unit_bottom);
            unitBottom.setTextColor((bInvert?black:white));
            LinearLayout unitLayout = cellList[pos].findViewById(R.id.cell_unit_layout);
            ImageView unitLine = cellList[pos].findViewById(R.id.cell_unit_line);
            unitLine.setColorFilter((bInvert?black:white));
            ImageView arrow = cellList[pos].findViewById(R.id.cell_image);
            if (dataField == DataFields.WIND) {
                arrow.setVisibility(VISIBLE);
                arrow.getLayoutParams().height = (int) value.getTextSize();
                arrow.getLayoutParams().width = (int) value.getTextSize();
                arrow.setColorFilter((bInvert?black:white));
                value.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            } else
                arrow.setVisibility(GONE);
            if (pos > 17)
                title.setText(getString(map[1]));
            else
                title.setText(getString(map[0]));
            if (map[2] == -1) {
                unitLayout.setVisibility(GONE);
            } else {
                unitLayout.setVisibility(VISIBLE);
                if (bMetric)
                    unitTop.setText(getString(map[3]));
                else
                    unitTop.setText(getString(map[2]));
                if (map[4] == -1) {
                    unitLine.setVisibility(GONE);
                    unitBottom.setVisibility(GONE);
                } else {
                    unitLine.setVisibility(VISIBLE);
                    unitBottom.setVisibility(VISIBLE);
                    if (bMetric)
                        unitBottom.setText(getString(map[5]));
                    else
                        unitBottom.setText(getString(map[4]));
                }
            }
        }
    }

    public void setValue(int pos, String val) {
        if (activityDataFields[pos] != DataFields.NONE) {
            TextView tv = cellList[pos].findViewById(R.id.cell_value);
            tv.setText(val);
            TextView tvTitle = cellList[pos].findViewById(R.id.cell_title);
            String sTitle = (tvTitle!=null?tvTitle.getText().toString():"");
            if (bHrZoneColors && (sTitle.equals("Heart Rate") || sTitle.equals("HR"))) {
                int iHrZone = -1;
                if (COMPILE.matcher(val).matches()) {
                    int iHr = Integer.parseInt(val);
                    for (int[] zone : DataFields.HR_ZONE) {
                        if (zone[0] * iAthleteHrMax < iHr * 100)
                            iHrZone++;
                    }
                }
                if (iHrZone==-1)
                    iHrZone++;
                tv.setTextColor(context.getColor(DataFields.HR_ZONE[iHrZone][(bInvert?1:2)]));
            }
            if (bPowerZoneColors && sTitle.startsWith("Power")) {
                int iZone = -1;
                if (COMPILE.matcher(val).matches()) {
                    int iVal = Integer.parseInt(val);
                    for (int[] zone : DataFields.POWER_ZONE) {
                        if (zone[0] * iAthleteFtp < iVal * 100)
                            iZone++;
                    }
                }
                if (iZone==-1)
                    iZone++;
                tv.setTextColor(context.getColor(DataFields.POWER_ZONE[iZone][(bInvert?1:2)]));
            }
        }
    }

    public void setImageValue(int pos, float rot) {
        if (activityDataFields[pos] != DataFields.NONE) {
            ImageView iv = cellList[pos].findViewById(R.id.cell_image);
            iv.setRotation(rot);
        }
    }

    public void setSSValue(int pos, SpannableString val) {
        if (activityDataFields[pos] != DataFields.NONE) {
            TextView tv = cellList[pos].findViewById(R.id.cell_value);
            tv.setText(val);
        }
    }

    public void setCornerIcon(int pos, int value) {
        if (activityDataFields[pos] != DataFields.NONE) {
            ImageView iv = cellList[pos].findViewById(R.id.corner_icon);
            if (value == BatteryStatus.LOW.getIntValue())
                iv.setVisibility(VISIBLE);
            else if (value == BatteryStatus.CRITICAL.getIntValue()) {
                iv.setVisibility((iv.getVisibility()==VISIBLE ? GONE : VISIBLE));
            }
        }
    }

}
