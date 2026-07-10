package com.eduplatform.agent.service;

import com.eduplatform.agent.llm.LlmService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntelligentQAServiceTest {

    @Mock LlmService llmService;
    @Mock StringRedisTemplate redisTemplate;
    @Mock ValueOperations<String, String> values;

    private IntelligentQAService service;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(values);
        when(llmService.chatSimple(anyString(), anyString())).thenReturn("回答");
        service = new IntelligentQAService(llmService, redisTemplate);
    }

    @Test
    void conversationHistoryKeyContainsUserAndCourse() {
        service.ask("问题", "课程上下文", 12L, 7L);

        verify(values).get("qa:history:7:course:12");
    }

    @Test
    void redisOutageDoesNotBreakQuestionAnswering() {
        when(values.get("qa:history:7:course:12"))
                .thenThrow(new RedisConnectionFailureException("down"));

        String answer = service.ask("问题", "课程上下文", 12L, 7L);

        assertThat(answer).isEqualTo("回答");
    }
}
