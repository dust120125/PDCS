package tw.idv.poipoi.pdcs.properties;

import android.content.Context;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import tw.idv.poipoi.pdcs.PropertiesManager;

/**
 * Created by DuST on 2017/5/18.
 */

public class Config {

    private static Config mConfig;

    public static final String NAME = "config.ini";

    private static final String LASTEST_GEO_DATABASE_TIME = "LastestGeoTime";
    private static final String LASTEST_GCO_DATABASE_TIME = "LastestGcoTime";
    private static final String LASTEST_CAP_UPDATE_TIME = "LastestCapTime";

    private static final String USER_ID = "userId";
    private static final String USER_PW = "userPw";
    private static final String LOGIN_TIME = "loginTime";

    private DefaultProperties mProperties;
    private boolean lastestGeo = false;
    private boolean lastestGco = false;

    public Config() {
        mProperties = new DefaultProperties();
        mProperties.NAME = NAME;
    }

    public Config(DefaultProperties mProperties) {
        this.mProperties = mProperties;
    }

    public void setLastestGeoDatabaseTime(Date date) {
        mProperties.put(LASTEST_GEO_DATABASE_TIME, formatDate(date));
    }

    public Date getLastestGeoDatabaseTime() {
        return parseDate((String) mProperties.get(LASTEST_GEO_DATABASE_TIME));
    }

    public void setLastestGcoDatabaseTime(Date date) {
        mProperties.put(LASTEST_GCO_DATABASE_TIME, formatDate(date));
    }

    public Date getLastestGcoDatabaseTime() {
        return parseDate((String) mProperties.get(LASTEST_GCO_DATABASE_TIME));
    }

    public void setLastestCapUpdateTime(Date date) {
        mProperties.put(LASTEST_CAP_UPDATE_TIME, formatDate(date));
    }

    public Date getLastestCapUpdateTime() {
        return parseDate((String) mProperties.get(LASTEST_CAP_UPDATE_TIME));
    }

    private Date parseDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN);
        try {
            return sdf.parse(dateStr);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN);
        return sdf.format(date);
    }

    public String getUserId() {
        return mProperties.getProperty(USER_ID);
    }

    public void setUserId(String userId) {
        mProperties.put(USER_ID, userId);
    }

    public String getUserPw() {
        return mProperties.getProperty(USER_PW);
    }

    public void setUserPw(String userPw) {
        mProperties.put(USER_PW, userPw);
    }

    public String getLoginTime() {
        return mProperties.getProperty(LOGIN_TIME);
    }

    public void setLoginTime(String loginTime) {
        mProperties.put(LOGIN_TIME, loginTime);
    }

    public synchronized boolean save(Context context){
        try {
            PropertiesManager.saveProperties(context, getProperties());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public DefaultProperties getProperties() {
        return mProperties;
    }

}
