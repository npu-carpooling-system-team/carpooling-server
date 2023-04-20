package edu.npu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.dto.AddMessageDto;
import edu.npu.entity.Chat;
import edu.npu.entity.LoginAccount;
import edu.npu.entity.User;
import edu.npu.feignClient.UserServiceClient;
import edu.npu.service.ChatService;
import edu.npu.mapper.ChatMapper;
import edu.npu.vo.FromUserVo;
import edu.npu.vo.MessageListItem;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author wangminan
 * @description 针对表【chat(用户与司机交流表)】的数据库操作Service实现
 * @createDate 2023-04-20 14:21:40
 */
@Service
@Slf4j
public class ChatServiceImpl extends ServiceImpl<ChatMapper, Chat>
        implements ChatService {

    @Resource
    private UserServiceClient userServiceClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public R addMessage(AddMessageDto addMessageDto, LoginAccount loginAccount) {
        // 存到MySQL
        Long fromUserId = userServiceClient.getUserWithAccountUsername(
                loginAccount.getUsername()
        ).getId();
        Long toUserId = addMessageDto.toUserId();
        String message = addMessageDto.message();
        Chat chat = Chat.builder()
                .fromUserId(fromUserId)
                .toUserId(toUserId)
                .message(message)
                .build();
        boolean saveChatToDb = save(chat);
        if (!saveChatToDb) {
            log.error("保存聊天记录:{}失败,MySQL异常", chat);
            return R.error(ResponseCodeEnum.ServerError, "保存聊天记录失败");
        }
        // 将消息通知存入Redis 我的想法是用一个set
        // key值为 communication:notice:{toUserId} value在set中加入fromUserId
        stringRedisTemplate.opsForSet().add(
                "communication:notice:" + toUserId,
                fromUserId.toString()
        );
        return R.ok();
    }

    @Override
    public R getMessage(LoginAccount loginAccount) {
        List<MessageListItem> list = new ArrayList<>();
        User currUser = userServiceClient.getUserWithAccountUsername(
                loginAccount.getUsername()
        );
        // 检查chat表中的内容
        List<Chat> chatList = list(
                new LambdaQueryWrapper<Chat>()
                        .eq(Chat::getFromUserId, currUser.getId())
                        .or()
                        .eq(Chat::getToUserId, currUser.getId())
        );
        // 将对话根据对话者进行分组
        List<List<Chat>> chatGroup = new ArrayList<>();
        for (Chat chat : chatList) {
            Long fromUserId = chat.getFromUserId();
            Long toUserId = chat.getToUserId();
            // 从chatGroup中找到对应的聊天记录
            List<Chat> chatGroupItem = null;
            for (List<Chat> item : chatGroup) {
                if ((item.get(0).getFromUserId().equals(fromUserId)
                        && item.get(0).getToUserId().equals(toUserId))
                        || (item.get(0).getFromUserId().equals(toUserId)
                        && item.get(0).getToUserId().equals(fromUserId))
                ) {
                    chatGroupItem = item;
                    break;
                }
            }
            // 如果没有找到对应的聊天记录,则新建一个
            if (Objects.isNull(chatGroupItem)) {
                chatGroupItem = new ArrayList<>();
                chatGroup.add(chatGroupItem);
            }
            chatGroupItem.add(chat);
        }
        // 再遍历一次 结合Redis中的通知信息,构建MessageListItem
        for (List<Chat> item : chatGroup) {
            Chat chat = item.get(0);
            Long fromUserId = chat.getFromUserId();
            Long toUserId = chat.getToUserId();
            // 从Redis中获取通知信息
            boolean hasNewMessage =
                    Boolean.TRUE.equals(Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(
                            "communication:notice:" + currUser.getId(),
                            fromUserId.toString()
                    )) || Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(
                            "communication:notice:" + currUser.getId(),
                            toUserId.toString()
                    )));
            // 构建
            User oppositeUser = userServiceClient.getUserWithId(
                    fromUserId.equals(currUser.getId())
                            ? toUserId : fromUserId
            );
            FromUserVo fromUserVo = FromUserVo.builder()
                    .id(oppositeUser.getId())
                    .username(oppositeUser.getUsername())
                    .avatar(oppositeUser.getUserImage())
                    .build();
            MessageListItem messageListItem =
                    MessageListItem.builder()
                            .hasNewMessage(hasNewMessage)
                            .fromUserVo(fromUserVo)
                            .chats(item)
                            .build();
            list.add(messageListItem);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        return R.ok(result);
    }
}

