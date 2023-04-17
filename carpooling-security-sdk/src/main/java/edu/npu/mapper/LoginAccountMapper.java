package edu.npu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.npu.entity.LoginAccount;
import org.apache.ibatis.annotations.Mapper;

/**
* @author wangminan
* @description 针对表【login_account(用于用户名密码登录所需表格)】的数据库操作Mapper
* @createDate 2023-04-17 10:57:34
* @Entity edu.npu.entity.LoginAccount
*/
@Mapper
public interface LoginAccountMapper extends BaseMapper<LoginAccount> {

}




