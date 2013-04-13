package com.auguraclient.model;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Project implements ProjectJSONParser {

    private Integer nID;

    private String id;

    public Integer getnID() {
        return nID;
    }


    public void setnID(Integer nID) {
        this.nID = nID;
    }


    private String name;

    private String text;

    private List<ProjectOrder> projectOrderList;


    public Project() {
        this.projectOrderList =  new ArrayList<ProjectOrder>();
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

    public void addProjectOrder(ProjectOrder pi) {
        this.projectOrderList.add(pi);
        pi.setProject(this);
    }

    public void addProjectOrder( List<ProjectOrder> list) {
        if (list == null ) {
            return;
        }
        for(int i=0;i<list.size(); i++) {
            ProjectOrder pi = list.get(i);
            this.projectOrderList.add(pi);
            pi.setProject(this);
        }
    }

    public ProjectOrder getOrder(int index) {
        if (index <0 || index > projectOrderList.size() ) {
            return null;
        }
        return projectOrderList.get(index);
    }


    public int getOrderCount() {
        return projectOrderList.size();
    }


}
