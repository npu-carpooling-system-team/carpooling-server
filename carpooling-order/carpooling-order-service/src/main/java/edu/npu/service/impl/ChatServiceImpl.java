package edu.npu.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.common.RedisConstants;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.dto.AddMessageDto;
import edu.npu.entity.Chat;
import edu.npu.entity.LoginAccount;
import edu.npu.entity.User;
import edu.npu.feignClient.UserServiceClient;
import edu.npu.mapper.ChatMapper;
import edu.npu.service.ChatService;
import edu.npu.vo.MessageListItem;
import edu.npu.vo.R;
import edu.npu.vo.ToUserVo;
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
    private ChatMapper chatMapper;

    @Resource
    private UserServiceClient userServiceClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public R addMessage(AddMessageDto addMessageDto, LoginAccount loginAccount) {
        // 存到MySQL
        Long fromUserId = userServiceClient.getUserByAccountUsername(
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
            return R.error(ResponseCodeEnum.SERVER_ERROR, "保存聊天记录失败");
        }
        // 将消息通知存入Redis 我的想法是用一个set
        // key值为 communication:notice:{toUserId} value在set中加入fromUserId
        stringRedisTemplate.opsForSet().add(
                RedisConstants.MESSAGE_NOTICE_KEY + toUserId,
                fromUserId.toString()
        );
        return R.ok();
    }

    @Override
    public R getMessage(LoginAccount loginAccount) {
        List<MessageListItem> list = new ArrayList<>();
        User currUser = userServiceClient.getUserByAccountUsername(
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
            boolean hasNewMessage = false;
            // 从Redis中删除通知信息
            if (Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(
                    RedisConstants.MESSAGE_NOTICE_KEY + currUser.getId(),
                    fromUserId.toString()
            ))) {

                stringRedisTemplate.opsForSet().remove(
                        RedisConstants.MESSAGE_NOTICE_KEY + currUser.getId(),
                        fromUserId.toString()
                );
                hasNewMessage = true;
            } else if (Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(
                    RedisConstants.MESSAGE_NOTICE_KEY + currUser.getId(),
                    toUserId.toString()
            ))) {
                stringRedisTemplate.opsForSet().remove(
                        RedisConstants.MESSAGE_NOTICE_KEY + currUser.getId(),
                        toUserId.toString()
                );
                hasNewMessage = true;
            }
            // 构建
            User oppositeUser = userServiceClient.getUserById(
                    fromUserId.equals(currUser.getId())
                            ? toUserId : fromUserId
            );
            ToUserVo toUserVo = ToUserVo.builder()
                    .id(oppositeUser.getId())
                    .username(oppositeUser.getUsername())
                    .avatar(oppositeUser.getUserImage())
                    .build();
            MessageListItem messageListItem =
                    MessageListItem.builder()
                            .hasNewMessage(hasNewMessage)
                            .toUserVo(toUserVo)
                            .chats(item)
                            .build();
            list.add(messageListItem);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        return R.ok(result);
    }

    /**
     * 接收XXL-JOB的分片调度 删除已读且过期的聊天记录
     *
     * @param shardIndex 分片索引
     * @param shardTotal 分片总数
     * @param count      一次任务删除的数量
     * @return 是否删除成功
     */
    @Override
    public boolean deleteChatRecord(int shardIndex, int shardTotal, int count) {
        // 根据给出的分片索引和分片总数,计算出需要删除的聊天记录的id范围
        // 例如:分片索引为0,分片总数为2,则需要删除id为1,3,5,7,9...的聊天记录
        List<Chat> chats = chatMapper.selectListByShardIndex(shardIndex, shardTotal, count);
        // 遍历 判断时间与是否已读(redis) 若已读且过期则删除
        Date now = new Date();
        for (Chat chat : chats) {
            // 判断是否已读 如果存在键值对则说明未读 跳过
            if (Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(
                    RedisConstants.MESSAGE_NOTICE_KEY + chat.getToUserId(),
                    chat.getFromUserId().toString()
            ))) {
                continue;
            }
            // 判断是否过期 使用DateUtil
            if (chat.getSendTime().before(
                    DateUtil.offset(now, DateField.DAY_OF_YEAR, -7))) {
                chatMapper.deleteById(chat.getId());
            }
        }
        return true;
    }
}

