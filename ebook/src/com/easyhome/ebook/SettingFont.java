
package com.easyhome.ebook;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.easyhome.ebook.R;

public class SettingFont extends Activity {
    private TextView textShow;
    private Button btnOk;
    private SeekBar seekBar;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.settting_fontsize);

        sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        textShow = (TextView) findViewById(R.id.textShow);

        btnOk = (Button) findViewById(R.id.btnOk);
        btnOk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                final int fontSize = getValidateProgress(seekBar.getProgress());
                Editor edit = sharedPreferences.edit();
                edit.putInt("font_size", fontSize);
                edit.commit();
                finish();
            }
        });

        seekBar = (SeekBar) findViewById(R.id.seekBarFontSize);
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
                textShow.setText(progress + "");
                textShow.setTextSize(progress);
            }
        });

        int progress = sharedPreferences.getInt("font_size", 26);
        textShow.setText(progress + "");
        textShow.setTextSize(progress);
        seekBar.setProgress(progress);
    }
    
    private int getValidateProgress(int progress){
        return progress + 8;
    }
}
