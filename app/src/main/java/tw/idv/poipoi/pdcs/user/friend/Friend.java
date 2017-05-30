package tw.idv.poipoi.pdcs.user.friend;

/**
 * Created by DuST on 2017/5/30.
 */

public class Friend {

    private String friendID;
    private String inviterID;
    private boolean agree;

    public Friend(String friendID, String inviterID) {
        this.friendID = friendID;
        this.inviterID = inviterID;
    }

    public String getFriendID() {
        return friendID;
    }

    public String getInviterID() {
        return inviterID;
    }

    public boolean isAgree() {
        return agree;
    }

    public void setAgree(boolean agree) {
        this.agree = agree;
    }
}
