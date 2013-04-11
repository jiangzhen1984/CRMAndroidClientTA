package com.auguraclient.model;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Project implements ProjectJSONParser {

    private String id;

    private String name;

    private String text;

    private List<ProjectItem> projectItemList;


    public Project() {
        this.projectItemList =  new ArrayList<ProjectItem>();
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void parser(JSONObject jsonObject) throws JSONParserException {
        // TODO Auto-generated method stub

    }

    public void addProject(ProjectItem pi) {
        this.projectItemList.add(pi);
        pi.setProject(this);
    }



}
