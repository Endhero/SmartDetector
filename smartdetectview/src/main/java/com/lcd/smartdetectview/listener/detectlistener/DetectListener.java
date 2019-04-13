package com.lcd.smartdetectview.listener.detectlistener;


import com.lcd.mylibrary.listener.BaseListener;

import entity.detectresult.BaseDetectResult;

public interface DetectListener extends BaseListener
{
    void onResultDetected(BaseDetectResult basedetectresult);
}
