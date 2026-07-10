package com.eduplatform.agent.security;

import org.springframework.stereotype.Component;

/** 将服务端认证身份短暂绑定到同步 Agent 工具调用线程。 */
@Component
public class AgentRequestContext {

    private final ThreadLocal<Identity> current = new ThreadLocal<>();

    public Scope open(Long userId, String role, Long courseId) {
        Identity previous = current.get();
        current.set(new Identity(userId, role, courseId));
        return () -> {
            if (previous == null) current.remove();
            else current.set(previous);
        };
    }

    public Identity require() {
        Identity identity = current.get();
        if (identity == null) throw new IllegalStateException("Agent 身份上下文缺失");
        return identity;
    }

    public record Identity(Long userId, String role, Long courseId) {}

    @FunctionalInterface
    public interface Scope extends AutoCloseable {
        @Override
        void close();
    }
}
