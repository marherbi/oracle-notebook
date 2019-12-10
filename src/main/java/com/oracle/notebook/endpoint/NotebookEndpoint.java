package com.oracle.notebook.endpoint;

import com.oracle.notebook.model.CodeRequest;
import com.oracle.notebook.model.NotebookContext;
import com.oracle.notebook.model.ResultResponse;
import org.python.google.common.base.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.script.*;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class NotebookEndpoint {

    // Shared context for code requests without sessionId
    private ScriptContext sharedContext = new SimpleScriptContext();

    // Map containing the contexts of all the interpreters registred
    private Map<String, List<NotebookContext>> interpretersNotbookContexts = new HashMap<>();

    /**
     * execute Endpoint allowing to execute the command sent with the requested interpreter
     *
     * @param codeRequest request containing the code to execute
     * @return the result of the executed command
     */
    @PostMapping(path = "/execute")
    public ResponseEntity<ResultResponse> execute(@RequestBody CodeRequest codeRequest) {
        // Pattern with a regex having 2 groups
        // 1st one capturing the interpreter
        // 2nd one capturing the command
        Pattern pattern = Pattern.compile("^%(.*?)\\s(.*)");

        ResultResponse resultResponse;

        Matcher matcher = pattern.matcher(codeRequest.getCode());
        if (matcher.find()) {
            String interpreter = matcher.group(1);
            String code = matcher.group(2);
            ScriptEngine engine = getScriptEngine(interpreter, codeRequest.getSessionId());

            if (engine != null) {
                // Prepare a Writer to capture the output of the python code
                Writer consoleWriter = new StringWriter();
                // Attach the writer to the Python Interpreter
                engine.getContext().setWriter(consoleWriter);
                try {
                    engine.eval(code);
                    resultResponse = new ResultResponse(consoleWriter.toString().trim());
                } catch (ScriptException scriptException) {
                    // Catch the error after code execution and coping it in the error result
                    resultResponse = new ResultResponse(null, scriptException.getMessage());
                }
            } else {
                // Prepare an error message with interpeter unknown
                resultResponse = new ResultResponse(null, "Unknown '" + interpreter + "' as interpreter");
            }
        } else {
            // Send an error message if the code cannot be parsed
            resultResponse = new ResultResponse(null, "Requested code cannot be parsed");
        }
        return new ResponseEntity<>(resultResponse, HttpStatus.OK);
    }

    private ScriptEngine getScriptEngine(String interpreter, final String sessionId) {
        // get the scriptEngine from ScriptEngineManager by the interpreter name
        ScriptEngine engine = new ScriptEngineManager().getEngineByName(interpreter);
        NotebookContext notebookContext;
        // engine may be null (not found)
        if (engine != null) {
            // check if the requested code contains a sessionId, if no return the shared context
            if (Strings.isNullOrEmpty(sessionId)) {
                engine.setContext(sharedContext);
                return engine;
            }
            // check if the wanted interpreter have already a stored context with sessionId
            if (interpretersNotbookContexts.containsKey(interpreter)) {
                // Get the context of the requested sessionId
                Optional<NotebookContext> sessionContext = interpretersNotbookContexts.get(interpreter).stream().filter(nbc -> sessionId.equals(nbc.getSessionId())).findFirst();
                // if the sessionId exists, return the engine with its context
                if (sessionContext.isPresent()) {
                    ScriptContext scriptContext = sessionContext.get().getScriptContext();
                    engine.setContext(scriptContext);
                    return engine;
                }
            }
            // if there is no stored context or the interpreter is not already added to the map, add those items to the map
            interpretersNotbookContexts.putIfAbsent(interpreter, new Vector<>());
            notebookContext = new NotebookContext(sessionId);
            interpretersNotbookContexts.get(interpreter).add(notebookContext);
            engine.setContext(notebookContext.getScriptContext());
        }
        return engine;
    }
}
