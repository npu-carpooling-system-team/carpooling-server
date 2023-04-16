package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.dto.AlipayLoginCallbackDto;
import edu.npu.dto.CheckSmsCodeDto;
import edu.npu.dto.UserLoginDto;
import edu.npu.dto.UserRegisterDto;
import edu.npu.entity.LoginAccount;
import edu.npu.vo.R;

/**
* @author wangminan
* @description 针对表【login_account(用于用户名密码登录所需表格)】的数据库操作Service
* @createDate 2023-04-15 20:48:34
*/
public interface LoginAccountService extends IService<LoginAccount> {

    R registerUser(UserRegisterDto userRegisterDto);

    R login(UserLoginDto userLoginDto);

    R logout(LoginAccount loginAccount);

    R loginByPhone(CheckSmsCodeDto checkSmsCodeDto);

    String handleAlipayLogin(AlipayLoginCallbackDto alipayLoginCallbackDto);
}
