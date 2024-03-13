package com.example.service.integrationapp;

import com.example.service.integrationapp.model.EntityModel;
import com.example.service.integrationapp.model.UpsertEntityRequest;
import com.example.service.integrationapp.repository.DatabaseEntityRepository;
import com.example.service.integrationapp.service.DatabaseEntityService;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
//import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.testcontainers.utility.DockerImageName;
//import com.github.to
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@SpringBootTest
@Sql("classpath:db/init.sql") // используется для выполнения sql скриптов
@Transactional // для управления транзакциями
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Testcontainers // интегрируем тест контейнеры. все аннотиррованные тестконтейнером автоматом запускают докер перед тестом
public class AbstractTest {

    public static final UUID UPDATED_ID = UUID.fromString("fd2d8f0e-d000-4e4e-bf63-0687fbfef2b1");
    public static final Instant ENTITY_DATE = Instant.parse("2100-01-01T00:00:00.Z");

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected RedisTemplate<String, Object> redisTemplate;
    @Autowired
    protected ObjectMapper objectMapper;
//            = new ObjectMapper();
//        .registerModules(new JavaTimeModule())
//        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);;;
//    objectMapper.registerModule(new JavaTimeModule());
    @Autowired
    protected DatabaseEntityService databaseEntityService;
    @Autowired
    protected DatabaseEntityRepository databaseEntityRepository;

    @RegisterExtension // регистрируем расширения вайрмок
    protected static WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();
    protected static PostgreSQLContainer postgreSQLContainer; // пример без использования jiunit, инициализируем в статическом блоке и запускаем его

    @Container
    protected static final RedisContainer REDIS_CONTAINER =  new RedisContainer(DockerImageName.parse("redis:7.0.12"))
            .withExposedPorts(6379)
            .withReuse(true); // не будет удален после выполнения тестов

    static {
        DockerImageName postgres = DockerImageName.parse("postgres:12.3");

        postgreSQLContainer = (PostgreSQLContainer) new PostgreSQLContainer(postgres)
                .withReuse(true);

        postgreSQLContainer.start();
    }

    @DynamicPropertySource
    public static void registerProperties(DynamicPropertyRegistry registry){
        String jdbcUrl = postgreSQLContainer.getJdbcUrl();

        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.url",() -> jdbcUrl);

        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379).toString());

        registry.add("app.integration.base-url", wireMockServer::baseUrl);
    }

    @BeforeEach // удаляем все ключи
    public void before() throws Exception{
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();

        stubClient();
    }

    @AfterEach
    public void afterEach(){
        wireMockServer.resetAll();
    }

    private void stubClient() throws Exception{
        List<EntityModel> findAllResponseBody = new ArrayList<>();

        findAllResponseBody.add(new EntityModel(UUID.randomUUID(), "Entity 1", Instant.now()));
        findAllResponseBody.add(new EntityModel(UUID.randomUUID(), "Entity 2", Instant.now()));

        wireMockServer.stubFor(WireMock.get("/api/v1/entity")
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(findAllResponseBody))
                        .withStatus(200)));

        EntityModel findByNameResponseBody = new EntityModel(UUID.randomUUID(), "someEntity", ENTITY_DATE);

        wireMockServer.stubFor(WireMock.get("/api/v1/entity")
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(findByNameResponseBody))
                        .withStatus(200)));

        UpsertEntityRequest createRequest = new UpsertEntityRequest();
        createRequest.setName("newEntity");
        EntityModel createResponseBody = new EntityModel(UUID.randomUUID(), "newEntity", ENTITY_DATE);

        wireMockServer.stubFor(WireMock.post("/api/v1/entity")
                .withRequestBody(equalToJson(objectMapper.writeValueAsString(createRequest)))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(createResponseBody))
                        .withStatus(201)));

        UpsertEntityRequest updatedRequest = new UpsertEntityRequest();
        updatedRequest.setName("updatedRequest");

        EntityModel updatedResponseBody = new EntityModel(UPDATED_ID, "updatedName", ENTITY_DATE);

        wireMockServer.stubFor(WireMock.put("/api/v1/entity/" + UPDATED_ID)
                .withRequestBody(equalToJson(objectMapper.writeValueAsString(updatedRequest)))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
//                        .withBody(objectMapper.writeValueAsString(updatedResponseBody))
                        .withStatus(201)));

        wireMockServer.stubFor(WireMock.delete("/api/v1/entity/" + UPDATED_ID)
                .willReturn(aResponse().withStatus(204)));


    }
}
