package com.eduplatform.knowledge.search;

import com.eduplatform.knowledge.config.RagProperties;
import com.eduplatform.knowledge.config.RagSearchProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class ElasticsearchKnowledgeVectorIndexTest {

    private HttpServer server;
    private String baseUrl;
    private final List<CapturedRequest> requests = new CopyOnWriteArrayList<>();

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", exchange -> {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            requests.add(new CapturedRequest(exchange.getRequestMethod(), exchange.getRequestURI().getPath(), body));
            int status = "HEAD".equals(exchange.getRequestMethod()) ? 404 : 200;
            byte[] response = (exchange.getRequestURI().getPath().equals("/_bulk")
                    ? "{\"errors\":false}" : "{\"acknowledged\":true}")
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, "HEAD".equals(exchange.getRequestMethod()) ? -1 : response.length);
            if (!"HEAD".equals(exchange.getRequestMethod())) exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void createsDenseVectorMappingAndIndexesByChunkId() {
        ElasticsearchKnowledgeVectorIndex index = new ElasticsearchKnowledgeVectorIndex(
                searchProperties(), embeddingProperties(), new ObjectMapper().findAndRegisterModules());

        index.ensureIndex();
        index.upsertAll(List.of(new IndexedKnowledgeChunk(
                "31", 7L, 12L, 4, "数据结构基础", "前序遍历先访问根节点",
                new float[]{0.1f, 0.2f, 0.3f}, "bge-m3:latest", "hash", Instant.EPOCH)));

        assertThat(requests).anySatisfy(request -> {
            assertThat(request.method()).isEqualTo("PUT");
            assertThat(request.path()).isEqualTo("/knowledge-chunks-v1");
            assertThat(request.body()).contains("dense_vector", "\"dims\":3", "cosine");
        });
        assertThat(requests).anySatisfy(request -> {
            assertThat(request.path()).isEqualTo("/_bulk");
            assertThat(request.body()).contains("\"_id\":\"31\"", "前序遍历", "\"courseId\":\"12\"");
        });
    }

    private RagSearchProperties searchProperties() {
        return new RagSearchProperties(baseUrl, "knowledge-chunks-v1", "knowledge-chunks", 20, 5, 60);
    }

    private RagProperties embeddingProperties() {
        return new RagProperties(
                "bge-m3:latest", 3, "http://127.0.0.1:11434",
                Duration.ofSeconds(1), Duration.ofSeconds(1), 16, Duration.ofMinutes(30));
    }

    private record CapturedRequest(String method, String path, String body) {}
}
