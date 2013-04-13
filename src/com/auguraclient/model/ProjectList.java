package com.auguraclient.model;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProjectList implements ProjectJSONParser {

    private int resultCount;

    private int totalCount;

    private int nextOffet;

    private List<Project> list;

    public int getResultCount() {
        return resultCount;
    }

    public void setResultCount(int resultCount) {
        this.resultCount = resultCount;
        if(list == null) {
            list= new ArrayList<Project>(resultCount);
        }
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getNextOffet() {
        return nextOffet;
    }

    public void setNextOffet(int nextOffet) {
        this.nextOffet = nextOffet;
    }

    public List<Project> getList() {
        return list;
    }

    public void setList(List<Project> list) {
        this.list = list;
    }


    public void addProject(Project p ) {
        if(list == null || p == null) {
            return ;
        }
        this.list.add(p);
        this.resultCount += 1;
    }

    public void parser(JSONObject jsonObject) throws JSONParserException {
        // TODO Auto-generated method stub

    }




}
