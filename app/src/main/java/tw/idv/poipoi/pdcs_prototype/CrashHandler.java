package tw.idv.poipoi.pdcs_prototype;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by DuST on 2017/5/27.
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static final boolean DELETE_LOGS = false;

    private Context appContext;
    private String logFileName;
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    public CrashHandler(Context appContext, String logFileName) {
        this.logFileName = logFileName;
        this.appContext = appContext;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        processLogFiles();
    }

    private void processLogFiles(){
        File dir = appContext.getFilesDir();
        for (String s : dir.list()) {
            System.out.println("Root dir files: " + s);
            if (s.startsWith("AppCrash")){
                if (!DELETE_LOGS) {
                    Log.i("CrashLog: ", "-");
                    Log.i("CrashLog: ", s);
                    try {
                        FileInputStream fis = appContext.openFileInput(s);
                        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                        String tmp;
                        while ((tmp = br.readLine()) != null) {
                            Log.i("CrashLog: ", tmp);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.i("CrashLog: ", s + " end");
                    Log.i("CrashLog: ", "-");
                } else {
                    appContext.getFileStreamPath(s).delete();
                }
            }
        }
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex)){
            mDefaultHandler.uncaughtException(thread, ex);
        }
        mDefaultHandler.uncaughtException(thread, ex);
    }

    private boolean handleException(Throwable ex){
        if (ex == null) return false;
        StackTraceElement[] stackTraceElements = ex.getStackTrace();
        Toast.makeText(appContext, ex.getMessage(), Toast.LENGTH_LONG).show();
        String fileName = logFileName + "-" + System.currentTimeMillis() + ".log";
        try {
            FileOutputStream fos = appContext.openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(ex.toString().getBytes());
            fos.write("\n".getBytes());
            fos.write(ex.getMessage().getBytes());
            fos.write("\n".getBytes());
            for (StackTraceElement element : stackTraceElements) {
                fos.write(element.toString().getBytes());
                fos.write("\n".getBytes());
            }
            fos.flush();
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
