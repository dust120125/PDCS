package tw.idv.poipoi.pdcs.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import tw.idv.poipoi.pdcs.user.friend.Friend;

/**
 * Created by DuST on 2017/5/30.
 */

public class FriendSql implements OnDataChangedListener<Friend> {

    private SQLiteDatabase db;
    private static final boolean CLEAN_AT_START = true;

    static final String TABLE_NAME = "friends";

    private static final String FRIEND_ID = "friendID";
    private static final String INVITER_ID = "inviterID";
    private static final String AGREED = "agreed";

    private ArrayList<OnDataChangedListener<Friend>> listeners;

    static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    FRIEND_ID + " TEXT PRIMARY KEY, " +
                    INVITER_ID + " TEXT NOT NULL, " +
                    AGREED + " INTEGER DEFAULT 0);";

    public FriendSql(Context context) {
        this.db = FriendSqlHelper.getDatabase(context);
        if (CLEAN_AT_START){
            db.delete(TABLE_NAME, null, null);
        }
        listeners = new ArrayList<>();
    }

    public boolean insert(Friend friend){
        ContentValues cv = new ContentValues();
        cv.put(FRIEND_ID, friend.getFriendID());
        cv.put(INVITER_ID, friend.getInviterID());
        cv.put(AGREED, friend.isAgree() ? 1 : 0);

        long code = db.insert(TABLE_NAME, null, cv);
        if (code != -1){
            onInsert(friend);
            return true;
        } else {
            return false;
        }
    }

    public boolean update(Friend friend){
        String where = FRIEND_ID + " = '" + friend.getFriendID() + "'" +
                "AND " + INVITER_ID + " = '" + friend.getInviterID() + "'";

        ContentValues cv = new ContentValues();
        cv.put(AGREED, friend.isAgree() ? 1 : 0);

        long code = db.update(TABLE_NAME, cv, where, null);
        if (code > 0){
            onUpdate(friend);
            return true;
        } else {
            return false;
        }
    }

    public Friend getFriend(String friendID){
        String where = FRIEND_ID + " = '" + friendID + "'";
        try(Cursor cursor = db.query(TABLE_NAME, null, where, null, null, null, null)){
            if (cursor.moveToFirst()){
                return getRecord(cursor);
            }
            return null;
        }
    }

    public ArrayList<Friend> getAll(boolean containInvite){
        ArrayList<Friend> list = new ArrayList<>();
        String where = null;
        if (!containInvite){
            where = INVITER_ID + " <> " + FRIEND_ID;
        }
        try(Cursor cursor = db.query(TABLE_NAME, null, where, null, null, null, null)){
            if (cursor.moveToNext()){
                list.add(getRecord(cursor));
            }
            return list;
        }
    }

    public ArrayList<Friend> getAgreed(){
        ArrayList<Friend> list = new ArrayList<>();
        String where = AGREED + " > 0";
        try(Cursor cursor = db.query(TABLE_NAME, null, where, null, null, null, null)){
            if (cursor.moveToNext()){
                list.add(getRecord(cursor));
            }
            return list;
        }
    }

    public ArrayList<Friend> getInvite(){
        ArrayList<Friend> list = new ArrayList<>();
        String where = INVITER_ID + " = " + FRIEND_ID;
        try(Cursor cursor = db.query(TABLE_NAME, null, where, null, null, null, null)){
            if (cursor.moveToNext()){
                list.add(getRecord(cursor));
            }
            return list;
        }
    }

    private Friend getRecord(Cursor cursor){
        String friendID = cursor.getString(0);
        String inviterID = cursor.getString(1);
        boolean agreed = cursor.getInt(2) > 0;

        Friend friend = new Friend(friendID, inviterID);
        friend.setAgree(agreed);
        return friend;
    }

    public void addOnDataChangedListener(OnDataChangedListener<Friend> listener){
        listeners.add(listener);
    }

    public void removeOnDataChangedListener(OnDataChangedListener<Friend> listener){
        listeners.remove(listener);
    }


    @Override
    public void onInsert(Friend data) {
        for (OnDataChangedListener<Friend> listener : new ArrayList<>(listeners)) {
            listener.onInsert(data);
        }
    }

    @Override
    public void onUpdate(Friend data) {
        for (OnDataChangedListener<Friend> listener : new ArrayList<>(listeners)) {
            listener.onUpdate(data);
        }
    }

    @Override
    public void onDelete(Friend data) {
        for (OnDataChangedListener<Friend> listener : new ArrayList<>(listeners)) {
            listener.onDelete(data);
        }
    }
}
