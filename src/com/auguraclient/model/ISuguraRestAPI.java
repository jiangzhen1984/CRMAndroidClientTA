package com.auguraclient.model;


public interface ISuguraRestAPI {

    public User login(String userName, String password) throws APIException;


    public ProjectList queryProjectList(String name) throws APIException;

}
