package com.auguraclient.model;

import java.util.List;


public interface ISuguraRestAPI {

    public User login(String userName, String password) throws APIException;

    
    public ProjectList loadProject(String name) throws APIException;

    public ProjectList queryProjectList(String name) throws APIException;


    public List<ProjectOrder> queryProjectOrderList(String projectID) throws APIException;


    public List<ProjectCheckpoint> queryProjectOrderCheckpointList(String orderId) throws APIException;

}
