package com.auguraclient.model;

public class JSONParserException extends Exception {

    public JSONParserException() {
        super();
    }

    public JSONParserException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public JSONParserException(String detailMessage) {
        super(detailMessage);
    }

    public JSONParserException(Throwable throwable) {
        super(throwable);
    }


}
