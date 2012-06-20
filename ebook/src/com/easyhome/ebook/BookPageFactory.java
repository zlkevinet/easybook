
package com.easyhome.ebook;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.Vector;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.preference.PreferenceManager;

public class BookPageFactory {
    private final static int DEFAULT_BACK_COLOR = Color.WHITE;
    private final static int DEFAULT_FONT_COLOR = Color.BLACK;
    private final static int DEFAULT_FONT_SIZE = 26;
    private final static int DEFAULT_MARGIN_WIDTH = 15;
    private final static int DEFAULT_MARGIN_HEIGHT = 20;

    private File book_file;
    private MappedByteBuffer m_mbBuf;
    private int m_mbBufLen;
    private int m_mbBufBegin;
    private int m_mbBufEnd;
    private String m_strCharsetName = "GBK";
    private Bitmap m_book_bg;
    private int mWidth;
    private int mHeight;
    private Vector<String> m_lines = new Vector<String>();
    private int m_fontSize;
    private int m_textColor;
    private int m_backColor;
    private int marginWidth = DEFAULT_MARGIN_WIDTH;
    private int marginHeight = DEFAULT_MARGIN_HEIGHT;
    private int mLineCount;
    private float mVisibleHeight;
    private float mVisibleWidth;
    private boolean m_isfirstPage;
    private boolean m_islastPage;
    private Paint mPaint;
    private SharedPreferences sharedPreferences;
    DecimalFormat df = new DecimalFormat("##.##");
    private Context mContext;
    private int mPercent;

    public BookPageFactory(int w, int h, Context context) {
        mContext = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mWidth = w;
        mHeight = h - sharedPreferences.getInt("PaddingButtom", 0);;
        m_fontSize = sharedPreferences.getInt("font_size", DEFAULT_FONT_SIZE);
        m_textColor = sharedPreferences.getInt("font_color", DEFAULT_FONT_COLOR);
        m_backColor = sharedPreferences.getInt("back_color", DEFAULT_BACK_COLOR);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextAlign(Align.LEFT);
        mPaint.setTextSize(m_fontSize);
        mPaint.setColor(m_textColor);
        mVisibleWidth = mWidth - marginWidth * 2;
        mVisibleHeight = mHeight - marginHeight * 2;
        mLineCount = (int) (mVisibleHeight / m_fontSize);
        m_mbBufBegin = sharedPreferences.getInt("Begin", 0);
        m_mbBufEnd = m_mbBufBegin;
    }

    public void setFontSize(int size) {
        Editor edit = sharedPreferences.edit();
        edit.putInt("font_size", size);
        edit.commit();
    }

    public void setBackColor(int color) {
        Editor edit = sharedPreferences.edit();
        edit.putInt("back_color", color);
        edit.commit();
    }

    public void openbook(File file) throws IOException {
        book_file = file;
        long lLen = book_file.length();
        m_mbBufLen = (int) lLen;
        m_mbBuf = new RandomAccessFile(book_file, "r").getChannel().map(
                FileChannel.MapMode.READ_ONLY, 0, lLen);
    }

    protected byte[] readParagraphBack(int nFromPos) {
        int nEnd = nFromPos;
        int i;
        byte b0, b1;
        if (m_strCharsetName.equals("UTF-16LE")) {
            i = nEnd - 2;
            while (i > 0) {
                b0 = m_mbBuf.get(i);
                b1 = m_mbBuf.get(i + 1);
                if (b0 == 0x0a && b1 == 0x00 && i != nEnd - 2) {
                    i += 2;
                    break;
                }
                i--;
            }

        } else if (m_strCharsetName.equals("UTF-16BE")) {
            i = nEnd - 2;
            while (i > 0) {
                b0 = m_mbBuf.get(i);
                b1 = m_mbBuf.get(i + 1);
                if (b0 == 0x00 && b1 == 0x0a && i != nEnd - 2) {
                    i += 2;
                    break;
                }
                i--;
            }
        } else {
            i = nEnd - 1;
            while (i > 0) {
                b0 = m_mbBuf.get(i);
                if (b0 == 0x0a && i != nEnd - 1) {
                    i++;
                    break;
                }
                i--;
            }
        }
        if (i < 0)
            i = 0;
        int nParaSize = nEnd - i;
        int j;
        byte[] buf = new byte[nParaSize];
        for (j = 0; j < nParaSize; j++) {
            buf[j] = m_mbBuf.get(i + j);
        }
        return buf;
    }

    protected byte[] readParagraphForward(int nFromPos) {
        int nStart = nFromPos;
        int i = nStart;
        byte b0, b1;
        if (m_strCharsetName.equals("UTF-16LE")) {
            while (i < m_mbBufLen - 1) {
                b0 = m_mbBuf.get(i++);
                b1 = m_mbBuf.get(i++);
                if (b0 == 0x0a && b1 == 0x00) {
                    break;
                }
            }
        } else if (m_strCharsetName.equals("UTF-16BE")) {
            while (i < m_mbBufLen - 1) {
                b0 = m_mbBuf.get(i++);
                b1 = m_mbBuf.get(i++);
                if (b0 == 0x00 && b1 == 0x0a) {
                    break;
                }
            }
        } else {
            while (i < m_mbBufLen) {
                b0 = m_mbBuf.get(i++);
                if (b0 == 0x0a) {
                    break;
                }
            }
        }
        int nParaSize = i - nStart;
        byte[] buf = new byte[nParaSize];
        for (i = 0; i < nParaSize; i++) {
            buf[i] = m_mbBuf.get(nFromPos + i);
        }
        return buf;
    }

