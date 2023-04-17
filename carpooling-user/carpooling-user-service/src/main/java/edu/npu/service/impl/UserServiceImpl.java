package edu.npu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.entity.User;
import edu.npu.service.UserService;
import edu.npu.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author wangminan
* @description 针对表【user(用户表,用于记录用户的详细信息)】的数据库操作Service实现
* @createDate 2023-04-17 11:23:58
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




