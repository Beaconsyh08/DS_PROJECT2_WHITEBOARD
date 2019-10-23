package Client;

public class UserProfile {
    private String userName;
    private boolean isManager;
    public UserProfile(String userName, boolean isManager) {
        this.userName = userName;
        this.isManager = isManager;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    public boolean isManager() {
        return isManager;
    }

    public void setManager(boolean manager) {
        isManager = manager;
    }
}
