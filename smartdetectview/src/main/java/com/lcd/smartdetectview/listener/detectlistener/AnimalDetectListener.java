package com.lcd.smartdetectview.listener.detectlistener;

import entity.detectresult.AnimalDetectResult;
import entity.detectresult.BaseDetectResult;

public abstract class AnimalDetectListener implements DetectListener
{
    @Override
    public void onResultDetected(BaseDetectResult basedetectresult)
    {
        AnimalDetectResult animaldetectresult = (AnimalDetectResult) basedetectresult;
        onResultDetected(animaldetectresult);
    }

    public abstract void onResultDetected(AnimalDetectResult animaldetectresult);
}
