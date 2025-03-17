package com.sclera.rule_engine.syslog.service;

import com.alibaba.fastjson2.JSON;

import com.sclera.rule_engine.syslog.dto.ResponseDTO;
import com.sclera.rule_engine.syslog.dto.TenantDTO;
import com.sclera.rule_engine.syslog.utils.ScleraWebClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class WebClientService {

    @Autowired
    public ScleraWebClient scleraWebClient;


    @Value("${sclera.server.bff.url}")
    private String BFF_SERVER_URL;




    public Map<String, String> constructToken(HttpServletRequest request) {
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", request.getHeader("Authorization"));
        log.info("Constructing Token.Endpoint:{}", request.getRequestURI());
        return header;
    }

    public TenantDTO getAllTenants(String issuer) {
        Map<String, String> params = new HashMap<>();
        params.put("issuer", issuer);

        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.GET,
                BFF_SERVER_URL + "/api/getTenantByIssuer",
                params,
                null,
                null,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                20000,
                20000,
                10000);

        System.out.println(response);

        ResponseDTO responseBody = response.getBody();
        if (responseBody != null && responseBody.getData() != null) {
            String jsonString = JSON.toJSONString(responseBody.getData());
            return JSON.parseObject(jsonString, TenantDTO.class);
        } else {
            return new TenantDTO(); // Return an empty TenantDTO
        }
    }


}