package tw.idv.poipoi.pdcs;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by DuST on 2017/5/29.
 */

public class MyInstanceIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d("Firebase", "Token: " + token);
    }
}
