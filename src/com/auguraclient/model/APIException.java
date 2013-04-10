package com.auguraclient.model;

public class APIException extends Exception {

    public APIException() {
        super();
    }

    public APIException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public APIException(String detailMessage) {
        super(detailMessage);
    }

    public APIException(Throwable throwable) {
        super(throwable);
    }


}
