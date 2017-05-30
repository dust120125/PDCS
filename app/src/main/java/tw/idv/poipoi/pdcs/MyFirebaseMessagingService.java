package tw.idv.poipoi.pdcs;

import android.content.Intent;
import android.os.Message;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import tw.idv.poipoi.pdcs.user.Response;
import tw.idv.poipoi.pdcs.user.ServerAction;

/**
 * Created by DuST on 2017/5/29.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.i("Firebase", "onMessageReceived: " + remoteMessage.getFrom());
        Log.i("Thread", "FCM_Message: " + Thread.currentThread().getId());
        Response response = new Response();
        response.setAction(ServerAction.RECEIVE_FCM_MESSAGE);
        response.setData(remoteMessage.getData());
        Message msg = Message.obtain();
        msg.what = HandlerCode.FCM_MESSAGE_RECEIVE;
        msg.obj = response;
        Core.CORE.sendMessage(msg);
    }
}
