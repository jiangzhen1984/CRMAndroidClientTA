package com.auguraclient.model;

import org.json.JSONArray;
import org.json.JSONObject;

public interface ProjectJSONParser {

    public void parser(JSONObject jsonObject) throws JSONParserException;


    public JSONArray toJSONArray()  throws JSONParserException;

}
