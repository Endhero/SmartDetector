package com.lcd.smartdetectview.wegit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

import entity.info.resultinfo.MainObjectResultInfo;

public class DetectFinderView extends View
{
    private Paint m_paint;
    private int m_nLeft;
    private int m_nTop;
    private int m_nRight;
    private int m_nBottom;
    private String m_strDescription;
    private TextPaint m_textpaint;

    public DetectFinderView(Context context)
    {
        super(context);

        init();
    }

    public DetectFinderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public DetectFinderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        m_paint = new Paint();
        //防锯齿
        m_paint.setAntiAlias(true);
        //防抖动
        m_paint.setDither(true);
        m_paint.setStyle(Paint.Style.STROKE);
        m_paint.setColor(Color.RED);
        m_paint.setStrokeWidth(1);

        m_textpaint = new TextPaint();
        m_textpaint.setColor(Color.GREEN);
        m_textpaint.setTextSize(30);
        m_textpaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//清空画布
        canvas.drawRect(m_nLeft, m_nTop, m_nRight, m_nBottom, m_paint);

        if (m_strDescription != null) {
            //canvas.drawText无法换行需要使用StaticLayout
            StaticLayout staticlayout = new StaticLayout(m_strDescription, m_textpaint, canvas.getWidth(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            staticlayout.draw(canvas);
        }

        invalidate();
    }

    public void setFinderLocation(MainObjectResultInfo mainobjectresultinfo) {
        int nLeft = 0;
        int nTop = 0;
        int nWidth = 0;
        int nHeight = 0;

        if (mainobjectresultinfo != null) {
            nLeft = mainobjectresultinfo.getLeft();
            nTop = mainobjectresultinfo.getTop();
            nWidth = mainobjectresultinfo.getWidth();
            nHeight = mainobjectresultinfo.getHeight();
        }

        setFinderLoaction(nLeft, nTop, nLeft + nWidth, nTop + nHeight);
        invalidate();
    }

    public void setFinderLoaction(int nLeft, int nTop, int nRight, int nBottom) {
        m_nLeft = nLeft;
        m_nTop = nTop;
        m_nRight = nRight;
        m_nBottom = nBottom;
    }

    public void setDescription(JSONArray jsonarrayResult) throws Exception {
        if (jsonarrayResult != null) {
            StringBuilder stringbuilder = new StringBuilder("");

            for (int i = 0; i < jsonarrayResult.length(); i++) {
                JSONObject jsonobject = jsonarrayResult.getJSONObject(i);

                if (jsonobject.has("name")) {
                    stringbuilder.append("检测结果" + (i + 1) + ":" + jsonobject.getString("name") + " ");
                } else if (jsonobject.has("keyword")) {
                    stringbuilder.append("检测结果" + (i + 1) + ":" + jsonobject.getString("keyword") + " ");
                }

                if (jsonobject.has("score")) {
                    stringbuilder.append("相似度:" + String.format("%.2f", jsonobject.getDouble("score") * 100) + "% \n");
                }

//                if (jsonobject.has("baike_info"))
//                {
//                    JSONObject jsonobjectBaikeInfo = jsonobject.getJSONObject("baike_info");
//
//                    if (jsonobjectBaikeInfo.has("description"))
//                    {
//                        stringbuilder.append("description:" + jsonobjectBaikeInfo.getString("description") + "\n");
//                    }
//                }
            }

            m_strDescription = stringbuilder.toString();
        } else
            m_strDescription = "";

        invalidate();
    }
}
