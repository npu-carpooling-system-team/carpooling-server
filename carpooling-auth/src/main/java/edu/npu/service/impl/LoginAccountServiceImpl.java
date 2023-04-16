package edu.npu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.dto.UserRegisterDto;
import edu.npu.entity.Driver;
import edu.npu.entity.LoginAccount;
import edu.npu.entity.User;
import edu.npu.mapper.DriverMapper;
import edu.npu.mapper.LoginAccountMapper;
import edu.npu.mapper.UserMapper;
import edu.npu.service.LoginAccountService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
* @author wangminan
* @description 针对表【login_account(用于用户名密码登录所需表格)】的数据库操作Service实现
* @createDate 2023-04-15 20:48:34
*/
@Service
public class LoginAccountServiceImpl extends ServiceImpl<LoginAccountMapper, LoginAccount>
    implements LoginAccountService{

    @Resource
    private UserMapper userMapper;

    @Resource
    private DriverMapper driverMapper;

    @Override
    public R registerUser(UserRegisterDto userRegisterDto) {
        if(!userRegisterDto.isDriver() && !userRegisterDto.isPassenger()){
            return R.error(ResponseCodeEnum.PreCheckFailed, "请至少选择一个角色");
        }
        if (userRegisterDto.isDriver()){
            // 接下来的字段都不允许为空，否则直接返回失败结果
            if (userRegisterDto.driversPersonalId().isBlank()
                    || userRegisterDto.driversLicenseId().isBlank()
                    || userRegisterDto.driversLicenseType().isBlank()
                    || userRegisterDto.driversVehicleType().isBlank()
                    || userRegisterDto.driversExpireDate().isBlank()
                    || userRegisterDto.driversPlateNo().isBlank()
            ){
                return R.error(ResponseCodeEnum.NotEnoughInformation, "司机注册信息不完整");
            }
        }
        // 开始处理注册信息
        if (userRegisterDto.isPassenger() && !userRegisterDto.isDriver()){
            // 只需要改动user表
            User user = new User();
            BeanUtils.copyProperties(userRegisterDto, user);
            int insert = userMapper.insert(user);
            return insert == 1 ? R.ok("注册成功") :
                    R.error(ResponseCodeEnum.ServerError, "注册失败");
        } else {
            // 需要同时改动user表和driver表
            User user = new User();
            BeanUtils.copyProperties(userRegisterDto, user);
            int insertUser = userMapper.insert(user);

            Driver driver = new Driver();
            driver.setDriverId(
                    userMapper.selectOne(
                    new QueryWrapper<User>().lambda()
                            .eq(User::getUsername, userRegisterDto.username()))
                            .getId());
            BeanUtils.copyProperties(userRegisterDto, driver);
            int insertDriver = driverMapper.insert(driver);

            return insertUser == 1 && insertDriver == 1 ? R.ok("注册成功") :
                    R.error(ResponseCodeEnum.ServerError, "注册失败");
        }
    }
}




