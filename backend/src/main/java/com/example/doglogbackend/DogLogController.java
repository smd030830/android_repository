package com.example.doglogbackend;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DogLogController {
    private static final String JSON_UTF8 = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8";
    private static final String TEXT_UTF8 = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8";

    private final JdbcTemplate jdbcTemplate;

    public DogLogController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping(value = "/health", produces = TEXT_UTF8)
    public String health() {
        return "ok";
    }

    @GetMapping(value = "/ServerProject/GetDogList.jsp", produces = JSON_UTF8)
    public List<Dog> getDogList(@RequestParam(required = false, defaultValue = "") String fosterId) {
        if (!fosterId.trim().isEmpty()) {
            return jdbcTemplate.query(
                    "SELECT name, breed, age, status, COALESCE(fosterId, '') AS fosterId, COALESCE(photoData, '') AS photoData FROM DOG "
                            + "WHERE fosterId = ? ORDER BY id DESC",
                    (rs, rowNum) -> new Dog(
                            rs.getString("name"),
                            rs.getString("breed"),
                            rs.getInt("age"),
                            rs.getString("status"),
                            rs.getString("fosterId"),
                            rs.getString("photoData")),
                    fosterId.trim());
        }

        return jdbcTemplate.query(
                "SELECT name, breed, age, status, COALESCE(fosterId, '') AS fosterId, COALESCE(photoData, '') AS photoData FROM DOG ORDER BY id DESC",
                (rs, rowNum) -> new Dog(
                        rs.getString("name"),
                        rs.getString("breed"),
                        rs.getInt("age"),
                        rs.getString("status"),
                        rs.getString("fosterId"),
                        rs.getString("photoData")));
    }

    @GetMapping(value = "/ServerProject/GetDogDetail.jsp", produces = JSON_UTF8)
    public List<Dog> getDogDetail(
            @RequestParam String name,
            @RequestParam(required = false, defaultValue = "") String fosterId) {
        if (!fosterId.trim().isEmpty()) {
            return jdbcTemplate.query(
                    "SELECT name, breed, age, status, COALESCE(fosterId, '') AS fosterId, COALESCE(photoData, '') AS photoData "
                            + "FROM DOG WHERE name = ? AND fosterId = ? ORDER BY id DESC LIMIT 1",
                    (rs, rowNum) -> new Dog(
                            rs.getString("name"),
                            rs.getString("breed"),
                            rs.getInt("age"),
                            rs.getString("status"),
                            rs.getString("fosterId"),
                            rs.getString("photoData")),
                    name,
                    fosterId.trim());
        }

        return jdbcTemplate.query(
                "SELECT name, breed, age, status, COALESCE(fosterId, '') AS fosterId, COALESCE(photoData, '') AS photoData "
                        + "FROM DOG WHERE name = ? ORDER BY id DESC LIMIT 1",
                (rs, rowNum) -> new Dog(
                        rs.getString("name"),
                        rs.getString("breed"),
                        rs.getInt("age"),
                        rs.getString("status"),
                        rs.getString("fosterId"),
                        rs.getString("photoData")),
                name);
    }

    @PostMapping(value = "/ServerProject/SaveDog.jsp", produces = TEXT_UTF8)
    public String saveDog(
            @RequestParam String name,
            @RequestParam(required = false, defaultValue = "") String breed,
            @RequestParam(required = false, defaultValue = "0") int age,
            @RequestParam(required = false, defaultValue = "임시보호중") String status,
            @RequestParam(required = false, defaultValue = "") String fosterId,
            @RequestParam(required = false, defaultValue = "") String photoData) {
        try {
            String ownerId = fosterId.trim();
            if (ownerId.isEmpty()) {
                return "fail";
            }

            List<Integer> existingIds = jdbcTemplate.query(
                    "SELECT id FROM DOG WHERE name = ? AND fosterId = ? LIMIT 1",
                    (rs, rowNum) -> rs.getInt("id"),
                    name,
                    ownerId);

            if (!existingIds.isEmpty()) {
                return "success";
            }

            int inserted = jdbcTemplate.update(
                    "INSERT INTO DOG (name, breed, age, status, fosterId, photoData) VALUES (?, ?, ?, ?, ?, ?)",
                    name,
                    emptyToDefault(breed, "견종 미상"),
                    age,
                    status,
                    ownerId,
                    photoData);
            return inserted > 0 ? "success" : "fail";
        } catch (DataAccessException e) {
            return "error";
        }
    }

    @PostMapping(value = "/ServerProject/UpdateDog.jsp", produces = TEXT_UTF8)
    public String updateDog(
            @RequestParam String name,
            @RequestParam(required = false, defaultValue = "") String breed,
            @RequestParam(required = false, defaultValue = "0") int age,
            @RequestParam(required = false, defaultValue = "임시보호중") String status,
            @RequestParam(required = false, defaultValue = "") String fosterId,
            @RequestParam(required = false, defaultValue = "") String photoData) {
        try {
            String ownerId = fosterId.trim();
            if (ownerId.isEmpty()) {
                return "fail";
            }

            int updated = jdbcTemplate.update(
                    "UPDATE DOG SET breed = ?, age = ?, status = ?, photoData = ? WHERE name = ? AND fosterId = ?",
                    emptyToDefault(breed, "견종 미상"),
                    age,
                    status,
                    photoData,
                    name,
                    ownerId);
            return updated > 0 ? "success" : "fail";
        } catch (DataAccessException e) {
            return "error";
        }
    }

    @GetMapping(value = "/ServerProject/GetDiaryList.jsp", produces = JSON_UTF8)
    public List<Diary> getDiaryList(
            @RequestParam String dogName,
            @RequestParam(required = false, defaultValue = "") String fosterId) {
        if (!fosterId.trim().isEmpty()) {
            return jdbcTemplate.query(
                    "SELECT id, fosterId, dogName, dateText, foodAmount, poopCount, content, COALESCE(photoData, '') AS photoData "
                            + "FROM DIARY WHERE dogName = ? AND fosterId = ? ORDER BY id DESC",
                    (rs, rowNum) -> new Diary(
                            rs.getInt("id"),
                            rs.getString("fosterId"),
                            rs.getString("dogName"),
                            rs.getString("dateText"),
                            rs.getInt("foodAmount"),
                            rs.getInt("poopCount"),
                            rs.getString("content"),
                            rs.getString("photoData")),
                    dogName,
                    fosterId.trim());
        }

        return jdbcTemplate.query(
                "SELECT id, fosterId, dogName, dateText, foodAmount, poopCount, content, COALESCE(photoData, '') AS photoData "
                        + "FROM DIARY WHERE dogName = ? ORDER BY id DESC",
                (rs, rowNum) -> new Diary(
                        rs.getInt("id"),
                        rs.getString("fosterId"),
                        rs.getString("dogName"),
                        rs.getString("dateText"),
                        rs.getInt("foodAmount"),
                        rs.getInt("poopCount"),
                        rs.getString("content"),
                        rs.getString("photoData")),
                dogName);
    }

    @PostMapping(value = "/ServerProject/SaveDiary.jsp", produces = TEXT_UTF8)
    public String saveDiary(
            @RequestParam(required = false, defaultValue = "") String fosterId,
            @RequestParam String dogName,
            @RequestParam String dateText,
            @RequestParam int foodAmount,
            @RequestParam int poopCount,
            @RequestParam String content,
            @RequestParam(required = false, defaultValue = "") String photoData) {
        try {
            String ownerId = fosterId.trim();
            if (ownerId.isEmpty()) {
                return "fail";
            }

            if (!dogBelongsToFoster(dogName, ownerId)) {
                return "fail";
            }

            int inserted = jdbcTemplate.update(
                    "INSERT INTO DIARY (fosterId, dogName, dateText, foodAmount, poopCount, content, photoData) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                    ownerId,
                    dogName,
                    dateText,
                    foodAmount,
                    poopCount,
                    content,
                    photoData);
            return inserted > 0 ? "success" : "fail";
        } catch (DataAccessException e) {
            return "error";
        }
    }

    @PostMapping(value = "/ServerProject/DeleteDiary.jsp", produces = TEXT_UTF8)
    public String deleteDiary(
            @RequestParam int id,
            @RequestParam(required = false, defaultValue = "") String fosterId) {
        try {
            String ownerId = fosterId.trim();
            if (ownerId.isEmpty()) {
                return "fail";
            }

            int deleted = jdbcTemplate.update(
                    "DELETE FROM DIARY WHERE id = ? AND fosterId = ?",
                    id,
                    ownerId);
            return deleted > 0 ? "success" : "fail";
        } catch (DataAccessException e) {
            return "error";
        }
    }

    @PostMapping(value = "/ServerProject/DeleteDog.jsp", produces = TEXT_UTF8)
    public String deleteDog(
            @RequestParam String name,
            @RequestParam(required = false, defaultValue = "") String fosterId) {
        try {
            String ownerId = fosterId.trim();
            if (ownerId.isEmpty()) {
                return "fail";
            }

            List<Integer> dogIds = jdbcTemplate.query(
                    "SELECT id FROM DOG WHERE name = ? AND fosterId = ? LIMIT 1",
                    (rs, rowNum) -> rs.getInt("id"),
                    name,
                    ownerId);
            if (dogIds.isEmpty()) {
                return "fail";
            }

            jdbcTemplate.update("DELETE FROM DIARY WHERE dogName = ? AND fosterId = ?", name, ownerId);
            int deleted = jdbcTemplate.update("DELETE FROM DOG WHERE id = ? AND fosterId = ?", dogIds.get(0), ownerId);
            return deleted > 0 ? "success" : "fail";
        } catch (DataAccessException e) {
            return "error";
        }
    }

    @PostMapping(value = "/ServerProject/SaveMessage.jsp", produces = TEXT_UTF8)
    public String saveMessage(
            @RequestParam(required = false, defaultValue = "") String senderId,
            @RequestParam(required = false, defaultValue = "") String receiverId,
            @RequestParam(required = false, defaultValue = "") String dogName,
            @RequestParam(required = false, defaultValue = "") String title,
            @RequestParam(required = false, defaultValue = "") String content) {
        try {
            String trimmedSender = senderId.trim();
            String trimmedReceiver = receiverId.trim();
            String trimmedContent = content.trim();
            if (trimmedSender.isEmpty() || trimmedReceiver.isEmpty() || trimmedContent.isEmpty()) {
                return "fail";
            }

            int inserted = jdbcTemplate.update(
                    "INSERT INTO CONTACT_MESSAGE (senderId, receiverId, dogName, title, content) VALUES (?, ?, ?, ?, ?)",
                    trimmedSender,
                    trimmedReceiver,
                    dogName.trim(),
                    emptyToDefault(title, dogName.trim().isEmpty() ? "문의 메시지" : dogName.trim() + " 문의"),
                    trimmedContent);
            return inserted > 0 ? "success" : "fail";
        } catch (DataAccessException e) {
            return "error";
        }
    }

    @GetMapping(value = "/ServerProject/GetReceivedMessages.jsp", produces = JSON_UTF8)
    public List<ContactMessage> getReceivedMessages(
            @RequestParam(required = false, defaultValue = "") String receiverId) {
        String ownerId = receiverId.trim();
        if (ownerId.isEmpty()) {
            return List.of();
        }

        return jdbcTemplate.query(
                "SELECT id, senderId, receiverId, dogName, title, content, "
                        + "COALESCE(replyContent, '') AS replyContent, createdAt, repliedAt "
                        + "FROM CONTACT_MESSAGE WHERE receiverId = ? ORDER BY id DESC",
                (rs, rowNum) -> new ContactMessage(
                        rs.getInt("id"),
                        rs.getString("senderId"),
                        rs.getString("receiverId"),
                        rs.getString("dogName"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("replyContent"),
                        timestampText(rs.getTimestamp("createdAt")),
                        timestampText(rs.getTimestamp("repliedAt"))),
                ownerId);
    }

    @GetMapping(value = "/ServerProject/GetSentMessages.jsp", produces = JSON_UTF8)
    public List<ContactMessage> getSentMessages(
            @RequestParam(required = false, defaultValue = "") String senderId) {
        String ownerId = senderId.trim();
        if (ownerId.isEmpty()) {
            return List.of();
        }

        return jdbcTemplate.query(
                "SELECT id, senderId, receiverId, dogName, title, content, "
                        + "COALESCE(replyContent, '') AS replyContent, createdAt, repliedAt "
                        + "FROM CONTACT_MESSAGE WHERE senderId = ? ORDER BY id DESC",
                (rs, rowNum) -> new ContactMessage(
                        rs.getInt("id"),
                        rs.getString("senderId"),
                        rs.getString("receiverId"),
                        rs.getString("dogName"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("replyContent"),
                        timestampText(rs.getTimestamp("createdAt")),
                        timestampText(rs.getTimestamp("repliedAt"))),
                ownerId);
    }

    @PostMapping(value = "/ServerProject/ReplyMessage.jsp", produces = TEXT_UTF8)
    public String replyMessage(
            @RequestParam int id,
            @RequestParam(required = false, defaultValue = "") String receiverId,
            @RequestParam(required = false, defaultValue = "") String replyContent) {
        try {
            String ownerId = receiverId.trim();
            String content = replyContent.trim();
            if (ownerId.isEmpty() || content.isEmpty()) {
                return "fail";
            }

            int updated = jdbcTemplate.update(
                    "UPDATE CONTACT_MESSAGE SET replyContent = ?, repliedAt = CURRENT_TIMESTAMP "
                            + "WHERE id = ? AND receiverId = ?",
                    content,
                    id,
                    ownerId);
            return updated > 0 ? "success" : "fail";
        } catch (DataAccessException e) {
            return "error";
        }
    }

    @PostMapping(value = "/ServerProject/UserLogin.jsp", produces = JSON_UTF8)
    public LoginResponse login(
            @RequestParam String userID,
            @RequestParam String userPassword) {
        List<String> userTypes = findUserTypes("APP_USER", userID, userPassword);
        if (userTypes.isEmpty()) {
            userTypes = findUserTypes("`USER`", userID, userPassword);
        }

        if (userTypes.isEmpty()) {
            return LoginResponse.failure();
        }
        return new LoginResponse(true, userTypes.get(0));
    }

    @PostMapping(value = "/ServerProject/UserRegister.jsp", produces = TEXT_UTF8)
    @Transactional
    public String register(
            @RequestParam String userID,
            @RequestParam String userPassword,
            @RequestParam(required = false, defaultValue = "") String userEmail,
            @RequestParam(required = false, defaultValue = "") String userGender,
            @RequestParam String userType) {
        try {
            String trimmedUserId = userID.trim();
            if (trimmedUserId.isEmpty() || userPassword.trim().isEmpty()) {
                return "fail";
            }

            if (userExists("APP_USER", trimmedUserId) || userExists("`USER`", trimmedUserId)) {
                return "fail";
            }

            int appUserInserted = insertUser("APP_USER", trimmedUserId, userPassword, userEmail, userGender, userType);
            int legacyUserInserted = insertUser("`USER`", trimmedUserId, userPassword, userEmail, userGender, userType);
            return appUserInserted > 0 && legacyUserInserted > 0 ? "success" : "fail";
        } catch (DataAccessException e) {
            return "error";
        }
    }

    private String emptyToDefault(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }

    private String timestampText(java.sql.Timestamp timestamp) {
        return timestamp == null ? "" : timestamp.toLocalDateTime().toString().replace('T', ' ');
    }

    private int insertUser(
            String tableName,
            String userID,
            String userPassword,
            String userEmail,
            String userGender,
            String userType) {
        return jdbcTemplate.update(
                "INSERT INTO " + tableName + " (userID, userPassword, userEmail, userGender, userType) VALUES (?, ?, ?, ?, ?)",
                userID,
                userPassword,
                userEmail,
                userGender,
                userType);
    }

    private boolean userExists(String tableName, String userID) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM " + tableName + " WHERE userID = ?",
                    Integer.class,
                    userID);
            return count != null && count > 0;
        } catch (DataAccessException e) {
            return false;
        }
    }

    private boolean dogBelongsToFoster(String dogName, String fosterId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM DOG WHERE name = ? AND fosterId = ?",
                Integer.class,
                dogName,
                fosterId);
        return count != null && count > 0;
    }

    private List<String> findUserTypes(String tableName, String userID, String userPassword) {
        try {
            return jdbcTemplate.query(
                    "SELECT userType FROM " + tableName + " WHERE userID = ? AND userPassword = ?",
                    (rs, rowNum) -> rs.getString("userType"),
                    userID,
                    userPassword);
        } catch (DataAccessException e) {
            return List.of();
        }
    }
}
