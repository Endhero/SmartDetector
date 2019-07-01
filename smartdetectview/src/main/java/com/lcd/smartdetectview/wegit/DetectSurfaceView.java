package com.lcd.smartdetectview.wegit;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.lcd.smartdetectview.Utils.TransUtil;
import com.lcd.smartdetectview.listener.detectlistener.*;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import detector.AnimalDetector;
import detector.CarDetector;
import detector.DetectorFactory;
import detector.GeneralObjectDetector;
import detector.MainObjectDetector;
import detector.PlantDetector;
import entity.detectresult.BaseDetectResult;
import entity.detectresult.MainObjectDetectResult;
import imageclassify.AipImageClassify;

import static android.content.Context.SENSOR_SERVICE;

public class DetectSurfaceView extends SurfaceView implements SurfaceHolder.Callback,
        Camera.AutoFocusCallback, Camera.PreviewCallback, SensorEventListener
{
    private static final int DETECT_TYPE_INNER = 1;//内部主体检测
    private static final int DETECT_TYPE_OUTER = 2;//外部检测

    private Context m_context;
    private SurfaceHolder m_surfaceholder;
    private Camera m_camera;
    private Boolean m_bNeedDetect;
    private Handler m_hander;
    private int m_nInterval;
    private BaseDetectResult m_basedetectresult;
    private Class m_class;
    private HashMap<String, String> m_hashmapOptions;
    private MainObjectDetectListener m_mainobjectdetectlistener;
    private DetectListener m_detectlistenerResult;
    private DetectListener m_detectlistenerDescription;
    private List<DetectListener> m_listDetectListener;
    private long m_lCurrentTime;
    private ExecutorService m_executerservice;
    private SensorManager m_sensormanager;
    private float[] m_fGravity = new float[3];//Android重力加速度传感器数据去噪
    private boolean m_bIsMoving;
    private int m_nTimeOut;
    private boolean m_bIsShowResult;
    private boolean m_bIsShowArea;
    private boolean m_bIsShowDescription;

    public DetectSurfaceView(Context context)
    {
        super(context);

        init(context);
    }

    public DetectSurfaceView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        init(context);
    }

    public DetectSurfaceView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    @Override
    public void onAutoFocus(boolean bSuccess, Camera camera)
    {
        if (bSuccess)
        {
            m_camera.cancelAutoFocus();
            m_bNeedDetect = true;

            if (m_sensormanager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null)
            {
                m_hander.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        m_camera.autoFocus(DetectSurfaceView.this);
                    }
                }, m_nInterval);
            }
        }
        else
        {
            Toast.makeText(getContext(),"不支持自动对焦", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onSensorChanged(SensorEvent sensorvent)
    {
        switch (sensorvent.sensor.getType())
        {
            case Sensor.TYPE_ACCELEROMETER:

                float f = 0.8f;//0.8有效率

                m_fGravity[0] = f * m_fGravity[0] + (1 - f) * sensorvent.values[0];
                m_fGravity[1] = f * m_fGravity[1] + (1 - f) * sensorvent.values[1];
                m_fGravity[2] = f * m_fGravity[2] + (1 - f) * sensorvent.values[2];

                float fX = Math.abs(sensorvent.values[0] - m_fGravity[0]);
                float fY = Math.abs(sensorvent.values[1] - m_fGravity[1]);
                float fZ = Math.abs(sensorvent.values[2] - m_fGravity[2]);

                //获取当前时间戳
                if (System.currentTimeMillis() -  m_lCurrentTime >= m_nInterval)
                {
                    if ((fX + fY + fZ) / 3 < 0.3 && m_bIsMoving)
                    {
                        //相对静止
                        m_bIsMoving = false;
                        m_camera.autoFocus(this);
                        m_lCurrentTime = System.currentTimeMillis();
                        Log.d("Detect", "相对静止");
                    }
                    else if ((fX + fY + fZ) / 3 > 0.3 && !m_bIsMoving)
                    {
                        //相对加速
                        m_bIsMoving = true;
                        m_bNeedDetect = false;

                        if (!m_executerservice.isShutdown())
                        {
                            m_executerservice.shutdownNow();
                            m_executerservice = Executors.newSingleThreadExecutor();
                            System.gc();
                        }

                        if (m_mainobjectdetectlistener != null)
                            m_mainobjectdetectlistener.onResultDetected(null);

                        if (m_detectlistenerDescription != null)
                            m_detectlistenerDescription.onResultDetected(null);

                        Log.d("Detect", "相对加速");
                    }
                }

                break;
        }
    }

    //传感器精度变化
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera)
    {
        try
        {
            //data默认NV21格式，需要转换成Yuv格式才能提供给百度AI
            if (m_bNeedDetect)
            {
                Camera.Size size = camera.getParameters().getPreviewSize();

                if (m_mainobjectdetectlistener != null && m_bIsShowArea)
                {
                    HashMap<String, String> hashmapOptions = new HashMap<String, String>();
                    hashmapOptions.put("with_face", "0");

                    byte[] bRotate = null;

                    //竖屏需要先将图片旋转90度以正确获取图像主体区域
                    if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
                        bRotate = TransUtil.rotateYUV420Degree90(data, size.width, size.height);

                    else
                    {
                        bRotate = data;
                    }

                    MainObjectDetectResult mainobjectdetectresult = new MainObjectDetectResult();
                    detect(mainobjectdetectresult, m_mainobjectdetectlistener, DETECT_TYPE_INNER, MainObjectDetector.class, TransUtil.Nv21toYuv(bRotate, size.width, size.height), hashmapOptions);
                }

                if (m_class != null && (m_bIsShowResult || m_bIsShowDescription))
                {
                    HashMap<String, String> hashmapOptions = new HashMap<String, String>();

                    if (m_hashmapOptions == null)
                        hashmapOptions.put("baike_num", "5");

                    else
                    {
                        hashmapOptions = m_hashmapOptions;
                    }

                    detect(m_basedetectresult, m_detectlistenerResult, DETECT_TYPE_OUTER, m_class, TransUtil.Nv21toYuv(data, size.width, size.height), hashmapOptions);
                }
            }
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        m_executerservice = Executors.newSingleThreadExecutor();
        initCamera(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        initCamera(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        if (m_camera != null)
        {
            m_camera.setPreviewCallback(null);
            m_camera.autoFocus(null);
            m_camera.stopPreview();// 停止预览
            m_camera.release(); // 释放摄像头资源
            m_camera = null;
        }

        if (!m_executerservice.isShutdown())
        {
            m_executerservice.shutdownNow();
            m_executerservice = null;
        }

        if (m_sensormanager != null && m_sensormanager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null)
        {
            m_sensormanager.unregisterListener(this);
            m_sensormanager = null;
        }

        m_hander.removeCallbacksAndMessages(null);
    }

    public void setInterval(int n)
    {
        m_nInterval = n;
    }

    public int getInterval()
    {
        return m_nInterval;
    }

    public void setTimeOut(int n)
    {
        m_nTimeOut = n;
    }

    public int getTimeOut()
    {
        return m_nTimeOut;
    }

    public void setIsShowResult(boolean b)
    {
        m_bIsShowResult = b;
    }

    public boolean getIsShowResult()
    {
        return m_bIsShowResult;
    }

    public void setIsShowArea(boolean b)
    {
        m_bIsShowArea = b;
    }

    public boolean getIsShowArea()
    {
        return m_bIsShowArea;
    }

    public void setIsShowDescription(boolean b)
    {
        m_bIsShowDescription = b;
    }

    public boolean getIsShowDescription()
    {
        return m_bIsShowDescription;
    }

    public void setOptions(HashMap<String, String> hashmap)
    {
        m_hashmapOptions = hashmap;
    }

    public HashMap<String, String> getOptions()
    {
        return m_hashmapOptions;
    }

    public Class getDetectClass()
    {
        return m_class;
    }

    public void setMainObjectDetectListener(MainObjectDetectListener mainobjectdetectlistener)
    {
        m_mainobjectdetectlistener = mainobjectdetectlistener;
    }

    public MainObjectDetectListener getMainObjectListener()
    {
        return m_mainobjectdetectlistener;
    }

    public void setResultDetectListener(DetectListener resultdetectedlistener)
    {
        if (resultdetectedlistener instanceof  AnimalDetectListener)
            m_class = AnimalDetector.class;

        if (resultdetectedlistener instanceof GeneralObjectDetectListener)
            m_class = GeneralObjectDetector.class;

        if (resultdetectedlistener instanceof PlantDetectListener)
            m_class = PlantDetector.class;

        if (resultdetectedlistener instanceof CarDetectListener)
            m_class = CarDetector.class;

        if (resultdetectedlistener instanceof  MainObjectDetectListener)
            m_class = MainObjectDetector.class;

        m_detectlistenerResult = resultdetectedlistener;
    }

    public DetectListener getResultDetectListener()
    {
        return m_detectlistenerResult;
    }

    public void setDescriptionDetectListener(DetectListener descriptionlistener)
    {
        m_detectlistenerDescription = descriptionlistener;
    }

    public DetectListener getDescriptionDetectListener()
    {
        return m_detectlistenerDescription;
    }

    private void init(Context context)
    {
        m_hander = new Handler();
        m_context = context;
        m_surfaceholder = getHolder();
        m_surfaceholder.addCallback(this);
        m_bIsMoving = true;//初始化为移动状态，防止在静止状态下启用不对焦
        m_bIsShowResult = true;
        m_bIsShowArea = false;//关闭可减少检测等待时间
        m_bIsShowDescription = true;
        m_bNeedDetect = false;
        m_nTimeOut = 1000;
        m_nInterval = 1000;
        m_sensormanager = (SensorManager) m_context.getSystemService(SENSOR_SERVICE);
    }

    private void initCamera(SurfaceHolder holder)
    {
        try
        {
            if (m_camera == null)
            {
                if (Camera.getNumberOfCameras() < 1)
                {
                    Toast.makeText(m_context,"不支持拍照", Toast.LENGTH_LONG).show();
                    return;
                }

                m_camera = Camera.open(0);

                Camera.Parameters parameters = m_camera.getParameters();

                //自动对焦
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

                //照片竖屏
                parameters.set("orientation", "portrait");
                parameters.set("rotation", 90);

                m_camera.setParameters(parameters);

                //预览方向
                if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
                    m_camera.setDisplayOrientation(90);

                else
                    m_camera.setDisplayOrientation(0);

                //开启预览
                m_camera.setPreviewDisplay(holder);
                m_camera.startPreview();
                //设置预览回调
                m_camera.setPreviewCallback(this);

                //注册传感器(加速度)
                if (m_sensormanager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null)
                {
                    m_sensormanager.registerListener(this, m_sensormanager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
                    m_lCurrentTime = System.currentTimeMillis();
                }
                else
                {
                    //设置对焦回调，让Camera进行自动对焦，调用onAutoFocus，但必须在开启预览之后才能设置
                    m_camera.autoFocus(this);
                    Toast.makeText(getContext(), "没有加速度传感器,定时对焦", Toast.LENGTH_SHORT).show();
                }
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    private void detect(BaseDetectResult basedetectresult, DetectListener detectlistener, int nType, Class clazz, byte[] b, HashMap<String ,String> hashmap) throws Exception
    {
        if (AipImageClassify.getAipImageClassify() == null)
            return;

        DetectThread detectthread = new DetectThread(basedetectresult, detectlistener, nType, clazz, b, hashmap);

        m_executerservice.execute(detectthread);
    }

    class DetectThread extends Thread
    {
        private BaseDetectResult m_basedetectresult;
        private Class m_class;
        private byte[] m_byte;
        private HashMap<String, String> m_hashmapOptions;
        private DetectListener m_detectlistener;
        private int m_nType;

        public DetectThread(BaseDetectResult basedetectresult, DetectListener detectListener, int nType, Class clazz, byte[] bData, HashMap<String, String> hashmap)
        {
            m_basedetectresult = basedetectresult;
            m_class = clazz;
            m_byte = bData;
            m_hashmapOptions = hashmap;
            m_detectlistener =detectListener;
            m_nType = nType;
        }

        @Override
        public void run()
        {
            try
            {
                if (m_bNeedDetect)
                {
                    m_basedetectresult = null;

                    long lTime = System.currentTimeMillis();
                    Log.d("Detect", "DetectStart:" + m_nType + " " + System.currentTimeMillis());
                    m_basedetectresult = DetectorFactory.createDetector(m_class, m_byte, m_hashmapOptions).getDetectResult();

                    while (m_basedetectresult == null)
                    {
                        if (System.currentTimeMillis() - lTime <= m_nTimeOut)
                            Thread.sleep(50);

                        else
                        {
                            Toast.makeText(m_context, "Detect TimeOut", Toast.LENGTH_SHORT).show();

                            break;
                        }
                    }

                    if (m_detectlistener != null && m_basedetectresult != null)
                    {
                        Log.d("Detect", "DetectFinish:" + m_nType + " " + System.currentTimeMillis());

                        if (m_nType == DETECT_TYPE_OUTER)
                        {
                            m_bNeedDetect = false;

                            if (m_bIsShowResult)
                                m_detectlistener.onResultDetected(m_basedetectresult);

                            if (m_detectlistenerDescription != null && m_bIsShowDescription)
                                m_detectlistenerDescription.onResultDetected(m_basedetectresult);

                            //阻止线程池执行其它任务，并清空线程池
                            if (!m_executerservice.isShutdown())
                            {
                                m_executerservice.shutdownNow();
                                m_executerservice = Executors.newSingleThreadExecutor();
                                System.gc();//提醒系统进行垃圾回收
                            }
                        }
                        else
                            m_detectlistener.onResultDetected(m_basedetectresult);
                    }
                }
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }
    }
}
