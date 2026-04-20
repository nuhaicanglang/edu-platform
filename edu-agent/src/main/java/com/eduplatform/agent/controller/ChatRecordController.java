package com.eduplatform.agent.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eduplatform.agent.domain.entity.AiChatRecord;
import com.eduplatform.agent.mapper.AiChatRecordMapper;
import com.eduplatform.common.core.domain.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 对话记录查询控制器
 */
@RestController
@RequestMapping("/agent/chat-record")
@RequiredArgsConstructor
public class ChatRecordController {

    private final AiChatRecordMapper chatRecordMapper;

    /** 查询当前用户的对话记录（分页，最新在前） */
    @GetMapping("/my")
    public R<Map<String, Object>> myRecords(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        if (userId == null) return R.ok(Map.of("records", List.of(), "total", 0));

        Page<AiChatRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AiChatRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatRecord::getUserId, userId)
               .orderByDesc(AiChatRecord::getCreateTime);
        Page<AiChatRecord> result = chatRecordMapper.selectPage(page, wrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        return R.ok(data);
    }

    /** 查询所有对话记录（教师/管理员用，分页） */
    @GetMapping("/all")
    public R<Map<String, Object>> allRecords(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        Page<AiChatRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AiChatRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AiChatRecord::getCreateTime);
        Page<AiChatRecord> result = chatRecordMapper.selectPage(page, wrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        return R.ok(data);
    }

    /** 统计对话数量 */
    @GetMapping("/stats")
    public R<Map<String, Object>> stats(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Map<String, Object> stats = new HashMap<>();
        LambdaQueryWrapper<AiChatRecord> allWrapper = new LambdaQueryWrapper<>();
        stats.put("totalRecords", chatRecordMapper.selectCount(allWrapper));

        if (userId != null) {
            LambdaQueryWrapper<AiChatRecord> myWrapper = new LambdaQueryWrapper<>();
            myWrapper.eq(AiChatRecord::getUserId, userId);
            stats.put("myRecords", chatRecordMapper.selectCount(myWrapper));
        }
        return R.ok(stats);
    }
}
