package rnfive.htfu.cyclingcomputer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import rnfive.htfu.cyclingcomputer.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class ArrayAdapter_AntSearchResult extends ArrayAdapter<Activity_AntSensorSearch.AntSearchResult>
{

    private ArrayList<Activity_AntSensorSearch.AntSearchResult> mData;
    private Context mContext;

    ArrayAdapter_AntSearchResult(@NonNull Context context,
                                      ArrayList<Activity_AntSensorSearch.AntSearchResult> data)
    {
        super(context, R.layout.array_adapter_ant_search_result, data);
        mData = data;
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.array_adapter_ant_search_result,parent,false);

        Activity_AntSensorSearch.AntSearchResult i = mData.get(position);

        String s_deviceType;
        if (i != null) {
            TextView tv_deviceType = listItem.findViewById(R.id.ant_saved_device_name);
            TextView tv_deviceId = listItem.findViewById(R.id.ant_saved_device_id);
            ImageView iv_saved = listItem.findViewById(R.id.ant_saved_device_connected);
            ImageView iv_connected = listItem.findViewById(R.id.ant_connected_icon);

            if (tv_deviceType != null) {
                s_deviceType = i.mDeviceType.toString();
                tv_deviceType.setText(s_deviceType.substring(0,s_deviceType.length()-1));
            }
            if (tv_deviceId != null) {
                tv_deviceId.setText(String.valueOf(i.mDeviceId));
            }
            if (iv_saved != null) {
                iv_saved.setEnabled(i.bSaved);
                iv_saved.setColorFilter((i.bSaved? MainActivity.accent: MainActivity.gray));
            }
            if (iv_connected != null)
                iv_connected.setVisibility(i.bConnected?View.VISIBLE:View.GONE);
        }

        return listItem;
    }
}
