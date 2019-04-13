package com.lcd.smartdetectview.listener.detectlistener;

import entity.detectresult.BaseDetectResult;
import entity.detectresult.GeneralObjectDetectResult;

public abstract class GeneralObjectDetectListener implements DetectListener
{
    @Override
    public void onResultDetected(BaseDetectResult basedetectresult)
    {
        GeneralObjectDetectResult generalobjectdetectresult = (GeneralObjectDetectResult) basedetectresult;
        onResultDetected(generalobjectdetectresult);
    }

    public abstract void onResultDetected(GeneralObjectDetectResult generalobjectdetectresult);
}
