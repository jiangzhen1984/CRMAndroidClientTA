package com.auguraclient.util;

import com.auguraclient.model.Project;
import com.auguraclient.model.ProjectList;
import com.auguraclient.model.User;

public class GlobalHolder {

	private  User currentLogInedUser;

	private  ProjectList pl;

	private  String GLOBAL_STORAGE_PATH;

	private  String[] CATEGORY_ENUM = { "", "Accessory", "Appearance",
			"Dimension Weight", "Functioning", "Marking", "Material",
			"Packaging", "Resistance", "Wrapping", "Other" };
	
	private  String[] CATEGORY_ENUM_VALUE = { "", "accessory", "appearance",
		"dimension_weight", "functioning", "marking", "material",
		"packaging", "resistance", "wrapping", "other" };

	private  String[] CHECK_TYPE_ENUM = { "", "Visual", "Manual Test",
			"Ruler", "Scale", "Caliper", "Pulling Tool" };
	private String[] CHECK_TYPE_ENUM_VALUE = { "", "visual", "manual",
		"ruler", "scale", "caliper", "pulling_tool" };
	
	private String[] QC_ACTION_ENUM ={"", "Correct During QC","Correct After QC", "Cannot Correct", "Refuse to Correct"};
	
	private String[] QC_ACTION_ENUM_VALUE ={"", "during_qc","after_qc", "cannot", "refuse"};
	
	
	private String[] QC_Status_ENUM ={"", "Failed","Passed", "Alert", "Ready"};
	
	
	
	
	
	
	private static GlobalHolder mInstance;
	
	public static GlobalHolder getInstance() {
		if(mInstance == null) {
			mInstance = new GlobalHolder();
		}
		return mInstance;
	}
	
	private GlobalHolder() {
		
	}
	
	public String getStoragePath() {
		return this.GLOBAL_STORAGE_PATH;
	}

	public synchronized  void setCurrentUser(User user) {
		currentLogInedUser = user;
	}

	public synchronized  User getCrrentUser() {
		return currentLogInedUser;
	}

	public  String getSessionId() {
		if (currentLogInedUser == null) {
			return null;
		}
		return currentLogInedUser.getmSessionID();
	}

	public  ProjectList getPl() {
		return pl;
	}

	public  void setPl(ProjectList pl) {
		this.pl = pl;
	}
	
	public  int getIndex(Project p) {
		for (int i = 0; i < pl.getList().size(); i++) {
			Project newP = pl.getList().get(i);
			if(newP.getId().equals(p.getId())) {
				return i;
			}
		}
		return -1;
	}

	public  Project getProject(int index) {
		if (pl == null || pl.getList() == null || pl.getList().isEmpty()
				|| pl.getList().size() < index) {
			return null;
		}

		return pl.getList().get(index);
	}

	public  void addProject(Project p) {
		pl.addProject(p);
	}
	
	public  Project getProjectById(String id) {
		for (int i = 0; i < pl.getList().size(); i++) {
			Project newP = pl.getList().get(i);
			if(newP.getId().equals(id)) {
				return newP;
			}
		}
		return null;
	}

	public  void addProject(ProjectList pList) {
		if (pl == null) {
			pl = pList;
		} else {
			for (int i = 0; i < pList.getList().size(); i++) {
				Project newP = pList.getList().get(i);
				pl.removeProject(newP);
				pl.addProject(newP);
			}
		}
	}
	
	
	public String[] getCategoryLabel() {
		return this.CATEGORY_ENUM;
	}
	
	public String[] getCategoryValue() {
		return this.CATEGORY_ENUM_VALUE;
	}
	
	public String[] getQcActionLabel() {
		return this.QC_ACTION_ENUM;
	}
	public String[] getQcActionValue() {
		return this.QC_ACTION_ENUM_VALUE;
	}
	
	public String[] getChecktypeLabel() {
		return this.CHECK_TYPE_ENUM;
	}
	
	public String[] getChecktypeValue() {
		return this.CHECK_TYPE_ENUM_VALUE;
	}
	
	
	public void setStoragePath(String path){
		this.GLOBAL_STORAGE_PATH = path;
	}

	
	public String[] getQcStatus() {
		return this.QC_Status_ENUM;
	}
}
