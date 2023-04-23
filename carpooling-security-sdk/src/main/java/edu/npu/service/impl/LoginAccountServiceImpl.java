package edu.npu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.entity.LoginAccount;
import edu.npu.mapper.LoginAccountMapper;
import edu.npu.service.LoginAccountService;
import org.springframework.stereotype.Service;

/**
* @author wangminan
* @description 针对表【login_account(用于用户名密码登录所需表格)】的数据库操作Service实现
* @createDate 2023-04-23 15:57:40
*/
@Service
public class LoginAccountServiceImpl extends ServiceImpl<LoginAccountMapper, LoginAccount>
    implements LoginAccountService{

}




