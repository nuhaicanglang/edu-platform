package com.eduplatform.knowledge.embedding;

import com.eduplatform.knowledge.config.RagProperties;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OllamaEmbeddingClientTest {

    private HttpServer server;
    private String baseUrl;
    private final AtomicReference<String> requestBody = new AtomicReference<>();

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/embed", exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = "{\"model\":\"bge-m3:latest\",\"embeddings\":[[0.1,0.2,0.3]]}"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
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
    void sendsBatchRequestAndReturnsEmbeddings() {
        OllamaEmbeddingClient client = new OllamaEmbeddingClient(properties(3));

        List<float[]> result = client.embedAll(List.of("数据结构"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).containsExactly(0.1f, 0.2f, 0.3f);
        assertThat(requestBody.get()).contains("bge-m3:latest", "数据结构", "keep_alive");
    }

    @Test
    void rejectsUnexpectedEmbeddingDimension() {
        OllamaEmbeddingClient client = new OllamaEmbeddingClient(properties(4));

        assertThatThrownBy(() -> client.embedAll(List.of("数据结构")))
                .isInstanceOf(EmbeddingUnavailableException.class)
                .hasMessageContaining("向量维度");
    }

    private RagProperties properties(int dimensions) {
        return new RagProperties(
                "bge-m3:latest", dimensions, baseUrl,
                Duration.ofSeconds(2), Duration.ofSeconds(5), 16, Duration.ofMinutes(30));
    }
}
