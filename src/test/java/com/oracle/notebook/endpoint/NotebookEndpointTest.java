package com.oracle.notebook.endpoint;

import com.oracle.notebook.model.CodeRequest;
import com.oracle.notebook.model.ResultResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class NotebookEndpointTest {

    private NotebookEndpoint notebookEndpoint = new NotebookEndpoint();

    @Test
    void test_execute_nominal_print_sum() {
        CodeRequest codeRequest = new CodeRequest("%python print 1 + 1");
        ResponseEntity<ResultResponse> resultResponseEntity = notebookEndpoint.execute(codeRequest);
        ResultResponse resultResponse = resultResponseEntity.getBody();

        Assertions.assertEquals("2", resultResponse.getResult());
    }

    @Test
    void test_execute_nominal_var_declaration() {
        CodeRequest codeRequest = new CodeRequest("%python a = 3");
        ResponseEntity<ResultResponse> resultResponseEntity = notebookEndpoint.execute(codeRequest);
        ResultResponse resultResponse = resultResponseEntity.getBody();

        Assertions.assertEquals("", resultResponse.getResult());
    }

    @Test
    void test_execute_print_undeclared_var() {
        CodeRequest codeRequest = new CodeRequest("%python print a");
        ResponseEntity<ResultResponse> resultResponseEntity = notebookEndpoint.execute(codeRequest);
        ResultResponse resultResponse = resultResponseEntity.getBody();

        Assertions.assertEquals("NameError: name 'a' is not defined in <script> at line number 1", resultResponse.getError());
    }

    @Test
    void test_execute_unknown_interpreter() {
        CodeRequest codeRequest = new CodeRequest("%abc a = 1");
        ResponseEntity<ResultResponse> resultResponseEntity = notebookEndpoint.execute(codeRequest);
        ResultResponse resultResponse = resultResponseEntity.getBody();

        Assertions.assertEquals("Unknown 'abc' as interpreter", resultResponse.getResult());
    }

    @Test
    void test_execute_parse_error() {
        CodeRequest codeRequest = new CodeRequest("%pythona=1");
        ResponseEntity<ResultResponse> resultResponseEntity = notebookEndpoint.execute(codeRequest);
        ResultResponse resultResponse = resultResponseEntity.getBody();

        Assertions.assertEquals("Requested code cannot be parsed", resultResponse.getResult());
    }

    @Test
    void test_execute_with_session_id() {
        CodeRequest codeRequest = new CodeRequest("%python a = 1", "1");
        ResponseEntity<ResultResponse> resultResponseEntity = notebookEndpoint.execute(codeRequest);
        ResultResponse resultResponse = resultResponseEntity.getBody();

        Assertions.assertEquals("", resultResponse.getResult());
    }

    @Test
    void test_execute_with_session_id_twice() {
        CodeRequest codeRequest = new CodeRequest("%python a = 5", "1");
        ResponseEntity<ResultResponse> resultResponseEntity = notebookEndpoint.execute(codeRequest);
        ResultResponse resultResponse = resultResponseEntity.getBody();

        Assertions.assertEquals("", resultResponse.getResult());

        codeRequest = new CodeRequest("%python print a", "1");
        resultResponseEntity = notebookEndpoint.execute(codeRequest);
        resultResponse = resultResponseEntity.getBody();

        Assertions.assertEquals("5", resultResponse.getResult());
    }

    @Test
    void test_execute_with_different_session_id() {
        CodeRequest codeRequest = new CodeRequest("%python a = 5", "1");
        ResponseEntity<ResultResponse> resultResponseEntity = notebookEndpoint.execute(codeRequest);
        ResultResponse resultResponse = resultResponseEntity.getBody();

        Assertions.assertEquals("", resultResponse.getResult());

        codeRequest = new CodeRequest("%python print a", "2");
        resultResponseEntity = notebookEndpoint.execute(codeRequest);
        resultResponse = resultResponseEntity.getBody();

        Assertions.assertEquals("NameError: name 'a' is not defined in <script> at line number 1", resultResponse.getError());
    }
}