package com.auguraclient.util;

import com.auguraclient.model.Project;
import com.auguraclient.model.ProjectList;
import com.auguraclient.model.User;

public class GlobalHolder {

	private static User currentLogInedUser;

	private static ProjectList pl;

	public static String GLOBAL_STORAGE_PATH;

	public static String[] CATEGORY_ENUM = { "", "Accessory", "Appearance",
			"Dimension Weight", "Functioning", "Marking", "Material",
			"Packaging", "Resistance", "Wrapping", "Other" };
	
	public static String[] CATEGORY_ENUM_VALUE = { "", "accessory", "appearance",
		"dimension_weight", "functioning", "marking", "material",
		"packaging", "resistance", "wrapping", "other" };

	public static String[] CHECK_TYPE_ENUM = { "", "Visual", "Manual Test",
			"Ruler", "Scale", "Caliper", "Pulling Tool" };
	public static String[] CHECK_TYPE_ENUM_VALUE = { "", "visual", "manual",
		"ruler", "scale", "caliper", "pulling_tool" };
	
	public static String[] QC_ACTION_ENUM ={"", "Correct During QC","Correct After QC", "Cannot Correct", "Refuse to Correct"};
	
	public static String[] QC_ACTION_ENUM_VALUE ={"", "during_qc","after_qc", "cannot", "refuse"};
	
	
	public static String[] QC_Status_ENUM ={"", "Failed","Passed", "Alert", "Ready"};

	public synchronized static void setCurrentUser(User user) {
		currentLogInedUser = user;
	}

	public synchronized static User getCrrentUser() {
		return currentLogInedUser;
	}

	public static String getSessionId() {
		if (currentLogInedUser == null) {
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
	
	public static int getIndex(Project p) {
		for (int i = 0; i < pl.getList().size(); i++) {
			Project newP = pl.getList().get(i);
			if(newP.getId().equals(p.getId())) {
				return i;
			}
		}
		return -1;
	}

	public static Project getProject(int index) {
		if (pl == null || pl.getList() == null || pl.getList().isEmpty()
				|| pl.getList().size() < index) {
			return null;
		}

		return pl.getList().get(index);
	}

	public static void addProject(Project p) {
		pl.addProject(p);
	}
	
	public static Project getProjectById(String id) {
		for (int i = 0; i < pl.getList().size(); i++) {
			Project newP = pl.getList().get(i);
			if(newP.getId().equals(id)) {
				return newP;
			}
		}
		return null;
	}

	public static void addProject(ProjectList pList) {
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

}
