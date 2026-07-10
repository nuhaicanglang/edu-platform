package com.eduplatform.agent.client;

import com.eduplatform.agent.domain.dto.KnowledgeRetrievalRequest;
import com.eduplatform.agent.domain.dto.KnowledgeRetrievalResponse;
import com.eduplatform.common.core.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/** 访问知识服务的受保护检索接口。 */
@FeignClient(name = "edu-knowledge")
public interface KnowledgeClient {

    @PostMapping("/knowledge/retrieve")
    R<KnowledgeRetrievalResponse> retrieve(
            @RequestBody KnowledgeRetrievalRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role);
}
