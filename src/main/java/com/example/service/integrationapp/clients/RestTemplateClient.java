package com.example.service.integrationapp.clients;

import com.example.service.integrationapp.model.EntityModel;
import com.example.service.integrationapp.model.UpsertEntityRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RestTemplateClient {
    private final RestTemplate restTemplate;
    @Value("${app.integration.base-url}")
    private String baseUrl;

    public void uploadFile(MultipartFile file){
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        restTemplate.postForObject(baseUrl + "/api/v1/file/upload", requestEntity, String.class);
    }

    public Resource downloadFile(String filename){
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        ResponseEntity<Resource> response = restTemplate.exchange(
                baseUrl + "/api/v1/file/download/{filename}", HttpMethod.GET, new HttpEntity<>(headers),Resource.class,filename
        );

        return response.getBody();
    }

    public List<EntityModel> getEntityList(){
        ResponseEntity<List<EntityModel>> response = restTemplate.exchange(
                baseUrl + "/api/v1/entity", HttpMethod.GET, null, new ParameterizedTypeReference<List<EntityModel>>() {
                }
        );

        return response.getBody();
    }

    public EntityModel getEntityByName(String name){
        ResponseEntity<EntityModel> response = restTemplate.exchange(
                baseUrl + "/api/v1/entity/{name}", HttpMethod.GET, null, EntityModel.class, name
        );
        return response.getBody();
    }

    public EntityModel createEntity(UpsertEntityRequest request){
        HttpEntity<UpsertEntityRequest> requestEntity = new HttpEntity<>(request);
        ResponseEntity<EntityModel> response = restTemplate.exchange(
                baseUrl + "/api/v1/entity", HttpMethod.POST, requestEntity, EntityModel.class
        );
        return response.getBody();
    }

    public EntityModel updateEntity(UUID id, UpsertEntityRequest request){
        HttpEntity<UpsertEntityRequest> requestEntity = new HttpEntity<>(request);

        ResponseEntity<EntityModel> response = restTemplate.exchange(
                baseUrl + "/api/v1/entity/{id}", HttpMethod.POST, requestEntity, EntityModel.class, id
        );
        return response.getBody();
    }

    public void deleteEntityById(UUID id){
        restTemplate.exchange(
                baseUrl + "/api/v1/entity/{id}", HttpMethod.DELETE, null, Void.class, id
        );
    }
}
