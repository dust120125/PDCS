package tw.idv.poipoi.pdcs.user;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

import tw.idv.poipoi.pdcs.CareService;
import tw.idv.poipoi.pdcs.net.Callback;
import tw.idv.poipoi.pdcs.net.URLConnectRunner;

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
    private boolean login;
    private ArrayList<UserCallbacks> mCallbacks;

    private String userID;
    private String password;
    private String loginTime;

    private Gson gson;

    public User(){
        mCallbacks = new ArrayList<>();
        gson = new Gson();
    }

    public static User getInstance(){
        if (mUser == null){
            mUser = new User();
        }
        return mUser;
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

    public void addListener(UserCallbacks callbacks){
        if (!mCallbacks.contains(callbacks))
            mCallbacks.add(callbacks);
    }

    public void removeListener(UserCallbacks callbacks){
        mCallbacks.remove(callbacks);
    }

    public String login(String email, String password, String androidID) {
        String result = null;
        try {
            URL url = new URL("http://www.poipoi.idv.tw/android_login/Login2.php");
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
                this.userID = email;
                this.password = password;
                this.loginTime = result.split(" ")[1];
                setLogin(true);
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public String register(String email, String password) {
        String result = null;
        try {
            URL url = new URL("http://www.poipoi.idv.tw/android_login/Register2.php");
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

    public boolean checkLogin(){
        if (userID == null || password == null || loginTime == null || CareService.android_id == null){
            return false;
        }

        String post = "userID=" + userID +
                "&loginTime=" + loginTime +
                "&android_id=" + CareService.android_id;

        new URLConnectRunner("http://www.poipoi.idv.tw/android_login/CheckLogin2.php",
                post, Charset.defaultCharset(),
                new Callback() {
                    @Override
                    public void runCallback(Object... params) {
                        String result = params[0].toString();
                        if (result.startsWith(LOGIN_OR_REGISTER_SUCCESS)){
                            setLogin(true);
                        } else {
                            onLoginFail(result);
                        }
                    }
                });
        return true;
    }

    public boolean serverService(String url, String[] post){
        if (userID == null || password == null || loginTime == null || CareService.android_id == null){
            return false;
        }

        String postStr = "userID=" + userID +
                "&loginTime=" + loginTime +
                "&android_id=" + CareService.android_id;

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
                        Response response = gson.fromJson(result, Response.class);
                        onReceive(response);
                    }
                });
        return true;
    }

    @Override
    public void onLogin() {
        for (UserCallbacks callback : mCallbacks) {
            callback.onLogin();
        }
    }

    @Override
    public void onLoginFail(String error) {
        for (UserCallbacks callback : mCallbacks) {
            callback.onLoginFail(error);
        }
    }

    @Override
    public void onLogout() {
        for (UserCallbacks callback : mCallbacks) {
            callback.onLogout();
        }
    }

    @Override
    public void onReceive(Response response) {
        for (UserCallbacks callback : mCallbacks) {
            callback.onReceive(response);
        }
    }
}
