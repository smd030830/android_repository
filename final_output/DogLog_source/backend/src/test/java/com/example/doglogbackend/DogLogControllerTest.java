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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:doglog-test;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver"
})
@AutoConfigureMockMvc
class DogLogControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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

        Integer appUserCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM APP_USER WHERE userID = ?",
                Integer.class,
                "test-foster");
        Integer userCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM `USER` WHERE userID = ?",
                Integer.class,
                "test-foster");
        org.assertj.core.api.Assertions.assertThat(appUserCount).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(userCount).isEqualTo(1);

        mockMvc.perform(post("/ServerProject/UserLogin.jsp")
                        .param("userID", "test-foster")
                        .param("userPassword", "password"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.userType").value("foster"));
    }

    @Test
    void userCanLoginWithExistingUserTableData() throws Exception {
        jdbcTemplate.update("""
                CREATE TABLE IF NOT EXISTS `USER` (
                    userID VARCHAR(50) PRIMARY KEY,
                    userPassword VARCHAR(100) NOT NULL,
                    userEmail VARCHAR(255),
                    userGender VARCHAR(20),
                    userType VARCHAR(30) NOT NULL
                )
                """);
        jdbcTemplate.update("""
                INSERT INTO `USER` (userID, userPassword, userEmail, userGender, userType)
                VALUES (?, ?, ?, ?, ?)
                """, "legacy-foster", "legacy-password", "legacy@example.com", "female", "foster");

        mockMvc.perform(post("/ServerProject/UserLogin.jsp")
                        .param("userID", "legacy-foster")
                        .param("userPassword", "legacy-password"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.userType").value("foster"));
    }

    @Test
    void koreanDiaryTextIsStoredAndReturnedAsUtf8() throws Exception {
        saveDog("보리", "믹스", 2, "임시보호중", "한글사용자");

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
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].dogName").value("보리"))
                .andExpect(jsonPath("$[0].content").value("오늘 산책을 즐겁게 다녀왔어요"));
    }

    @Test
    void diaryCanOnlyBeSavedForDogsRegisteredByThatFoster() throws Exception {
        saveDog("내강아지", "믹스", 1, "임시보호중", "real-owner");

        mockMvc.perform(post("/ServerProject/SaveDiary.jsp")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("fosterId", "other-owner")
                        .param("dogName", "내강아지")
                        .param("dateText", "2026.06.12 12:00")
                        .param("foodAmount", "100")
                        .param("poopCount", "1")
                        .param("content", "남의 강아지 일지 작성 시도"))
                .andExpect(status().isOk())
                .andExpect(content().string("fail"));

        mockMvc.perform(post("/ServerProject/SaveDiary.jsp")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("fosterId", "real-owner")
                        .param("dogName", "내강아지")
                        .param("dateText", "2026.06.12 12:10")
                        .param("foodAmount", "100")
                        .param("poopCount", "1")
                        .param("content", "본인 강아지 일지 작성"))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));
    }

    @Test
    void onlyDiaryAuthorCanDeleteDiary() throws Exception {
        saveDog("삭제일지강아지", "믹스", 2, "임시보호중", "diary-owner");

        mockMvc.perform(post("/ServerProject/SaveDiary.jsp")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("fosterId", "diary-owner")
                        .param("dogName", "삭제일지강아지")
                        .param("dateText", "2026.06.12 10:00")
                        .param("foodAmount", "80")
                        .param("poopCount", "1")
                        .param("content", "삭제 권한 테스트"))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));

        Integer diaryId = jdbcTemplate.queryForObject(
                "SELECT id FROM DIARY WHERE fosterId = ? AND dogName = ?",
                Integer.class,
                "diary-owner",
                "삭제일지강아지");

        mockMvc.perform(post("/ServerProject/DeleteDiary.jsp")
                        .param("id", String.valueOf(diaryId))
                        .param("fosterId", "other-owner"))
                .andExpect(status().isOk())
                .andExpect(content().string("fail"));

        Integer afterWrongOwnerCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM DIARY WHERE id = ?",
                Integer.class,
                diaryId);
        org.assertj.core.api.Assertions.assertThat(afterWrongOwnerCount).isEqualTo(1);

        mockMvc.perform(post("/ServerProject/DeleteDiary.jsp")
                        .param("id", String.valueOf(diaryId))
                        .param("fosterId", "diary-owner"))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));

        Integer afterOwnerDeleteCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM DIARY WHERE id = ?",
                Integer.class,
                diaryId);
        org.assertj.core.api.Assertions.assertThat(afterOwnerDeleteCount).isZero();
    }

    @Test
    void onlyDogOwnerCanDeleteDogAndTheirDiaries() throws Exception {
        saveDog("삭제강아지", "믹스", 3, "임시보호중", "dog-owner");

        mockMvc.perform(post("/ServerProject/SaveDiary.jsp")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("fosterId", "dog-owner")
                        .param("dogName", "삭제강아지")
                        .param("dateText", "2026.06.12 11:00")
                        .param("foodAmount", "90")
                        .param("poopCount", "2")
                        .param("content", "강아지 삭제 시 같이 정리될 일지"))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));

        mockMvc.perform(post("/ServerProject/DeleteDog.jsp")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("name", "삭제강아지")
                        .param("fosterId", "other-owner"))
                .andExpect(status().isOk())
                .andExpect(content().string("fail"));

        Integer afterWrongOwnerDogCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM DOG WHERE name = ? AND fosterId = ?",
                Integer.class,
                "삭제강아지",
                "dog-owner");
        org.assertj.core.api.Assertions.assertThat(afterWrongOwnerDogCount).isEqualTo(1);

        mockMvc.perform(post("/ServerProject/DeleteDog.jsp")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("name", "삭제강아지")
                        .param("fosterId", "dog-owner"))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));

        Integer afterOwnerDeleteDogCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM DOG WHERE name = ? AND fosterId = ?",
                Integer.class,
                "삭제강아지",
                "dog-owner");
        Integer afterOwnerDeleteDiaryCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM DIARY WHERE dogName = ? AND fosterId = ?",
                Integer.class,
                "삭제강아지",
                "dog-owner");
        org.assertj.core.api.Assertions.assertThat(afterOwnerDeleteDogCount).isZero();
        org.assertj.core.api.Assertions.assertThat(afterOwnerDeleteDiaryCount).isZero();
    }

    @Test
    void userCanSendContactMessageToDiaryAuthor() throws Exception {
        mockMvc.perform(post("/ServerProject/SaveMessage.jsp")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("senderId", "adopter-1")
                        .param("receiverId", "foster-1")
                        .param("dogName", "초코")
                        .param("title", "초코 문의")
                        .param("content", "일지를 보고 연락드립니다."))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM CONTACT_MESSAGE WHERE senderId = ? AND receiverId = ? AND dogName = ?",
                Integer.class,
                "adopter-1",
                "foster-1",
                "초코");
        org.assertj.core.api.Assertions.assertThat(count).isEqualTo(1);

        mockMvc.perform(get("/ServerProject/GetReceivedMessages.jsp")
                        .param("receiverId", "foster-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].senderId").value("adopter-1"))
                .andExpect(jsonPath("$[0].receiverId").value("foster-1"))
                .andExpect(jsonPath("$[0].replyContent").value(""));

        Integer messageId = jdbcTemplate.queryForObject(
                "SELECT id FROM CONTACT_MESSAGE WHERE senderId = ? AND receiverId = ? AND dogName = ?",
                Integer.class,
                "adopter-1",
                "foster-1",
                "초코");

        mockMvc.perform(post("/ServerProject/ReplyMessage.jsp")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("id", String.valueOf(messageId))
                        .param("receiverId", "foster-1")
                        .param("replyContent", "문의 감사합니다. 내일 연락드릴게요."))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));

        mockMvc.perform(get("/ServerProject/GetSentMessages.jsp")
                        .param("senderId", "adopter-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].receiverId").value("foster-1"))
                .andExpect(jsonPath("$[0].replyContent").value("문의 감사합니다. 내일 연락드릴게요."));
    }

    private void saveDog(String name, String breed, int age, String status, String fosterId) throws Exception {
        mockMvc.perform(post("/ServerProject/SaveDog.jsp")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("name", name)
                        .param("breed", breed)
                        .param("age", String.valueOf(age))
                        .param("status", status)
                        .param("fosterId", fosterId))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));
    }
}