    protected Vector<String> pageDown() {
        String strParagraph = "";
        Vector<String> lines = new Vector<String>();
        while (lines.size() < mLineCount && m_mbBufEnd < m_mbBufLen) {
            byte[] paraBuf = readParagraphForward(m_mbBufEnd);
            m_mbBufEnd += paraBuf.length;
            try {
                strParagraph = new String(paraBuf, m_strCharsetName);
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            String strReturn = "";
            if (strParagraph.indexOf("\r\n") != -1) {
                strReturn = "\r\n";
                strParagraph = strParagraph.replaceAll("\r\n", "");
            } else if (strParagraph.indexOf("\n") != -1) {
                strReturn = "\n";
                strParagraph = strParagraph.replaceAll("\n", "");
            }

            if (strParagraph.length() == 0) {
                lines.add(strParagraph);
            }
            while (strParagraph.length() > 0) {
                int nSize = mPaint.breakText(strParagraph, true, mVisibleWidth,
                        null);
                lines.add(strParagraph.substring(0, nSize));
                strParagraph = strParagraph.substring(nSize);
                if (lines.size() >= mLineCount) {
                    break;
                }
            }
            if (strParagraph.length() != 0) {
                try {
                    m_mbBufEnd -= (strParagraph + strReturn)
                            .getBytes(m_strCharsetName).length;
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return lines;
    }

    protected void pageUp() {
        if (m_mbBufBegin < 0) {
            m_mbBufBegin = 0;
        }
        Vector<String> lines = new Vector<String>();
        String strParagraph = "";
        while (lines.size() < mLineCount && m_mbBufBegin > 0) {
            Vector<String> paraLines = new Vector<String>();
            byte[] paraBuf = readParagraphBack(m_mbBufBegin);
            m_mbBufBegin -= paraBuf.length;
            try {
                strParagraph = new String(paraBuf, m_strCharsetName);
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            strParagraph = strParagraph.replaceAll("\r\n", "");
            strParagraph = strParagraph.replaceAll("\n", "");

            if (strParagraph.length() == 0) {
                paraLines.add(strParagraph);
            }
            while (strParagraph.length() > 0) {
                int nSize = mPaint.breakText(strParagraph, true, mVisibleWidth,
                        null);
                paraLines.add(strParagraph.substring(0, nSize));
                strParagraph = strParagraph.substring(nSize);
            }
            lines.addAll(0, paraLines);
        }
        while (lines.size() > mLineCount) {
            try {
                m_mbBufBegin += lines.get(0).getBytes(m_strCharsetName).length;
                lines.remove(0);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        m_mbBufEnd = m_mbBufBegin;
        return;
    }

    protected void prePage() throws IOException {
        if (m_mbBufBegin <= 0) {
            m_mbBufBegin = 0;
            m_isfirstPage = true;
            return;
        } else {
            m_isfirstPage = false;
        }
        m_lines.clear();
        pageUp();
        m_lines = pageDown();
    }

    public void nextPage() throws IOException {
        if (m_mbBufEnd >= m_mbBufLen) {
            m_islastPage = true;
            return;
        } else {
            m_islastPage = false;
        }
        m_lines.clear();
        m_mbBufBegin = m_mbBufEnd;
        m_lines = pageDown();
    }

    public void onDraw(Canvas c) {
        if (m_lines.size() == 0) {
            m_lines = pageDown();
        }
        if (m_lines.size() > 0) {
            if (m_book_bg == null) {
                c.drawColor(m_backColor);
            } else {
                c.drawBitmap(m_book_bg, 0, 0, null);
            }
            int y = marginHeight;
            for (String strLine : m_lines) {
                y += m_fontSize;
                c.drawText(strLine, marginWidth, y, mPaint);
            }
        }
        float fPercent = (float) (m_mbBufBegin * 1.0 / m_mbBufLen);
        mPercent = (int) (fPercent * 100);
        String strPercent = df.format(fPercent * 100) + "%";
        int nPercentWidth = (int) mPaint.measureText("999.9%") + 1;
        c.drawText(strPercent, mWidth - nPercentWidth, mHeight, mPaint);
        Editor edit = sharedPreferences.edit();
        edit.putInt("Begin", m_mbBufBegin);
        edit.commit();
    }

    public int getmPercent() {
        return mPercent;
    }
    
    public void setPaddingButtom(int paddingButtom) {
        Editor edit = sharedPreferences.edit();
        edit.putInt("PaddingButtom", paddingButtom);
        edit.commit();
    }

    public void setmPercent(int mPercent) {
        this.mPercent = mPercent;
        m_mbBufEnd = m_mbBufBegin = (int) ((float) (m_mbBufLen * 1.0 * mPercent) / 100);
        Editor edit = sharedPreferences.edit();
        edit.putInt("Begin", m_mbBufBegin);
        edit.commit();
    }

    public void setBgBitmap(Bitmap BG) {
        m_book_bg = BG;
    }

    public boolean isfirstPage() {
        return m_isfirstPage;
    }

    public boolean islastPage() {
        return m_islastPage;
    }
}
