package rnfive.djs.cyclingcomputer;

import android.os.AsyncTask;

import rnfive.djs.cyclingcomputer.R;

import androidx.fragment.app.FragmentTransaction;

import static rnfive.djs.cyclingcomputer.MainActivity.fm;
import static rnfive.djs.cyclingcomputer.MainActivity.fragmentListener;

public class AsyncTask_FragmentDataFields extends AsyncTask<Void,Void,Fragment_DataFields> {
    @Override
    protected Fragment_DataFields doInBackground(Void... voids) {
        Fragment_DataFields dataFields = new Fragment_DataFields();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.fragment_view,dataFields);
        ft.commit();
        return dataFields;
    }

    @Override
    protected void onPostExecute(Fragment_DataFields fragment) {
        fragment.setDataFields();
        fragmentListener.onFragmentReady(fragment);
    }
}
