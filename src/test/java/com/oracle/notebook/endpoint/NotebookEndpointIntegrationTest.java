package com.oracle.notebook.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oracle.notebook.model.CodeRequest;
import com.oracle.notebook.model.ResultResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class NotebookEndpointIntegrationTest {

    private static final ObjectMapper om = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void test_simple_code_without_session() throws Exception {
        CodeRequest codeRequest = new CodeRequest("%python a = 3");
        String jsonResult = om.writeValueAsString(new ResultResponse(""));
        mockMvc.perform(
                post("/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(codeRequest)))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResult));
    }

    @Test
    public void test_consecutive_code_without_session() throws Exception {
        CodeRequest codeRequest1 = new CodeRequest("%python a = 3");
        CodeRequest codeRequest2 = new CodeRequest("%python print a + 2");

        mockMvc.perform(
                post("/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(codeRequest1)));

        String jsonResult = om.writeValueAsString(new ResultResponse("5"));

        mockMvc.perform(
                post("/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(codeRequest2)))
                .andExpect(content().json(jsonResult));
    }

    @Test
    public void test_simple_code_with_session() throws Exception {
        CodeRequest codeRequest = new CodeRequest("%python a = 3", "1");
        String jsonResult = om.writeValueAsString(new ResultResponse(""));
        mockMvc.perform(
                post("/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(codeRequest)))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResult));
    }

    @Test
    public void test_consecutive_code_with_session() throws Exception {
        CodeRequest codeRequest1 = new CodeRequest("%python a = 3", "1");
        CodeRequest codeRequest2 = new CodeRequest("%python print a + 2", "1");

        mockMvc.perform(
                post("/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(codeRequest1)));

        String jsonResult = om.writeValueAsString(new ResultResponse("5"));

        mockMvc.perform(
                post("/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(codeRequest2)))
                .andExpect(content().json(jsonResult));
    }

    @Test
    public void test_consecutive_code_with_different_sessions() throws Exception {
        CodeRequest codeRequestSession1 = new CodeRequest("%python a = 3", "1");
        CodeRequest codeRequestSession2 = new CodeRequest("%python a = 7", "2");
        CodeRequest codeRequestSession1Print = new CodeRequest("%python print a + 2", "1");
        CodeRequest codeRequestSession2Print = new CodeRequest("%python print a + 2", "2");

        mockMvc.perform(
                post("/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(codeRequestSession1)));
        mockMvc.perform(
                post("/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(codeRequestSession2)));

        String jsonResult1 = om.writeValueAsString(new ResultResponse("5"));
        String jsonResult2 = om.writeValueAsString(new ResultResponse("9"));

        mockMvc.perform(
                post("/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(codeRequestSession1Print)))
                .andExpect(content().json(jsonResult1));
        mockMvc.perform(
                post("/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(codeRequestSession2Print)))
                .andExpect(content().json(jsonResult2));
    }

    @Test
    public void test_unknown_interpreter() throws Exception {
        CodeRequest codeRequest = new CodeRequest("%noPython a = 3", "1");

        String jsonResult = om.writeValueAsString(new ResultResponse(null, "Unknown 'noPython' as interpreter"));

        mockMvc.perform(
                post("/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(codeRequest)))
                .andExpect(content().json(jsonResult));
    }

    @Test
    public void test_compilation_error() throws Exception {
        CodeRequest codeRequest = new CodeRequest("%python print var", "1");

        String jsonResult = om.writeValueAsString(new ResultResponse(null, "NameError: name 'var' is not defined in <script> at line number 1"));

        mockMvc.perform(
                post("/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(codeRequest)))
                .andExpect(content().json(jsonResult));
    }
    @Test
    public void test_parse_error() throws Exception {
        CodeRequest codeRequest = new CodeRequest("python print var", "1");

        String jsonResult = om.writeValueAsString(new ResultResponse(null, "Requested code cannot be parsed"));

        mockMvc.perform(
                post("/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(codeRequest)))
                .andExpect(content().json(jsonResult));
    }

}
