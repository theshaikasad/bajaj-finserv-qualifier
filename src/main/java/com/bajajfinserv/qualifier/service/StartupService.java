package com.bajajfinserv.qualifier.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class StartupService implements CommandLineRunner {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void run(String... args) throws Exception {
        try {
            String webhookResponse = generateWebhook();
            JsonNode responseNode = objectMapper.readTree(webhookResponse);
            String webhookUrl = responseNode.get("webhook").asText();
            String accessToken = responseNode.get("accessToken").asText();
            String finalQuery = getSqlSolution();
            submitSolution(webhookUrl, accessToken, finalQuery);
            System.out.println("SUCCESS! Solution submitted!");
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String generateWebhook() throws Exception {
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String requestBody = "{\"name\":\"Shaik Mohammed Asad\",\"regNo\":\"22BCT0202\",\"email\":\"shaikasad17@gmail.com\"}";
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        return response.getBody();
    }

    private String getSqlSolution() {
        return "SELECT e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME, COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT FROM EMPLOYEE e1 INNER JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID LEFT JOIN EMPLOYEE e2 ON e1.DEPARTMENT = e2.DEPARTMENT AND e2.DOB > e1.DOB GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME ORDER BY e1.EMP_ID DESC";
    }

    private void submitSolution(String webhookUrl, String accessToken, String finalQuery) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", accessToken);
        String requestBody = "{\"finalQuery\":\"" + finalQuery + "\"}";
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        restTemplate.postForEntity(webhookUrl, request, String.class);
    }
}
