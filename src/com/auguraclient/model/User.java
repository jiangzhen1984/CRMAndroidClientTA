package com.auguraclient.model;

import org.json.JSONObject;

public class User implements ProjectJSONParser {

    private String mSessionID;

    private String userName;

    private String useID;

    private String userLanguage;

    public String getmSessionID() {
        return mSessionID;
    }

    public void setmSessionID(String mSessionID) {
        this.mSessionID = mSessionID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUseID() {
        return useID;
    }

    public void setUseID(String useID) {
        this.useID = useID;
    }

    public String getUserLanguage() {
        return userLanguage;
    }

    public void setUserLanguage(String userLanguage) {
        this.userLanguage = userLanguage;
    }

    public void parser(JSONObject jsonObject) throws JSONParserException {

    }





}
