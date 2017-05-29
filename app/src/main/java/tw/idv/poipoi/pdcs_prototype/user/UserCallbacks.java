package tw.idv.poipoi.pdcs_prototype.user;

/**
 * Created by DuST on 2017/5/29.
 */

public interface UserCallbacks {

    void onLogin();

    void onLoginFail(String error);

    void onLogout();

    void onReceive(Response response);

}
