package edu.npu.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayUserInfoShareRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayUserInfoShareResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.common.RedisConstants;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.common.RoleEnum;
import edu.npu.dto.AlipayLoginCallbackDto;
import edu.npu.dto.CheckSmsCodeDto;
import edu.npu.dto.UserLoginDto;
import edu.npu.dto.UserRegisterDto;
import edu.npu.entity.Driver;
import edu.npu.entity.LoginAccount;
import edu.npu.entity.User;
import edu.npu.mapper.DriverMapper;
import edu.npu.mapper.LoginAccountMapper;
import edu.npu.mapper.UserMapper;
import edu.npu.service.LoginAccountService;
import edu.npu.util.JwtTokenProvider;
import edu.npu.util.RsaUtil;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
* @author wangminan
* @description 针对表【login_account(用于用户名密码登录所需表格)】的数据库操作Service实现
* @createDate 2023-04-15 20:48:34
*/
@Service
@Slf4j
public class LoginAccountServiceImpl extends ServiceImpl<LoginAccountMapper, LoginAccount>
    implements LoginAccountService, UserDetailsService {

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private UserMapper userMapper;

    @Value("${var.rsa.private-key}")
    private String privateKey;

    @Resource
    private DriverMapper driverMapper;

    @Resource
    private JwtTokenProvider jwtTokenProvider;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private AlipayClient alipayClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R registerUser(UserRegisterDto userRegisterDto) {
        if(!userRegisterDto.isDriver() && !userRegisterDto.isPassenger()){
            return R.error(ResponseCodeEnum.PreCheckFailed, "请至少选择一个角色");
        }
        if (userRegisterDto.isDriver()){
            // 接下来的字段都不允许为空，否则直接返回失败结果
            if (userRegisterDto.driversPersonalId().isBlank()
                    || userRegisterDto.driversLicenseNo().isBlank()
                    || userRegisterDto.driversLicenseType().isBlank()
                    || userRegisterDto.driversVehicleType().isBlank()
                    || userRegisterDto.driversExpireDate().isBlank()
                    || userRegisterDto.driversPlateNo().isBlank()
            ){
                return R.error(ResponseCodeEnum.NotEnoughInformation, "司机注册信息不完整");
            }
        }
        // 开始处理注册信息
        // 添加到loginAccount表
        LoginAccount loginAccount = new LoginAccount();
        loginAccount.setUsername(userRegisterDto.username());
        // 使用RSA解密密码
        loginAccount.setPassword(
                passwordEncoder.encode(
                        RsaUtil.decrypt(privateKey, userRegisterDto.password())));
        loginAccount.setRole(RoleEnum.User.getValue());
        boolean saveLoginAccount = this.save(loginAccount);
        if (!saveLoginAccount){
            return R.error(ResponseCodeEnum.ServerError, "注册失败,请检查用户名是否重复");
        }

        // 只需要添加到user表
        if (userRegisterDto.isPassenger() && !userRegisterDto.isDriver()){
            // 只需要改动user表
            User user = new User();
            BeanUtils.copyProperties(userRegisterDto, user);
            int insert = userMapper.insert(user);
            return insert == 1 ? R.ok("注册成功") :
                    R.error(ResponseCodeEnum.ServerError, "注册失败,请检查用户名是否重复");
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
                    R.error(ResponseCodeEnum.ServerError, "注册失败,请检查用户名是否重复");
        }
    }

    @Override
    public R login(UserLoginDto userLoginDto) {
        try{
            LoginAccount loginAccount =
                    (LoginAccount) this.loadUserByUsername(userLoginDto.username());
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    loginAccount.getUsername(),
                                    RsaUtil.decrypt(privateKey, userLoginDto.password()),
                                    loginAccount.getAuthorities()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            Map<String, Object> result = genTokenWithLoginAccount(userLoginDto.username(), loginAccount);
            return R.ok(result);
        } catch (Exception e) {
            log.error("登录失败", e);
            return R.error(ResponseCodeEnum.ServerError, "登录失败");
        }
    }

    @Override
    public R logout(LoginAccount loginAccount) {
        // 将token从redis中删除 然后返回即可
        stringRedisTemplate.delete(RedisConstants.TOKEN_KEY_PREFIX + loginAccount.getUsername());
        return R.ok();
    }

    @Override
    public R loginByPhone(CheckSmsCodeDto checkSmsCodeDto) {
        String phone = checkSmsCodeDto.phone();
        String code = checkSmsCodeDto.code();
        String cachedCode = stringRedisTemplate.opsForValue().get(RedisConstants.SMS_CODE_PREFIX + phone);
        if (cachedCode == null){
            return R.error(ResponseCodeEnum.PreCheckFailed, "验证码已过期");
        } else {
            if (cachedCode.equals(code)){
                LoginAccount loginAccount =
                        (LoginAccount) this.loadUserByUsername(checkSmsCodeDto.phone());
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                loginAccount,
                                null,
                                loginAccount.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
                Map<String, Object> result = genTokenWithLoginAccount(phone, loginAccount);
                return R.ok(result);
            } else {
                return R.error(ResponseCodeEnum.Forbidden, "验证码错误");
            }
        }
    }

    @Override
    public String handleAlipayLogin(AlipayLoginCallbackDto alipayLoginCallbackDto) {
        AlipaySystemOauthTokenRequest tokenRequest = new AlipaySystemOauthTokenRequest();
        tokenRequest.setGrantType("authorization_code");
        tokenRequest.setCode(alipayLoginCallbackDto.auth_code());
        AlipaySystemOauthTokenResponse tokenResponse;
        log.info(alipayClient.toString());
        try {
            tokenResponse = alipayClient.execute(tokenRequest);
        } catch (AlipayApiException e) {
            throw new RuntimeException(e);
        }
        if(tokenResponse.isSuccess()){
            String accessToken = tokenResponse.getAccessToken();
            AlipayUserInfoShareRequest alipayIdRequest = new AlipayUserInfoShareRequest();
            AlipayUserInfoShareResponse alipayIdResponse;
            try {
                alipayIdResponse = alipayClient.execute(alipayIdRequest,accessToken);
            } catch (AlipayApiException e) {
                throw new RuntimeException(e);
            }
            if(alipayIdResponse.isSuccess()){
                // 支付宝的工作完成了 现在轮到我们的工作
                // 查数据库 看是否有alipayId和返回值匹配的用户
                // 照理说alipay的ID是要unique的 这个沙箱应用只给俩号 所以对不起 做不到
                User user = userMapper.selectOne(
                        new QueryWrapper<User>().lambda()
                                .eq(User::getAlipayId, alipayIdResponse.getUserId()));
                if (user == null){
                    log.error("支付宝ID: {} 未绑定用户", alipayIdResponse.getUserId());
                    // TODO 更换为前端异常回调地址
                    return "redirect:http://localhost:7070/#/oauth/alipay/failure";
                }
                // 查询LoginAccount
                LoginAccount loginAccount =
                        (LoginAccount) this.loadUserByUsername(user.getUsername());
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                loginAccount,
                                null,
                                loginAccount.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
                Map<String, Object> result = genTokenWithLoginAccount(
                        user.getUsername(), loginAccount);
                log.info("支付宝登录成功, 用户: {}, token: {}", user.getUsername(), result.get("token"));
                // 拼接URL
                return "redirect:http://localhost:7070/#/oauth/alipay/success?token=" +
                        result.get("token") +
                        "&id=" +
                        result.get("id");
            } else {
                log.error("调用获取支付宝ID接口失败, resp: {}", alipayIdResponse);
            }
        } else {
            log.error("调用获取支付宝AK接口失败, resp: {}", tokenResponse);
        }
        return "redirect:http://localhost:7070/#/oauth/alipay/failure";
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LoginAccount loginAccount = this.getOne(new QueryWrapper<LoginAccount>().lambda()
                .eq(LoginAccount::getUsername, username));
        if (loginAccount == null){
            throw new UsernameNotFoundException("用户名不存在");
        } else {
            return loginAccount;
        }
    }

    private Map<String, Object> genTokenWithLoginAccount(String username, LoginAccount loginAccount) {
        String token = jwtTokenProvider.generateToken(loginAccount);
        // token放入redis
        stringRedisTemplate.opsForValue().set(
                RedisConstants.TOKEN_KEY_PREFIX + username,
                token,
                RedisConstants.TOKEN_EXPIRE_TTL,
                TimeUnit.MILLISECONDS);
        // 组织返回结果
        Map<String, Object> result = new HashMap<>();
        if(loginAccount.getRole() == RoleEnum.User.getValue()){
            // 需要去查user表 拿到id
            User user = userMapper.selectOne(
                    new QueryWrapper<User>().lambda()
                            .eq(User::getUsername, username));
            result.put("id", user.getId());
            result.put("role", 0);
        } else if(loginAccount.getRole() == RoleEnum.Admin.getValue()){
            result.put("role", 1);
        }
        result.put("token", token);
        return result;
    }

}




