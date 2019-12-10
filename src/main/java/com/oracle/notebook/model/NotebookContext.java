package com.oracle.notebook.model;

import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;

public class NotebookContext {

    private String sessionId;
    private ScriptContext scriptContext;

    public NotebookContext(String sessionId) {
        this.sessionId = sessionId;
        scriptContext = new SimpleScriptContext();
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public ScriptContext getScriptContext() {
        return scriptContext;
    }

    public void setScriptContext(ScriptContext scriptContext) {
        this.scriptContext = scriptContext;
    }
}