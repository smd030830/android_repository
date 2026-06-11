package com.example.doglogbackend;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
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

    @GetMapping(value = "/ServerProject/GetDogList.jsp", produces = JSON_UTF8)
    public List<Dog> getDogList(@RequestParam(required = false, defaultValue = "") String fosterId) {
        if (!fosterId.trim().isEmpty()) {
            return jdbcTemplate.query(
                    "SELECT name, breed, age, status, COALESCE(fosterId, '') AS fosterId FROM DOG "
                            + "WHERE fosterId = ? ORDER BY id DESC",
                    (rs, rowNum) -> new Dog(
                            rs.getString("name"),
                            rs.getString("breed"),
                            rs.getInt("age"),
                            rs.getString("status"),
                            rs.getString("fosterId")),
                    fosterId.trim());
        }

        return jdbcTemplate.query(
                "SELECT name, breed, age, status, COALESCE(fosterId, '') AS fosterId FROM DOG ORDER BY id DESC",
                (rs, rowNum) -> new Dog(
                        rs.getString("name"),
                        rs.getString("breed"),
                        rs.getInt("age"),
                        rs.getString("status"),
                        rs.getString("fosterId")));
    }

    @PostMapping(value = "/ServerProject/SaveDog.jsp", produces = TEXT_UTF8)
    public String saveDog(
            @RequestParam String name,
            @RequestParam(required = false, defaultValue = "") String breed,
            @RequestParam(required = false, defaultValue = "0") int age,
            @RequestParam(required = false, defaultValue = "임시보호중") String status,
            @RequestParam(required = false, defaultValue = "") String fosterId) {
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
                    "INSERT INTO DOG (name, breed, age, status, fosterId) VALUES (?, ?, ?, ?, ?)",
                    name,
                    emptyToDefault(breed, "견종 미상"),
                    age,
                    status,
                    ownerId);
            return inserted > 0 ? "success" : "fail";
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
                    "SELECT fosterId, dogName, dateText, foodAmount, poopCount, content, COALESCE(photoData, '') AS photoData "
                            + "FROM DIARY WHERE dogName = ? AND fosterId = ? ORDER BY id DESC",
                    (rs, rowNum) -> new Diary(
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
                "SELECT fosterId, dogName, dateText, foodAmount, poopCount, content, COALESCE(photoData, '') AS photoData "
                        + "FROM DIARY WHERE dogName = ? ORDER BY id DESC",
                (rs, rowNum) -> new Diary(
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

    @PostMapping(value = "/ServerProject/UserLogin.jsp", produces = JSON_UTF8)
    public LoginResponse login(
            @RequestParam String userID,
            @RequestParam String userPassword) {
        List<String> userTypes = jdbcTemplate.query(
                "SELECT userType FROM `USER` WHERE userID = ? AND userPassword = ?",
                (rs, rowNum) -> rs.getString("userType"),
                userID,
                userPassword);

        if (userTypes.isEmpty()) {
            return LoginResponse.failure();
        }
        return new LoginResponse(true, userTypes.get(0));
    }

    @PostMapping(value = "/ServerProject/UserRegister.jsp", produces = TEXT_UTF8)
    public String register(
            @RequestParam String userID,
            @RequestParam String userPassword,
            @RequestParam(required = false, defaultValue = "") String userEmail,
            @RequestParam(required = false, defaultValue = "") String userGender,
            @RequestParam String userType) {
        try {
            int inserted = jdbcTemplate.update(
                    "INSERT INTO `USER` (userID, userPassword, userEmail, userGender, userType) VALUES (?, ?, ?, ?, ?)",
                    userID,
                    userPassword,
                    userEmail,
                    userGender,
                    userType);
            return inserted > 0 ? "success" : "fail";
        } catch (DataAccessException e) {
            return "error";
        }
    }

    private String emptyToDefault(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }
}
