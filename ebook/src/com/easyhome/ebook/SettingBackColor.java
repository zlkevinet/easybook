
package com.easyhome.ebook;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.easyhome.ebook.R;

public class SettingBackColor extends Activity implements OnClickListener {
    private TextView textView1;
    private TextView textView2;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.settting_backcolor);
        textView1 = (TextView) findViewById(R.id.textView1);
        textView1.setOnClickListener(this);
        textView2 = (TextView) findViewById(R.id.textView2);
        textView2.setOnClickListener(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        Editor edit = sharedPreferences.edit();
        switch (v.getId()) {
            case R.id.textView1:
                edit.putInt("back_color", 0xffffffff);
                edit.putInt("font_color", 0xff000000);
                edit.commit();
                finish();
                break;
            case R.id.textView2:
                edit.putInt("back_color", 0xff5a5a5a);
                edit.putInt("font_color", 0xffffffff);
                edit.commit();
                finish();
                break;
        }
    }
}
