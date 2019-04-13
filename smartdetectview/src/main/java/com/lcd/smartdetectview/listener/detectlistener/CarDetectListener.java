package com.lcd.smartdetectview.listener.detectlistener;

import entity.detectresult.BaseDetectResult;
import entity.detectresult.CarDetectResult;

public abstract class CarDetectListener implements DetectListener
{
    @Override
    public void onResultDetected(BaseDetectResult basedetectresult)
    {
        CarDetectResult cardetectresult = (CarDetectResult) basedetectresult;
        onResultDetected(cardetectresult);
    }

    public abstract void onResultDetected(CarDetectResult cardetectresult);
}
