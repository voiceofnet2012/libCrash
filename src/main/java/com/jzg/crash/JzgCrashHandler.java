package com.jzg.crash;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by voiceofnet on 2017/9/6.
 * handle exception and save to /mnt/sdcard/JzgCrash/,use single instance pattern
 */

public class JzgCrashHandler implements Thread.UncaughtExceptionHandler {
    public static final String TAG = JzgCrashHandler.class.getSimpleName();
    private Thread.UncaughtExceptionHandler crashHandler;
    private static JzgCrashHandler instance = new JzgCrashHandler();
    private Context mContext;

    private String basicInfo;//store device info and exception info
    private static final long EXPIRED_TIME = 7*24*60*60*1000;//crash log keep 7 days

    //used to format date time,to be a part of log file name
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss:SSSS");

    private String dataPath;

    private JzgCrashHandler() {}

    public static JzgCrashHandler getInstance() {
        return instance;
    }

    /**
     * @param context context
     */
    public void init(Context context) {
        mContext = context;
        dataPath = mContext.getFilesDir().getAbsolutePath() + File.separator + "JzgCrash";
        File dir = new File(dataPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        crashHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        getDeviceInfo(mContext);// when JzgCrashHandler init get device info only one time
        deleteExpiredCrashLog(dir);
        Log.e(TAG,"init completed");

    }

    /***
     * everytime when JzgCrashHandler init,it will delete all expired log files
     * @param dir
     */
    private void deleteExpiredCrashLog(File dir){
        File[] files = dir.listFiles();
        long now = System.currentTimeMillis();
        if(files.length>0){
            for(File log:files){
                long createTime = log.lastModified();
                if(now-createTime>=EXPIRED_TIME){
                    log.delete();
                }
            }
        }
    }


    /***
     * the core function to handle exception
     * @param thread excaption happend thread
     * @param ex exception
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (ex == null || crashHandler==null) {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
            return;
        }else{
            saveCrashLog(ex);
            String template = mContext.getResources().getString(R.string.exception_toast);
            Toast.makeText(mContext,template,Toast.LENGTH_LONG).show();
            crashHandler.uncaughtException(thread, ex);
        }
    }

    /**
     * get necessary device info
     * @param context context
     */
    public void getDeviceInfo(Context context) {
        StringBuffer sb = new StringBuffer();
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                sb.append("VERSION_NAME:").append(versionName).append("\n");
                sb.append("VERSION_CODE:").append(versionCode).append("\n");
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        sb.append("BRAND:").append(android.os.Build.BRAND).append("\n");
        sb.append("DEVICE:").append(android.os.Build.DEVICE).append("\n");
        sb.append("MANUFACTURER:").append(android.os.Build.MANUFACTURER).append("\n");
        sb.append("MODEL:").append(android.os.Build.MODEL).append("\n");
        sb.append("PRODUCT:").append(android.os.Build.PRODUCT).append("\n");
        sb.append("SERIAL:").append(android.os.Build.SERIAL).append("\n");
        sb.append("SDK_VERSION:").append(android.os.Build.VERSION.SDK_INT+"").append("\n");
        sb.append("RELEASE_VERSION:").append(android.os.Build.VERSION.RELEASE+"").append("\n");
        basicInfo = sb.toString();
    }

    /**
     * save device and crash info into log files
     * @param t
     * @return  file name
     */
    private String saveCrashLog(Throwable t) {
        StringBuffer sb = new StringBuffer(basicInfo);
        Writer writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        t.printStackTrace(pw);
        Throwable cause = t.getCause();
        while (cause != null) {
            cause.printStackTrace(pw);
            cause = cause.getCause();
        }
        pw.close();
        String crashInfo = writer.toString();
        sb.append(crashInfo);
        String time = sdf.format(new Date());
        String logName = "Crash-" + time+ ".log";
        try {
            FileOutputStream fos = new FileOutputStream(dataPath +File.separator+ logName);
            fos.write(sb.toString().getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return logName;
    }
}
