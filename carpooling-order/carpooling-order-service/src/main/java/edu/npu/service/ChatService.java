package edu.npu.service;

import edu.npu.dto.AddMessageDto;
import edu.npu.entity.Chat;
import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.entity.LoginAccount;
import edu.npu.vo.R;

/**
* @author wangminan
* @description 针对表【chat(用户与司机交流表)】的数据库操作Service
* @createDate 2023-04-20 14:21:40
*/
public interface ChatService extends IService<Chat> {

    R addMessage(AddMessageDto addMessageDto, LoginAccount loginAccount);

    R getMessage(LoginAccount loginAccount);

    boolean deleteChatRecord(int shardIndex, int shardTotal, int count);
}
