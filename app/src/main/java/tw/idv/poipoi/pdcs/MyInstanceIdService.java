package tw.idv.poipoi.pdcs;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import tw.idv.poipoi.pdcs.user.User;

/**
 * Created by DuST on 2017/5/29.
 */

public class MyInstanceIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d("Firebase", "Token: " + token);
        Log.i("Thread", "FirebaseId: " + Thread.currentThread().getId());
        User.getInstance().serverService("http://www.poipoi.idv.tw/android_login/UpdateFcmToken.php",
                new String[]{"token=" + token});
    }
}
