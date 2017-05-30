package tw.idv.poipoi.pdcs.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import tw.idv.poipoi.pdcs.HandlerCode;
import tw.idv.poipoi.pdcs.MainActivity;
import tw.idv.poipoi.pdcs.R;
import tw.idv.poipoi.pdcs.database.OnDataChangedListener;
import tw.idv.poipoi.pdcs.user.User;
import tw.idv.poipoi.pdcs.user.friend.Friend;

import static tw.idv.poipoi.pdcs.Core.CORE;
import static tw.idv.poipoi.pdcs.Core.getMainContext;
import static tw.idv.poipoi.pdcs.HandlerCode.FRIEND_LIST_DATA_CHANGED;

/**
 * Created by DuST on 2017/5/31.
 */

public class FriendListHandler extends BaseAdapter implements OnDataChangedListener<Friend> {

    private LayoutInflater inflater;
    private ArrayList<Friend> mFriendList;
    private ArrayList<Friend> mInviteList;
    private MyHandler mHandler;
    private Activity rootActivity;

    public static class MyHandler extends Handler {

        private FriendListHandler mFriendListHandler;

        public MyHandler(FriendListHandler mFriendListHandler) {
            this.mFriendListHandler = mFriendListHandler;
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == FRIEND_LIST_DATA_CHANGED) {
                mFriendListHandler.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onInsert(Friend data) {
        if (!data.isAgree() && data.getFriendID().equals(data.getInviterID())) {
            mInviteList.add(data);
        } else {
            mFriendList.add(data);
        }
        Message.obtain(mHandler, HandlerCode.FRIEND_LIST_DATA_CHANGED).sendToTarget();
    }

    @Override
    public void onUpdate(Friend data) {
        if (data.getInviterID().equals(data.getFriendID())){
            for (int i = 0; i < mInviteList.size(); i++) {
                Friend friend = mInviteList.get(i);
                if (friend.getFriendID().equals(data.getFriendID())
                        && friend.getInviterID().equals(data.getInviterID())) {
                    mInviteList.remove(i);
                    mFriendList.add(data);
                    break;
                }
            }
        } else {
            for (int i = 0; i < mFriendList.size(); i++) {
                Friend friend = mFriendList.get(i);
                if (friend.getFriendID().equals(data.getFriendID())
                        && friend.getInviterID().equals(data.getInviterID())) {
                    friend.setAgree(data.isAgree());
                    break;
                }
            }
        }
        Message.obtain(mHandler, HandlerCode.FRIEND_LIST_DATA_CHANGED).sendToTarget();
    }

    @Override
    public void onDelete(Friend data) {

        Message.obtain(mHandler, HandlerCode.FRIEND_LIST_DATA_CHANGED).sendToTarget();
    }

    private class ItemHolder {
        TextView userId;
        TextView msg;
        ImageView userIcon;

        public ItemHolder(TextView userId, TextView msg, ImageView userIcon) {
            this.userId = userId;
            this.msg = msg;
            this.userIcon = userIcon;
        }
    }

    public FriendListHandler(Activity activity) {
        rootActivity = activity;
        mFriendList = CORE.getFriendSql().getAll(false);
        mInviteList = CORE.getFriendSql().getInvite();
        mHandler = new MyHandler(this);
    }

    public void setParentList(final ListView list) {
        inflater = LayoutInflater.from(list.getContext());
        list.setAdapter(this);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0
                        || (mInviteList.size() > 0 && position == mInviteList.size() + 1)){
                    return;
                }

                Friend friend = (Friend) getItem(position);
                Log.i("Friend", "friend: " + friend.getFriendID() + ", inviter: " + friend.getInviterID() + ", agreed: " + friend.isAgree());
                if (friend.isAgree()) return;

                if (friend.getFriendID().equals(friend.getInviterID())){
                    showAgreeDialog(friend);
                }
            }
        });
    }

    private void showAgreeDialog(final Friend friend){
        AlertDialog.Builder builder = new AlertDialog.Builder(rootActivity);
        builder.setTitle("同意")
                .setMessage("確定接受 " + friend.getFriendID() + " 的好友邀請？")
                .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton("接受", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        User.getInstance().serverService("http://www.poipoi.idv.tw/android_login/Friend.php?mode=agree",
                                new String[]{"inviterID=" + friend.getInviterID()});
                    }
                });
        builder.show();
    }

    @Override
    public int getCount() {
        int count = mFriendList.size() + mInviteList.size() + 1;
        if (mInviteList.size() > 0) count++;
        return count;
    }

    @Override
    public Object getItem(int position) {
        int index = position - 1;
        //index = mInviteList.size() > 0 ? index - 1 : index;
        if (index < 0) return null;
        Friend friend;
        if ((index + 1) > mInviteList.size()) {
            if (mInviteList.size() > 0) index--;
            friend = mFriendList.get(index - mInviteList.size());
        } else {
            friend = mInviteList.get(index);
        }
        return friend;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ItemHolder holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listitem_friend, null);
            holder = new ItemHolder(
                    (TextView) convertView.findViewById(R.id.textView_userId),
                    (TextView) convertView.findViewById(R.id.textView_friend_subMessage),
                    (ImageView) convertView.findViewById(R.id.imageView_userIcon)
            );
            convertView.setTag(holder);
        } else {
            holder = (ItemHolder) convertView.getTag();
        }

        holder.msg.setVisibility(View.GONE);
        if (position == 0) {
            holder.userIcon.setVisibility(View.GONE);
            holder.userId.setTextSize(14f);
            if (mInviteList.size() > 0) {
                holder.userId.setText("好友邀請");
            } else {
                String tmp = "好友: " + mFriendList.size();
                holder.userId.setText(tmp);
            }
        } else if (mInviteList.size() > 0 && position == mInviteList.size() + 1) {
            holder.userIcon.setVisibility(View.GONE);
            holder.userId.setTextSize(14f);
            String tmp = "好友: " + mFriendList.size();
            holder.userId.setText(tmp);
        } else {
            Friend friend = (Friend) getItem(position);
            holder.userIcon.setVisibility(View.VISIBLE);
            holder.userId.setText(friend.getFriendID());
            holder.userId.setTextSize(20f);
            if (!friend.isAgree()){
                holder.msg.setVisibility(View.VISIBLE);
                if (friend.getInviterID().equals(friend.getFriendID())){
                    holder.msg.setText("點擊接受好友邀請");
                } else {
                    holder.msg.setText("對方未接受邀請");
                }
            }
        }

        return convertView;
    }
}
