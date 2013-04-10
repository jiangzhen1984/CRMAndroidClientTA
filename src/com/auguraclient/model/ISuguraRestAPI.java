package com.auguraclient.model;

import java.util.List;

public interface ISuguraRestAPI {

    public User login(String userName, String password) throws APIException;


    public List<Project> queryProjectList(String name) throws APIException;

}
