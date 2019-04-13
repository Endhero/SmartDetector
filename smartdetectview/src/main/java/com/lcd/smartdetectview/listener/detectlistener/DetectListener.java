package com.lcd.smartdetectview.listener.detectlistener;


import com.lcd.smartdetectview.listener.BaseListener;

import entity.detectresult.BaseDetectResult;

public interface DetectListener extends BaseListener
{
    void onResultDetected(BaseDetectResult basedetectresult);
}
