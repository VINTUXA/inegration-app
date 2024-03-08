package com.example.service.integrationapp.clients;

import com.example.service.integrationapp.model.EntityModel;
import com.example.service.integrationapp.model.UpsertEntityRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OkHttpClientSender {
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${app.integration.base-url}")
    private String baseUrl;

    @SneakyThrows
    public String uploadFile(MultipartFile file){
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(MediaType.parse("application/octet-stream"), file.getBytes()));

        Request request = new Request.Builder()
                .url(baseUrl + "/api/v1/file/upload")
                .header("Content-type", "multipart/form-data")
                .post(builder.build())
                .build();

        try (Response response = httpClient.newCall(request).execute()){
            if (!response.isSuccessful()){
                log.error("Error trying to request to upload file");
                return "Error";
            }

            return new String(response.body().bytes());
        }
    }

    public Resource downloadFile(String filename){
        Request request = new Request.Builder()
                .url(baseUrl + "/api/v1/download/" + filename)
                .header("Accept", "application/octet-stream")
                .get()
                .build();
        try (Response response = httpClient.newCall(request).execute()){
            if (!response.isSuccessful()){
                log.error("Error trying to download file!");
                return null;
            }
            return new ByteArrayResource(response.body().bytes());
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public List<EntityModel> getEntityList() {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/v1/entity")
                .build();

        return processResponse(request, new TypeReference<>(){});

    }
    public EntityModel getEntityByName(String name){
        Request request = new Request.Builder()
                .url(baseUrl + "/api/v1/entity/" + name)
                .build();
        return processResponse(request, new TypeReference<>() {});
    }

    @SneakyThrows
    public EntityModel createEntity(UpsertEntityRequest request){
        MediaType JSON = MediaType.get("application/json;charset=utf-8");
        String requestBody = objectMapper.writeValueAsString(request);
        RequestBody body = RequestBody.create(requestBody, JSON);

        Request httpRequest = new Request.Builder()
                .url(baseUrl + "/api/v1/entity")
                .post(body)
                .build();
        return processResponse(httpRequest, new TypeReference<>() {});
    }

    @SneakyThrows
    public EntityModel updateEntity(UUID id, UpsertEntityRequest request){
        MediaType JSON = MediaType.get("application/json;charset=utf-8");
        String requestBody = objectMapper.writeValueAsString(request);
        RequestBody body = RequestBody.create(requestBody, JSON);

        Request httpRequest = new Request.Builder()
                .url(baseUrl + "/api/v1/entity/" + id)
                .put(body)
                .build();
        return processResponse(httpRequest, new TypeReference<>() {});
    }

    @SneakyThrows
    public void deleteEntityById(UUID id){
        Request request = new Request.Builder()
                .url(baseUrl + "/api/v1/entity/" + id)
                .delete()
                .build();
        try (Response response = httpClient.newCall(request).execute()){
            if (!response.isSuccessful()){
                throw new RuntimeException("Unexpected response code! " + response);
            }
        }
    }
    @SneakyThrows
    private <T> T processResponse(Request request, TypeReference<T> typeReference){
        try (Response response = httpClient.newCall(request).execute()){
            if (!response.isSuccessful()){
                throw new RuntimeException("Unexpected response code: " + response.code());
            }
            ResponseBody responseBody  = response.body();

            if (responseBody != null) {
                String strinBody =responseBody.string();
                return objectMapper.readValue(strinBody, typeReference);
            } else {
                throw new RuntimeException("Response body is empty!");
            }
        }
    }
}
