package tw.idv.poipoi.pdcs_prototype;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import tw.idv.poipoi.pdcs_prototype.properties.DefaultProperties;

/**
 * Created by DuST on 2017/5/18.
 */

public class PropertiesManager {

    private static final String CONFIG_DIR = "config";
    private static HashMap<String, DefaultProperties> propertiesMap = new HashMap<>();

    public static boolean existsProperties(Context context, String fileName){
        return new File(context.getDir(CONFIG_DIR, Context.MODE_PRIVATE).getAbsoluteFile()
                + "/" + fileName).exists();
    }

    public static DefaultProperties getProperties(Context context, String fileName) throws IOException {
        DefaultProperties result;
        if ((result = propertiesMap.get(fileName)) == null){
            result = readProperties(context, fileName);
            propertiesMap.put(fileName, result);
        }
        return result;
    }

    private static DefaultProperties readProperties(Context context, String fileName) throws IOException {
        FileInputStream fis = new FileInputStream(
                context.getDir(CONFIG_DIR, Context.MODE_PRIVATE).getAbsoluteFile() + "/" + fileName);

        DefaultProperties result = new DefaultProperties();
        result.NAME = fileName;
        result.load(fis);
        fis.close();
        return result;
    }

    public static void saveProperties(Context context, DefaultProperties properties) throws IOException {
        FileOutputStream fos = new FileOutputStream(
                context.getDir(CONFIG_DIR, Context.MODE_PRIVATE).getAbsoluteFile() + "/" + properties.NAME);

        properties.store(fos, "");
        fos.close();
    }

}
