package com.lcd.smartdetectview.listener.detectlistener;

import entity.detectresult.BaseDetectResult;
import entity.detectresult.MainObjectDetectResult;

public abstract class MainObjectDetectListener implements DetectListener
{
    @Override
    public void onResultDetected(BaseDetectResult basedetectresult)
    {
        MainObjectDetectResult mainobjectdetectresult = (MainObjectDetectResult) basedetectresult;

        onResultDetected(mainobjectdetectresult);
    }

    public abstract void onResultDetected(MainObjectDetectResult mainobjectdetectresult);
}
