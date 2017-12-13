package tw.idv.poipoi.pdcs.user;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;

import tw.idv.poipoi.pdcs.CareService;
import tw.idv.poipoi.pdcs.Core;
import tw.idv.poipoi.pdcs.Setting;
import tw.idv.poipoi.pdcs.net.Callback;
import tw.idv.poipoi.pdcs.net.URLConnectRunner;
import tw.idv.poipoi.pdcs.properties.Config;

/**
 * Created by DuST on 2017/5/28.
 */

public class User implements UserCallbacks{

    public static final String LOGIN_OR_REGISTER_SUCCESS = "success";
    public static final String USER_NO_FOUND = "user_nofound";
    public static final String WORNG_PASSWORD = "password_worng";
    public static final String USER_ALREADY_EXISTED = "user_existed";
    public static final String EMAIL_INVALID = "userID_invalid";

    private static User mUser;
    public static UserStatus Status;
    private boolean login = false;
    private boolean checkedLogin = false;
    private ArrayList<UserCallbacks> mCallbacks;

    private String userID;
    private String password;
    private String loginTime;

    private Gson gson;

    public User(){
        mCallbacks = new ArrayList<>();
        Status = new UserStatus();
        gson = new Gson();
    }

    public static User getInstance(){
        if (mUser == null){
            mUser = new User();
        }
        return mUser;
    }

    public void loadConfig(Config config){
        userID = config.getUserId();
        password = config.getUserPw();
        loginTime = config.getLoginTime();
    }

    public String getUserID() {
        return userID;
    }

    public boolean isLogin() {
        return login;
    }

    public void setLogin(boolean login) {
        this.login = login;
        if (login){
            onLogin();
        } else {
            onLogout();
        }
    }

    public boolean isCheckedLogin() {
        return checkedLogin;
    }

    public void addListener(UserCallbacks callbacks){
        if (!mCallbacks.contains(callbacks))
            mCallbacks.add(callbacks);
    }

    public void removeListener(UserCallbacks callbacks){
        mCallbacks.remove(callbacks);
    }

    public synchronized void updateFirebaseToken(){
        String token = FirebaseInstanceId.getInstance().getToken();
        if (token != null){
            serverService(Setting.SERVER_DOMAIN + "android_login/UpdateFcmToken.php",
                    new String[]{"token=" + token});
        }
    }

    public void loginSuccess(String email, String password, String loginTime){
        this.userID = email;
        this.password = password;
        this.loginTime = loginTime;
        Config config = CareService.getInstance().getConfig();
        config.setUserId(userID);
        config.setUserPw(password);
        config.setLoginTime(loginTime);
        Core.saveConfig();
        setLogin(true);
    }

    public String login(String email, String password, String androidID) {
        String result = null;
        try {
            URL url = new URL(Setting.SERVER_DOMAIN + "android_login/Login2.php");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            String post = "userID=" + email + "&passWord=" + password + "&android_id=" + androidID;

            con.setRequestMethod("POST");
            con.setDoOutput(true);
            DataOutputStream dos = new DataOutputStream(con.getOutputStream());
            dos.writeBytes(post);
            dos.flush();
            dos.close();

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            StringWriter sw = new StringWriter();
            String tmp;
            while ((tmp = br.readLine()) != null) {
                sw.write(tmp);
            }
            result = sw.toString();
            //Remove UTF-8 BOM header
            if (result.charAt(0) == 65279) result = result.substring(1);

            if (result.startsWith(LOGIN_OR_REGISTER_SUCCESS)) {
                loginSuccess(email, password, result.split(" ")[1]);
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public String register(String email, String password) {
        String result = null;
        try {
            URL url = new URL(Setting.SERVER_DOMAIN + "android_login/Register2.php");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            String post = "userID=" + email + "&passWord=" + password;

            con.setRequestMethod("POST");
            con.setDoOutput(true);
            DataOutputStream dos = new DataOutputStream(con.getOutputStream());
            dos.writeBytes(post);
            dos.flush();
            dos.close();

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            StringWriter sw = new StringWriter();
            String tmp;
            while ((tmp = br.readLine()) != null) {
                sw.write(tmp);
            }
            result = sw.toString();
            //Remove UTF-8 BOM header
            if (result.charAt(0) == 65279) result = result.substring(1);

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public synchronized boolean checkLogin(){
        if (checkedLogin) return false;
        if (userID == null || password == null || loginTime == null || Core.android_id == null){
            return false;
        }

        String post = "userID=" + userID +
                "&loginTime=" + loginTime +
                "&android_id=" + Core.android_id;

        checkedLogin = true;
        new URLConnectRunner(Setting.SERVER_DOMAIN + "android_login/CheckLogin2.php",
                post, Charset.defaultCharset(),
                new Callback() {
                    @Override
                    public void runCallback(Object... params) {
                        String result = params[0].toString();
                        if (result.startsWith(LOGIN_OR_REGISTER_SUCCESS)){
                            onCheckedLogin(true);
                        } else {
                            onCheckedLogin(false);
                        }
                    }
                });
        return true;
    }

    public boolean serverService(String url, String[] post){
        if (userID == null || password == null || loginTime == null || Core.android_id == null){
            return false;
        }

        String postStr = "userID=" + userID +
                "&loginTime=" + loginTime +
                "&android_id=" + Core.android_id;

        if (post != null) {
            StringBuilder stringBuilder = new StringBuilder();
            for (String s : post) {
                stringBuilder.append("&");
                stringBuilder.append(s);
            }
            postStr += stringBuilder.toString();
        }

        URLConnectRunner urlConnectRunner =
                new URLConnectRunner(url, postStr, Charset.defaultCharset(), new Callback() {
                    @Override
                    public void runCallback(Object... params) {
                        String result = params[0].toString();
                        Response response = null;
                        try {
                            response = gson.fromJson(result, Response.class);
                        } catch (RuntimeException ex){
                            ex.printStackTrace();
                        }
                        if (response != null) {
                            onReceive(response);
                        }
                    }
                });
        return true;
    }

    @Override
    public void onLogin() {
        Log.i("User", "Login Success");
        updateFirebaseToken();
        for (UserCallbacks callback : new LinkedList<>(mCallbacks)) {
            callback.onLogin();
        }
    }

    @Override
    public void onLoginFail(String error) {
        Log.i("User", "Login Fail");
        for (UserCallbacks callback : new LinkedList<>(mCallbacks)) {
            callback.onLoginFail(error);
        }
    }

    @Override
    public void onLogout() {
        Log.i("User", "Logout");
        for (UserCallbacks callback : new LinkedList<>(mCallbacks)) {
            callback.onLogout();
        }
    }

    @Override
    public void onReceive(Response response) {
        for (UserCallbacks callback : new LinkedList<>(mCallbacks)) {
            callback.onReceive(response);
        }
    }

    @Override
    public void onCheckedLogin(boolean login) {
        Log.i("User", "CheckedLogin: " + login);
        for (UserCallbacks callback : new LinkedList<>(mCallbacks)) {
            callback.onCheckedLogin(login);
        }
        if (login){
            setLogin(true);
        }
    }

}
