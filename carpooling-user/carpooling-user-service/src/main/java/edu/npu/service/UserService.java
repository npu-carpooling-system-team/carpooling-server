package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.dto.BindAlipayCallbackDto;
import edu.npu.dto.PutUserInfoDto;
import edu.npu.entity.LoginAccount;
import edu.npu.entity.User;
import edu.npu.vo.R;

/**
* @author wangminan
* @description 针对表【user(用户表,用于记录用户的详细信息)】的数据库操作Service
* @createDate 2023-04-17 11:23:58
*/
public interface UserService extends IService<User> {
    String bindAlipayToUser(BindAlipayCallbackDto bindAlipayCallbackDto);

    R getInfo(LoginAccount loginAccount);

    R updateInfo(PutUserInfoDto putUserInfoDto);

    R deleteAccount(LoginAccount loginAccount);
}
