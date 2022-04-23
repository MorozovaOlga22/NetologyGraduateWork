package ru.netology.graduatework;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.netology.graduatework.dto.FileNameInRequest;
import ru.netology.graduatework.dto.LoginInRequest;
import ru.netology.graduatework.dto.LoginInResponse;
import ru.netology.graduatework.entities.FileEntity;
import ru.netology.graduatework.entities.TokenEntity;
import ru.netology.graduatework.entities.UserEntity;
import ru.netology.graduatework.repository.FileRepository;
import ru.netology.graduatework.repository.TokenRepository;
import ru.netology.graduatework.repository.UserRepository;

import java.util.Optional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:test.properties")
class GraduateWorkApplicationTests {
    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FileRepository fileRepository;

    @Autowired
    TokenRepository tokenRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        fileRepository.deleteAll();
        tokenRepository.deleteAll();
    }

    @Test
    public void testLogin() {
        userRepository.save(new UserEntity("ivan", "password"));
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        final LoginInRequest operation = new LoginInRequest("ivan", "password");
        final HttpEntity<LoginInRequest> request = new HttpEntity<>(operation, headers);

        final ResponseEntity<LoginInResponse> result = this.restTemplate.postForEntity("/login", request, LoginInResponse.class);
        Assertions.assertNotNull(result.getBody());
        Assertions.assertNotNull(result.getBody().getAuthToken());
    }

    @Test
    public void testLogout() {
        final String authToken = "123";
        tokenRepository.save(new TokenEntity(authToken));

        final HttpHeaders headers = new HttpHeaders();
        headers.set("auth-token", authToken);
        final HttpEntity<Void> request = new HttpEntity<>(null, headers);

        this.restTemplate.postForEntity("/logout", request, Void.class);
        Assertions.assertFalse(tokenRepository.existsById("123"));
    }

    @Test
    public void testUploadFile() {
        final String authToken = "123";
        tokenRepository.save(new TokenEntity(authToken));

        final HttpHeaders headers = new HttpHeaders();
        headers.set("auth-token", authToken);

        final MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("file", new ClassPathResource("anotherFile2.txt"));

        final HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(parts, headers);

        this.restTemplate.postForEntity("/file?filename=anotherFile2.txt", request, Void.class);

        final Optional<FileEntity> fileInRepository = fileRepository.findById("anotherFile2.txt");
        Assertions.assertTrue(fileInRepository.isPresent());
        Assertions.assertEquals(new FileEntity("anotherFile2.txt", new byte[]{49, 51, 50}), fileInRepository.get());
    }

    @Test
    public void testDeleteFile() {
        fileRepository.save(new FileEntity("anotherFile2.txt", new byte[]{49, 51, 50}));

        final String authToken = "123";
        tokenRepository.save(new TokenEntity(authToken));

        final HttpHeaders headers = new HttpHeaders();
        headers.set("auth-token", authToken);

        final HttpEntity<Void> request = new HttpEntity<>(null, headers);

        this.restTemplate.exchange("/file?filename=anotherFile2.txt", HttpMethod.DELETE, request, Void.class);

        Assertions.assertFalse(fileRepository.existsById("anotherFile2.txt"));
    }

    @Test
    public void testGetFile() {
        fileRepository.save(new FileEntity("anotherFile2.txt", new byte[]{49, 51, 50}));

        final String authToken = "123";
        tokenRepository.save(new TokenEntity(authToken));

        final HttpHeaders headers = new HttpHeaders();
        headers.set("auth-token", authToken);

        final HttpEntity<Void> request = new HttpEntity<>(null, headers);

        final ResponseEntity<byte[]> result = this.restTemplate.exchange("/file?filename=anotherFile2.txt", HttpMethod.GET, request, byte[].class);

        Assertions.assertNotNull(result.getBody());
        Assertions.assertArrayEquals(new byte[]{49, 51, 50}, result.getBody());
    }

    @Test
    public void testEditFile() {
        fileRepository.save(new FileEntity("anotherFile2.txt", new byte[]{49, 51, 50}));

        final String authToken = "123";
        tokenRepository.save(new TokenEntity(authToken));

        final HttpHeaders headers = new HttpHeaders();
        headers.set("auth-token", authToken);

        final HttpEntity<FileNameInRequest> request = new HttpEntity<>(new FileNameInRequest("anotherFile.txt"), headers);

        this.restTemplate.exchange("/file?filename=anotherFile2.txt", HttpMethod.PUT, request, Void.class);

        Assertions.assertFalse(fileRepository.existsById("anotherFile2.txt"));
        final Optional<FileEntity> fileInRepository = fileRepository.findById("anotherFile.txt");
        Assertions.assertTrue(fileInRepository.isPresent());
        Assertions.assertEquals(new FileEntity("anotherFile.txt", new byte[]{49, 51, 50}), fileInRepository.get());
    }

    @Test
    public void testGetFileList() {
        fileRepository.save(new FileEntity("anotherFile2.txt", new byte[]{49, 51, 50}));

        final String authToken = "123";
        tokenRepository.save(new TokenEntity(authToken));

        final HttpHeaders headers = new HttpHeaders();
        headers.set("auth-token", authToken);

        final HttpEntity<Void> request = new HttpEntity<>(null, headers);

        final ResponseEntity<Object> result = this.restTemplate.exchange("/list?limit=10", HttpMethod.GET, request, Object.class);

        Assertions.assertNotNull(result.getBody());
        Assertions.assertEquals("[{filename=anotherFile2.txt, size=3}]", result.getBody().toString());
    }
}