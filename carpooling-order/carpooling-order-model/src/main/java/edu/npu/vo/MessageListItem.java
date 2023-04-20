package edu.npu.vo;

import edu.npu.entity.Chat;
import lombok.Builder;

import java.util.List;

@Builder
public record MessageListItem(
        boolean hasNewMessage,
        // 聊天对象的User表ID
        ToUserVo toUserVo,
        // 聊天内容
        List<Chat> chats
) {
}
