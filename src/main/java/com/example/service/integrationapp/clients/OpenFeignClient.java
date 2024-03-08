package com.example.service.integrationapp.clients;

import com.example.service.integrationapp.model.EntityModel;
import com.example.service.integrationapp.model.UpsertEntityRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "OpenFeignClient", url = "${app.integration.base-url}")
public interface OpenFeignClient {
    @PostMapping(value = "api/v1/file/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String uploadFile(@RequestPart("file")MultipartFile file);

    @GetMapping(value = "api/v1/file/download/{filename}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    Resource downloadFile(@PathVariable String filename);

    @GetMapping(value = "api/v1/entity")
    List<EntityModel> getEntityList();

    @GetMapping(value = "api/v1/entity/{name}")
    EntityModel getEntityByName(@PathVariable("name") String name);

    @PostMapping(value = "api/v1/entity")
    EntityModel createEntity(UpsertEntityRequest request);

    @PutMapping(value = "api/v1/entity/{id}")
    EntityModel updateEntity(@PathVariable("id") UUID id, UpsertEntityRequest request);

    @DeleteMapping(value = "api/v1/entity/{id}")
    void deleteEntityById(@PathVariable("id") UUID id);


}
