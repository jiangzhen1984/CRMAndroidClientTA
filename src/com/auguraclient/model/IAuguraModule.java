package com.auguraclient.model;

import android.net.Uri;

public interface IAuguraModule {

	
	public Uri saveUpdateReocrd(UpdateRecord ur) throws Exception;
	
	
	public Uri saveProject(Project p) throws Exception;

	public Uri saveProjectOrder(ProjectOrder p) throws Exception;

	public Uri saveCheckpoint(ProjectCheckpoint p) throws Exception;

	public int updateProject(Project p) throws Exception;

	public int updateProjectOrder(ProjectOrder p) throws Exception;

	public int updateCheckpoint(ProjectCheckpoint p) throws Exception;

	public void deleteProject(Project p) throws Exception;

	public void deleteProjectOrder(ProjectOrder p) throws Exception;

	public void deleteCheckpoint(ProjectCheckpoint p) throws Exception;
	
	
    
	
	public int update(AbstractModel model, String whereCondition, String[] args);
	
    /**
     */
    public int deleteFromDB(Class<? extends AbstractModel> cls, String whereCondition, String[] args);

}
