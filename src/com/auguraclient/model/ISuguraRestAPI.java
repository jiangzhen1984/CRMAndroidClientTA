package com.auguraclient.model;

import java.util.List;


public interface ISuguraRestAPI {

    public User login(String userName, String password) throws APIException;


    public ProjectList queryProjectList(String name) throws APIException;


    public List<ProjectOrder> queryProjectItemList(String projectID) throws APIException;


    public List<ProjectCheckpoint> queryProjectItemOrderList(String orderId) throws APIException;

}
