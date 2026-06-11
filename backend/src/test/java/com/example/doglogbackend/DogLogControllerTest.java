package com.example.doglogbackend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:doglog-test;MODE=MySQL;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class DogLogControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointIsAvailable() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    void userCanRegisterAndLogin() throws Exception {
        mockMvc.perform(post("/ServerProject/UserRegister.jsp")
                        .param("userID", "test-foster")
                        .param("userPassword", "password")
                        .param("userType", "foster"))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));

        mockMvc.perform(post("/ServerProject/UserLogin.jsp")
                        .param("userID", "test-foster")
                        .param("userPassword", "password"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.userType").value("foster"));
    }

    @Test
    void koreanDiaryTextIsStoredAndReturnedAsUtf8() throws Exception {
        mockMvc.perform(post("/ServerProject/SaveDiary.jsp")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("fosterId", "한글사용자")
                        .param("dogName", "보리")
                        .param("dateText", "2026.06.11 20:30")
                        .param("foodAmount", "120")
                        .param("poopCount", "2")
                        .param("content", "오늘 산책을 즐겁게 다녀왔어요"))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));

        mockMvc.perform(get("/ServerProject/GetDiaryList.jsp")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("dogName", "보리")
                        .param("fosterId", "한글사용자"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dogName").value("보리"))
                .andExpect(jsonPath("$[0].content").value("오늘 산책을 즐겁게 다녀왔어요"));
    }
}
