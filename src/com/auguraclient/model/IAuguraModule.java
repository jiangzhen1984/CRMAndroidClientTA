package com.auguraclient.model;

import android.content.ContentResolver;

public interface IAuguraModule {

	public void saveOrUpdateProject(ContentResolver cr, Project p)
			throws Exception;

	public void saveOrUpdateProjectOrder(ContentResolver cr, ProjectOrder p)
			throws Exception;

	public void saveOrUpdateCheckpoint(ContentResolver cr, ProjectCheckpoint p)
			throws Exception;

	public void deleteProject(ContentResolver cr, Project p) throws Exception;

	public void deleteProjectOrder(ContentResolver cr, ProjectOrder p)
			throws Exception;

	public void deleteCheckpoint(ContentResolver cr, ProjectCheckpoint p)
			throws Exception;
	
	
}
