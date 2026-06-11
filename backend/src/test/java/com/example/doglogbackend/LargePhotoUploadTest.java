package com.example.doglogbackend;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Base64;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:large-photo-test;MODE=MySQL;DB_CLOSE_DELAY=-1")
class LargePhotoUploadTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void acceptsPhotoFormDataLargerThanTheDefaultTomcatLimit() {
        byte[] photoBytes = new byte[3 * 1024 * 1024];
        new Random(42).nextBytes(photoBytes);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("fosterId", "large-photo-user");
        form.add("dogName", "PhotoDog");
        form.add("dateText", "2026.06.11 20:00");
        form.add("foodAmount", "100");
        form.add("poopCount", "1");
        form.add("content", "large photo upload");
        form.add("photoData", Base64.getEncoder().encodeToString(photoBytes));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/ServerProject/SaveDiary.jsp",
                request,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("success");
    }
}
