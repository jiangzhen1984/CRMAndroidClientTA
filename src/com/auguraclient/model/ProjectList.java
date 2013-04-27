package com.auguraclient.model;

import org.json.JSONArray;
import org.json.JSONObject;

import com.auguraclient.util.Constants;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ProjectList  extends AbstractModel implements ProjectJSONParser {

    private int resultCount;

    private int totalCount;

    private int nextOffet;

    private List<Project> list;

    public int getResultCount() {
        return list == null? 0: list.size();
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
    }
    
    public void removeProject(Project p) {
    	if(p == null || p.getId()==null || p.getId().equals("")) {
    		throw new RuntimeException(" project id is null ");
    	}
    	for(int i=0;i<this.list.size(); i++) {
    		Project cache = list.get(i);
    		if(cache == null || cache.getId() == null || cache.getId().equals("")) {
    			Log.w(Constants.TAG, " Found invalid project in cache");
    			continue;
    		}
    		if(cache.getId().equals(p.getId())) {
    			list.remove(cache);
    		}
    	}    	
    }

    public void parser(JSONObject jsonObject) throws JSONParserException {
        // TODO Auto-generated method stub

    }

    public JSONArray toJSONArray()  throws JSONParserException {
        return null;
    }




}
