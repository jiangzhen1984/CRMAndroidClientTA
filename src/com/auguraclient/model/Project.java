package com.auguraclient.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Project extends AbstractModel implements ProjectJSONParser,Serializable {

    private Integer nID;

    private String id;

    private String name;

    private String text;

    private boolean isLoadOrderFromDB;
    
    private boolean isNeededUpdate;

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
    
    public ProjectOrder getOrder(String id) {
        if (id ==null || id.equals("") ) {
            return null;
        }
        for(int i=0;i<projectOrderList.size(); i++) {
            ProjectOrder pi = projectOrderList.get(i);
            if(pi.getId().equals(id)) {
            	return pi;
            }
        }
        return null;
    }


    public List<ProjectOrder> getProjectOrderList() {
		return projectOrderList;
	}


	public int getOrderCount() {
        return projectOrderList.size();
    }


    public Integer getnID() {
        return nID;
    }


    public void setnID(Integer nID) {
        this.nID = nID;
    }


    public boolean isLoadOrderFromDB() {
        return isLoadOrderFromDB;
    }


    public void setLoadOrderFromDB(boolean isLoadOrderFromDB) {
        this.isLoadOrderFromDB = isLoadOrderFromDB;
    }


    public JSONArray toJSONArray()  throws JSONParserException {
        return null;
    }


	public boolean isNeededUpdate() {
		return isNeededUpdate;
	}


	public void setNeededUpdate(boolean isNeededUpdate) {
		this.isNeededUpdate = isNeededUpdate;
	}

    
    
}
