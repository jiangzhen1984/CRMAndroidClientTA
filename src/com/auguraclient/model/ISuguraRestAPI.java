package com.auguraclient.model;

import java.util.List;


public interface ISuguraRestAPI {

    public User login(String userName, String password) throws APIException,SessionAPIException;


    public ProjectList loadProject(String name) throws APIException,SessionAPIException;

    public ProjectList queryProjectList(String name) throws APIException,SessionAPIException;


    public List<ProjectOrder> queryProjectOrderList(String projectID) throws APIException,SessionAPIException;


    public List<ProjectCheckpoint> queryProjectOrderCheckpointList(String orderId) throws APIException,SessionAPIException;


    public void deleteCheckpoint(String checkpointId) throws APIException,SessionAPIException;

    public void createCheckpoint(ProjectOrder order, ProjectCheckpoint checkpoint) throws APIException,SessionAPIException;


    public void updateCheckpoint(ProjectCheckpoint checkpoint) throws APIException,SessionAPIException;

}
