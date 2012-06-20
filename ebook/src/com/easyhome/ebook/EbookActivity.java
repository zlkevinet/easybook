
package com.easyhome.ebook;

import com.mobclick.android.MobclickAgent;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class EbookActivity extends Activity {

    public static final String TAG = "EbookActivity";
    private PageWidget mPageWidget;
    Bitmap mCurPageBitmap, mNextPageBitmap;
    Canvas mCurPageCanvas, mNextPageCanvas;
    
    BookPageFactory pagefactory;

    private int mScreenWidth;
    private int mScreenHeight;

    private ViewGroup viewGroup;
    
    private PopupWindow topWindow;
    private PopupWindow dockWindow;
    protected SharedPreferences sharedPreferences;

    private static final String SOURCE_FILE_NAME = "book.txt";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // umeng自动更新
        MobclickAgent.update(this);
        MobclickAgent.setUpdateOnlyWifi(false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        removePaddingButtom();
        getDisplay();
        setWindowStyle();
        
        mCurPageBitmap = Bitmap.createBitmap(mScreenWidth, mScreenHeight,
                Bitmap.Config.ARGB_8888);
        mNextPageBitmap = Bitmap.createBitmap(mScreenWidth, mScreenHeight,
                Bitmap.Config.ARGB_8888);

        mCurPageCanvas = new Canvas(mCurPageBitmap);
        mNextPageCanvas = new Canvas(mNextPageBitmap);

        mPageWidget.setBitmaps(mCurPageBitmap, mCurPageBitmap);
        mPageWidget.setOnTouchListener(pageWidgetOnTouchLsn());
        
        pagefactory = new BookPageFactory(mScreenWidth, mScreenHeight, this);
        initPopupWindow();
    }

    private void removePaddingButtom() {
        Editor editor = sharedPreferences.edit();
        editor.remove("PaddingButtom");
        editor.commit();
    }

    private File getSourceTextFile() throws IOException {
        File tempFile = this.getCacheDir().getAbsoluteFile();
        if (!tempFile.exists()) {
            tempFile.mkdirs();
        }
        tempFile = new File(tempFile.getAbsolutePath(), SOURCE_FILE_NAME);
        if (!tempFile.exists()) {
            tempFile.createNewFile();
            this.copyFile(this.getAssets().open(SOURCE_FILE_NAME), tempFile);
        }
        return tempFile;
    }

    private OnTouchListener pageWidgetOnTouchLsn() {
        return new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {

                boolean ret = false;
                if (v == mPageWidget) {
                    if (e.getAction() == MotionEvent.ACTION_DOWN) {
                        // if (e.getX() > mScreenWidth / 3 && e.getX() <
                        // mScreenWidth / 3 * 2
                        // && e.getY() > mScreenHeight / 3 && e.getY() <
                        // mScreenHeight / 3 * 2) {
                        // initPopupWindow();
                        // return false;
                        // }
                        mPageWidget.abortAnimation();
                        mPageWidget.calcCornerXY(e.getX(), e.getY());

                        pagefactory.onDraw(mCurPageCanvas);
                        if (mPageWidget.DragToRight()) {
                            try {
                                pagefactory.prePage();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            if (pagefactory.isfirstPage())
                                return false;
                            pagefactory.onDraw(mNextPageCanvas);
                        } else {
                            try {
                                pagefactory.nextPage();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            if (pagefactory.islastPage()) {
                                return false;
                            }
                            pagefactory.onDraw(mNextPageCanvas);
                        }
                        mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);
                    }

                    ret = mPageWidget.doTouchEvent(e);
                    return ret;
                }

                return false;
            }
        };
    }

    private void setWindowStyle() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mPageWidget = new PageWidget(this, mScreenWidth, mScreenHeight);
        // setContentView(mPageWidget);
        setContentView(R.layout.main);
        viewGroup = (ViewGroup) findViewById(R.id.book);
    }

    // 复制文件
    public void copyFile(InputStream srcInputStream, File targetFile)
            throws IOException {
        InputStream inBuff = null;
        BufferedOutputStream outBuff = null;
        try {
            // 新建文件输入流并对它进行缓冲
            inBuff = srcInputStream;
            // 新建文件输出流并对它进行缓冲
            outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));
            // 缓冲数组
            byte[] b = new byte[1024 * 5];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }
            // 刷新此缓冲的输出流
            outBuff.flush();
        } finally {
            // 关闭流
            if (inBuff != null)
                inBuff.close();
            if (outBuff != null)
                outBuff.close();
        }
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        goOnShowDraw();
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        
        Log.e(TAG, "keycode = " + keyCode);
        
        if(keyCode==KeyEvent.KEYCODE_VOLUME_DOWN){
            
            if(pagefactory != null){
                mPageWidget.abortAnimation();
                mPageWidget.calcCornerXY(mScreenWidth * 3 / 4, mScreenWidth / 4);
                mPageWidget.setStartPos(mScreenWidth * 3 / 4, mScreenWidth / 4);
                pagefactory.onDraw(mCurPageCanvas);
                if (mPageWidget.DragToRight()) {
                    try {
                        pagefactory.prePage();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    if (pagefactory.isfirstPage())
                        return true;
                    pagefactory.onDraw(mNextPageCanvas);
                } else {
                    try {
                        pagefactory.nextPage();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    if (pagefactory.islastPage()) {
                        return true;
                    }
                    pagefactory.onDraw(mNextPageCanvas);
                }
                mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);
                mPageWidget.pageActionWithAnimation();
                return true;
            }
        }else if(keyCode==KeyEvent.KEYCODE_VOLUME_UP){
            if(pagefactory != null){
                mPageWidget.abortAnimation();
                mPageWidget.calcCornerXY(mScreenWidth  / 4, mScreenWidth / 4);
                mPageWidget.setStartPos(mScreenWidth / 4, mScreenWidth / 4);
                pagefactory.onDraw(mCurPageCanvas);
                if (mPageWidget.DragToRight()) {
                    try {
                        pagefactory.prePage();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    if (pagefactory.isfirstPage())
                        return true;
                    pagefactory.onDraw(mNextPageCanvas);
                } else {
                    try {
                        pagefactory.nextPage();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    if (pagefactory.islastPage()) {
                        return true;
                    }
                    pagefactory.onDraw(mNextPageCanvas);
                }
                mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);
                mPageWidget.pageActionWithAnimation();
                return true;
            }
        } else if(keyCode == KeyEvent.KEYCODE_MENU){
            showOperatorWindow();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void getDisplay() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
    }


    private void initPopupWindow() {
        // TODO ADD TOP MENU WINDOW
//        topWindow = new PopupWindow(this);
//        topWindow.setContentView(getTopPopupView());
//        topWindow.setFocusable(true);
//        topWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.page_slide_bg));
//        dockWindow.setWidth(mScreenWidth);
//        dockWindow.setHeight(120);
        
        dockWindow = new PopupWindow(this);
        dockWindow.setContentView(getDockPopupView());
        dockWindow.setFocusable(true);
        dockWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.page_slide_bg));
        dockWindow.setWidth(mScreenWidth);
        dockWindow.setHeight(200);

    }

    private View getTopPopupView() {
        return null;
    }

    private void showOperatorWindow(){
        
        if(!dockWindow.isShowing()){
            dockWindow.showAtLocation(mPageWidget, Gravity.BOTTOM, 0, 0);
        }else{
            dockWindow.dismiss();
        }
    }
    
    private View getDockPopupView() {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View popView = layoutInflater.inflate(R.layout.page_slide, null);

        final TextView textView = (TextView) popView.findViewById(R.id.textsize);
        textView.setText(pagefactory.getmPercent() + "%");

        SeekBar seekBar = (SeekBar) popView.findViewById(R.id.seekBar);
        seekBar.setProgress(pagefactory.getmPercent());
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                pagefactory.setmPercent(seekBar.getProgress());
                goOnShowDraw();
                try {
                    pagefactory.nextPage();
                    pagefactory.prePage();
                    pagefactory.onDraw(mCurPageCanvas);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText(progress + "%");
            }
        });

        Button smaller = (Button) popView.findViewById(R.id.font_size_smaller);
        smaller.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                pagefactory.reduceFontSize();
                goOnShowDraw();
            }
        });
        
        Button larger = (Button) popView.findViewById(R.id.font_size_larger);
        larger.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                pagefactory.increaseFontSize();
                goOnShowDraw();
            }
        });
        
        Button whiteBack = (Button) popView.findViewById(R.id.back_color_white);
        whiteBack.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                pagefactory.setWhiteOrBlackBack(true);
                goOnShowDraw();
            }
        });
        Button blackBack = (Button) popView.findViewById(R.id.back_color_black);
        blackBack.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                pagefactory.setWhiteOrBlackBack(false);
                goOnShowDraw();
            }
        });
        
        
        return popView;
    }

    private void goOnShowDraw() {
        mPageWidget.setBitmaps(mCurPageBitmap, mCurPageBitmap);
        // setContentView(mPageWidget);
        viewGroup.removeView(mPageWidget);
        viewGroup.addView(mPageWidget);
        pagefactory = new BookPageFactory(mScreenWidth, mScreenHeight, this);
        // Bitmap bm = null;
        // pagefactory.setBgBitmap(bm);
        try {
            File tempFile = getSourceTextFile();
            pagefactory.openbook(tempFile);
            pagefactory.onDraw(mCurPageCanvas);
        } catch (IOException e) {
            Toast.makeText(this, R.string.open_failed_msg, Toast.LENGTH_LONG)
                    .show();
            e.printStackTrace();
        }
        
    }
}
