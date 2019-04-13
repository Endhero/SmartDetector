package com.lcd.smartdetectview.wegit;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;


import com.lcd.smartdetectview.listener.detectlistener.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import entity.detectresult.*;
import entity.info.resultinfo.*;


public class DetectView extends FrameLayout
{
    private DetectSurfaceView m_detectsurfaceview;
    private DetectFinderView m_detectfinderview;
    private boolean m_bIsNeedMainObject;
    private boolean m_bIsNeedDescription;

    public DetectView(Context context)
    {
        super(context);

        initView(context);
    }

    public DetectView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        initView(context);
    }

    public DetectView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        initView(context);
    }

    private void initView(Context context)
    {
        m_detectsurfaceview = new DetectSurfaceView(context);
        m_detectfinderview = new DetectFinderView(context);
        m_bIsNeedMainObject = true;
        m_bIsNeedDescription = true;

        MainObjectDetectListener mainobjectdetectlistener = new MainObjectDetectListener()
        {
            @Override
            public void onResultDetected(MainObjectDetectResult mainobjectdetectresult)
            {
                if (mainobjectdetectresult != null)
                {
                    if (m_bIsNeedMainObject)
                    {
                        MainObjectResultInfo mainobjectresultinfo = mainobjectdetectresult.getResultInfo();
                        m_detectfinderview.setFinderLocation(mainobjectresultinfo);
                    }
                }
                else
                    m_detectfinderview.setFinderLocation(null);
            }
        };

        DetectListener detectlistenerDescription = new DetectListener()
        {
            @Override
            public void onResultDetected(BaseDetectResult basedetectresult)
            {
                if (m_bIsNeedDescription)
                {
                    try
                    {
                        if (basedetectresult != null)
                        {
                            JSONObject jsonObject = basedetectresult.getJSON();

                            if (basedetectresult.getJSON() != null)
                            {
                                if (jsonObject.has("result"))
                                {
                                    JSONArray jsonarrayResult = jsonObject.getJSONArray("result");
                                    m_detectfinderview.setDescription(jsonarrayResult);
                                }
                            }
                        }
                        else
                            m_detectfinderview.setDescription(null);

                    }
                    catch (Exception exception)
                    {
                        exception.printStackTrace();
                    }
                }
            }
        };

        m_detectsurfaceview.setMainObjectDetectListener(mainobjectdetectlistener);
        m_detectsurfaceview.setDescriptionDetectListener(detectlistenerDescription);

        addView(m_detectsurfaceview);
        addView(m_detectfinderview);
    }

    public void setInterval(int n)
    {
        m_detectsurfaceview.setInterval(n);
    }

    public int getInterval()
    {
        return m_detectsurfaceview.getInterval();
    }

    public void setTimeOut(int n)
    {
        m_detectsurfaceview.setTimeOut(n);
    }

    public int getTimeOut()
    {
        return m_detectsurfaceview.getTimeOut();
    }

    public void setIsShowResult(boolean b)
    {
        m_detectsurfaceview.setIsShowResult(b);
    }

    public boolean getIsShowResult()
    {
        return m_detectsurfaceview.getIsShowResult();
    }

    public void setIsShowArea(boolean b)
    {
        m_detectsurfaceview.setIsShowArea(b);
    }

    public boolean getIsShowArea()
    {
        return m_detectsurfaceview.getIsShowArea();
    }

    public void setIsShowDescription(boolean b)
    {
        m_detectsurfaceview.setIsShowDescription(b);
    }

    public boolean getIsShowDescription()
    {
        return m_detectsurfaceview.getIsShowDescription();
    }

    public void setResultDetectedListener(DetectListener resultdetectedlistener)
    {
        m_detectsurfaceview.setResultDetectListener(resultdetectedlistener);
    }

    public DetectListener getResultDetectedListener()
    {
        return m_detectsurfaceview.getResultDetectListener();
    }

    public void setOptions(HashMap<String, String> hashmap)
    {
        m_detectsurfaceview.setOptions(hashmap);
    }

    public void setNeedMainObject(boolean b)
    {
        m_bIsNeedMainObject = b;
    }

    public void setNeedDescription(boolean b)
    {
        m_bIsNeedDescription = b;
    }

    public HashMap<String, String> getOptions()
    {
        return m_detectsurfaceview.getOptions();
    }

    public void setDetectClass(Class clazz)
    {
        m_detectsurfaceview.setDetectClass(clazz);
    }

    public Class getDetectClass()
    {
        return m_detectsurfaceview.getDetectClass();
    }

    public void setAipImageClassify(String strAppId, String strAppKey, String strSecretKey)
    {
        m_detectsurfaceview.setAipImageClassify(strAppId, strAppKey, strSecretKey);
    }
}
