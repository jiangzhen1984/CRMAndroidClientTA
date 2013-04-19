package com.auguraclient.model;

public class SessionAPIException extends Exception {

    public SessionAPIException() {
        super();
    }

    public SessionAPIException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public SessionAPIException(String detailMessage) {
        super(detailMessage);
    }

    public SessionAPIException(Throwable throwable) {
        super(throwable);
    }


}
