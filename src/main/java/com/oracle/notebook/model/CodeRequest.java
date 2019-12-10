package com.oracle.notebook.model;

import com.fasterxml.jackson.annotation.JsonInclude;

public class CodeRequest {

    private String code;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String sessionId;

    public CodeRequest() {
    }

    public CodeRequest(String code) {
        this.code = code;
    }

    public CodeRequest(String code, String sessionId) {
        this.code = code;
        this.sessionId = sessionId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
