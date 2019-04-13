package com.lcd.smartdetectview.listener.detectlistener;

import entity.detectresult.BaseDetectResult;
import entity.detectresult.PlantDetectResult;

public abstract class PlantDetectListener implements DetectListener
{
    @Override
    public void onResultDetected(BaseDetectResult basedetectresult)
    {
        PlantDetectResult plantdetectresult = (PlantDetectResult) basedetectresult;
        onResultDetected(plantdetectresult);
    }

    public abstract void onResultDetected(PlantDetectResult plantdetectresult);
}
