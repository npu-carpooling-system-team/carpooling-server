package edu.npu.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayUserInfoShareRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayUserInfoShareResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.dto.BindAlipayCallbackDto;
import edu.npu.dto.GetUserInfoDto;
import edu.npu.dto.PutUserInfoDto;
import edu.npu.entity.Driver;
import edu.npu.entity.LoginAccount;
import edu.npu.entity.User;
import edu.npu.exception.CarpoolingException;
import edu.npu.mapper.DriverMapper;
import edu.npu.mapper.LoginAccountMapper;
import edu.npu.mapper.UserMapper;
import edu.npu.service.UserService;
import edu.npu.util.JwtTokenProvider;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wangminan
 * @description 针对表【user(用户表,用于记录用户的详细信息)】的数据库操作Service实现
 * @createDate 2023-04-17 11:23:58
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private LoginAccountMapper loginAccountMapper;

    @Resource
    @Lazy
    private JwtTokenProvider jwtTokenProvider;

    @Resource
    private AlipayClient alipayClient;

    @Resource
    private DriverMapper driverMapper;


    @Override
    public String bindAlipayToUser(BindAlipayCallbackDto bindAlipayCallbackDto) {
        String token = bindAlipayCallbackDto.state();
        AlipaySystemOauthTokenRequest tokenRequest = new AlipaySystemOauthTokenRequest();
        tokenRequest.setGrantType("authorization_code");
        tokenRequest.setCode(bindAlipayCallbackDto.auth_code());
        AlipaySystemOauthTokenResponse tokenResponse;
        log.info(alipayClient.toString());
        try {
            tokenResponse = alipayClient.execute(tokenRequest);
        } catch (AlipayApiException e) {
            throw new CarpoolingException("调用获取支付宝AK接口失败");
        }
        if (tokenResponse.isSuccess()) {
            String accessToken = tokenResponse.getAccessToken();
            AlipayUserInfoShareRequest alipayIdRequest = new AlipayUserInfoShareRequest();
            AlipayUserInfoShareResponse alipayIdResponse;
            try {
                alipayIdResponse = alipayClient.execute(alipayIdRequest, accessToken);
            } catch (AlipayApiException e) {
                throw new CarpoolingException("调用获取支付宝ID接口失败");
            }
            if (alipayIdResponse.isSuccess()) {
                // 支付宝的工作完成了 现在需要把支付宝ID和User表的User绑定
                // 因为接口是从支付宝回调来的 所以没有登录状态 需要从state读到的token字段中获取信息
                String username = jwtTokenProvider.extractUsername(token);
                LoginAccount loginAccount =
                        // 直接写 不注入service的loadUserByUsername方法
                        loginAccountMapper.selectOne(
                                new LambdaQueryWrapper<LoginAccount>()
                                        .eq(LoginAccount::getUsername, username));
                User user =
                        this.getOne(
                                new QueryWrapper<User>().lambda()
                                        .eq(User::getUsername, loginAccount.getUsername()));
                user.setAlipayId(alipayIdResponse.getUserId());
                // 没有参数 也可以加 但建议前端缓存环境变量
                return this.updateById(user) ?
                    "redirect:" +
                        "https://carpooling-client.wangminan.me" +
                            "/#/main/my/bind-alipay/success?token=" + token :
                    "redirect:" +
                        "https://carpooling-client.wangminan.me" +
                            "/#/main/my/bind-alipay/failure?token=" + token;
            } else {
                log.error("调用获取支付宝ID接口失败, resp: {}", alipayIdResponse);
            }
        } else {
            log.error("调用获取支付宝AK接口失败, resp: {}", tokenResponse);
        }
        return "redirect:https://carpooling-client.wangminan.me/#/main/mybind-alipay/failure";
    }

    @Override
    public R getInfo(LoginAccount loginAccount) {
        if (loginAccount == null) {
            return R.error(ResponseCodeEnum.FORBIDDEN, "当前用户未登录");
        }
        User user = this.getOne(
                new QueryWrapper<User>()
                        .lambda()
                        .eq(User::getUsername, loginAccount.getUsername()));
        if (user == null) {
            log.error("用户不存在");
            return R.error(ResponseCodeEnum.NOT_FOUND, "用户不存在");
        }
        Driver driver = null;
        if (user.getIsDriver()) {
            driver = driverMapper.selectOne(
                    new QueryWrapper<Driver>()
                            .lambda()
                            .eq(Driver::getDriverId, user.getId()));
        }
        GetUserInfoDto getUserInfoDto = new GetUserInfoDto(user, driver);
        Map<String, Object> result = new HashMap<>();
        result.put("result", getUserInfoDto);
        return R.ok(result);
    }

    @Override
    public R updateInfo(PutUserInfoDto putUserInfoDto) {
        User user = this.getOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, putUserInfoDto.username()));
        if (user == null) {
            log.error("所需更新的用户不存在");
            return R.error(ResponseCodeEnum.NOT_FOUND, "所需更新的用户不存在");
        }
        BeanUtils.copyProperties(putUserInfoDto, user);

        boolean userUpdate = this.updateById(user);

        if (putUserInfoDto.isDriver()) {
            Driver driver = driverMapper.selectOne(
                    new QueryWrapper<Driver>()
                            .lambda()
                            .eq(Driver::getDriverId, user.getId()));
            int driverUpdate;
            if (driver == null) {
                driver = new Driver();
                BeanUtils.copyProperties(putUserInfoDto, driver);
                driverUpdate = driverMapper.insert(driver);
            } else {
                BeanUtils.copyProperties(putUserInfoDto, driver);
                driverUpdate = driverMapper.updateById(driver);
            }
            if (userUpdate && driverUpdate == 1)
                return R.ok("用户信息更新成功");
            else
                return R.error(ResponseCodeEnum.SERVER_ERROR, "数据库更新用户信息失败");
        } else {
            // driver表中查询是否存在该用户 如果存在则删除
            Driver driver = driverMapper.selectOne(
                    new LambdaQueryWrapper<Driver>()
                            .eq(Driver::getDriverId, user.getId()));
            if (driver != null) {
                driverMapper.deleteById(driver);
            }
            if (userUpdate)
                return R.ok("用户信息更新成功");
            else
                return R.error(ResponseCodeEnum.SERVER_ERROR, "数据库更新用户信息失败");
        }

    }


    @Override
    public R deleteAccount(LoginAccount loginAccount) {
        if (loginAccount == null){
            return R.error(ResponseCodeEnum.FORBIDDEN, "当前用户未登录");
        }
        User user = this.getOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, loginAccount.getUsername()));
        if (user == null) {
            log.error("所需删除的用户不存在");
            return R.error(ResponseCodeEnum.NOT_FOUND, "所需删除的用户不存在");
        }

        this.removeById(user);

        loginAccountMapper.deleteById(loginAccount);

        if (user.getIsDriver()) {
            Driver driver = driverMapper.selectOne(
                    new LambdaQueryWrapper<Driver>()
                            .eq(Driver::getDriverId, user.getId()));
            driverMapper.deleteById(driver);
        }

        user = this.getOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, loginAccount.getUsername()));
        if (user == null) {
            return R.ok("账号删除成功");
        } else {
            return R.error(ResponseCodeEnum.SERVER_ERROR, "数据库删除失败");
        }
    }

    @Override
    public Driver getDriverByAccountUsername(String username) {
        User user = this.getOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username));
        if (user == null) {
            log.error("用户不存在");
            return null;
        }
        if (!user.getIsDriver()) {
            log.error("用户不是司机");
            return null;
        }
        Driver driver = driverMapper.selectOne(
                new LambdaQueryWrapper<Driver>()
                        .eq(Driver::getDriverId, user.getId()));
        if (driver == null) {
            log.error("司机不存在");
            return null;
        }
        log.info(driver.getDriversLicenseType());
        return driver;
    }

    @Override
    public boolean banUser(User user) {
        LoginAccount loginAccount = loginAccountMapper.selectOne(
                new LambdaQueryWrapper<LoginAccount>()
                        .eq(LoginAccount::getUsername, user.getUsername())
        );
        if (loginAccount == null) {
            log.error("用户不存在");
            return false;
        }
        deleteAccount(loginAccount);
        return true;
    }
}




