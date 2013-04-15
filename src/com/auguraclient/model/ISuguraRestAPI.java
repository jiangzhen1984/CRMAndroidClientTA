package com.auguraclient.model;

import java.util.List;


public interface ISuguraRestAPI {

    public User login(String userName, String password) throws APIException;


    public ProjectList loadProject(String name) throws APIException;

    public ProjectList queryProjectList(String name) throws APIException;


    public List<ProjectOrder> queryProjectOrderList(String projectID) throws APIException;


    public List<ProjectCheckpoint> queryProjectOrderCheckpointList(String orderId) throws APIException;


    public void deleteCheckpoint(String checkpointId) throws APIException;

    public void createCheckpoint(ProjectOrder order, ProjectCheckpoint checkpoint) throws APIException;


    public void updateCheckpoint(ProjectCheckpoint checkpoint) throws APIException;

}
