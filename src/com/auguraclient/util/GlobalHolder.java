package com.auguraclient.util;

import com.auguraclient.model.Project;
import com.auguraclient.model.ProjectList;
import com.auguraclient.model.User;

public class GlobalHolder {

	private static User currentLogInedUser;

	private static ProjectList pl;
	
	public static String GLOBAL_STORAGE_PATH;

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

	public static void addProject(ProjectList pList) {
		if (pl == null) {
			pl = pList;
		} else {
			for (int i = 0; i < pList.getList().size(); i++) {
				pl.addProject(pList.getList().get(i));
			}
		}
	}

}
