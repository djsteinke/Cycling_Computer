package rnfive.djs.cyclingcomputer;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import rnfive.djs.cyclingcomputer.R;

import java.io.File;
import java.io.FileReader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import rnfive.djs.cyclingcomputer.utils.MenuUtil;

public class LogReader extends AppCompatActivity {

    private final static String TAG = "LogReader";

    private TextView tvLogText;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        tvLogText = findViewById(R.id.log_text);
        updateLog();
    }

    private void updateLog() {
        File file = new File(MainActivity.filePathLog,"log.txt");

        if (file.exists()) {
            try {
                StringBuilder sb = new StringBuilder();
                FileReader fr=new FileReader(file);
                int i;
                while((i=fr.read())!=-1)
                    sb.append((char)i);
                fr.close();
                tvLogText.setText(sb.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result;
        result = MenuUtil.menuItemSelector(this, item, TAG);
        return result || super.onOptionsItemSelected(item);
    }
}
