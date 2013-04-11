package com.auguraclient.util;

import com.auguraclient.model.Project;
import com.auguraclient.model.ProjectList;
import com.auguraclient.model.User;

public class GlobalHolder {

    private static User currentLogInedUser;

    private static ProjectList pl;


    public synchronized static void setCurrentUser(User user) {
        currentLogInedUser = user;
    }

    public synchronized static User getCrrentUser() {
        return currentLogInedUser;
    }

    public static String getSessionId() {
        if(currentLogInedUser == null) {
            return null;
        }
        return currentLogInedUser.getmSessionID();
    }

    public static ProjectList getPl() {
        return pl;
    }

    public static void setPl(ProjectList pl) {
        GlobalHolder.pl = pl;
    }


    public static Project getProject(int index) {
        if(pl == null || pl.getList() == null || pl.getList().isEmpty() ||  pl.getList().size()< index) {
            return null;
        }

        return pl.getList().get(index);
    }


}
