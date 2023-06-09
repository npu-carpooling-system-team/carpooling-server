package edu.npu.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayUserInfoShareRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayUserInfoShareResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import edu.npu.exception.CarpoolingException;
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
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static edu.npu.common.RedisConstants.*;
import static edu.npu.util.RegexPatterns.*;

/**
* @author wangminan
* @description 针对表【login_account(用于用户名密码登录所需表格)】的数据库操作Service实现
* @createDate 2023-04-15 20:48:34
*/
@Service
@Slf4j
public class LoginAccountServiceImpl extends ServiceImpl<LoginAccountMapper, LoginAccount>
    implements LoginAccountService, UserDetailsService {

    public static final String REGISTER_FAILED_MSG = "注册失败,请检查用户名是否重复";
    public static final String TOKEN = "token";
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

    @Resource
    private ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R registerUser(UserRegisterDto userRegisterDto) {
        if(!userRegisterDto.isDriver() && !userRegisterDto.isPassenger()){
            return R.error(ResponseCodeEnum.PRE_CHECK_FAILED, "请至少选择一个角色");
        }
        if (userRegisterDto.isDriver()){
            // 接下来的字段都不允许为空，否则直接返回失败结果
            if (isRegisterDtoValid(userRegisterDto)){
                return R.error(ResponseCodeEnum.NOT_ENOUGH_INFORMATION, "司机注册信息不完整");
            }
            // 车牌与身份证号正则
            if (!userRegisterDto.driversPlateNo().matches(PLATE_NO_REGEX)){
                return R.error(ResponseCodeEnum.PRE_CHECK_FAILED, "车牌号格式不正确");
            } else if (!userRegisterDto.driversPersonalId().matches(ID_CARD_REGEX)){
                return R.error(ResponseCodeEnum.PRE_CHECK_FAILED, "身份证号格式不正确");
            } else if (!userRegisterDto.driversLicenseNo().matches(ID_CARD_REGEX)){
                return R.error(ResponseCodeEnum.PRE_CHECK_FAILED, "驾驶证号格式不正确");
            }
        }
        if (StringUtils.hasText(userRegisterDto.email()) &&
                !userRegisterDto.email().matches(EMAIL_REGEX)){
            // 正则表达式匹配
            return R.error(ResponseCodeEnum.PRE_CHECK_FAILED, "邮箱格式不正确");
        }
        // 开始处理注册信息
        // 添加到loginAccount表
        LoginAccount loginAccount = new LoginAccount();
        loginAccount.setUsername(userRegisterDto.username());
        // 使用RSA解密密码
        loginAccount.setPassword(
                passwordEncoder.encode(
                        RsaUtil.decrypt(privateKey, userRegisterDto.password())));
        loginAccount.setRole(RoleEnum.USER.getValue());
        boolean saveLoginAccount = this.save(loginAccount);
        if (!saveLoginAccount){
            return R.error(ResponseCodeEnum.SERVER_ERROR, REGISTER_FAILED_MSG);
        }

        // 只需要添加到user表
        if (userRegisterDto.isPassenger() && !userRegisterDto.isDriver()){
            // 只需要改动user表
            User user = new User();
            BeanUtils.copyProperties(userRegisterDto, user);
            int insert = userMapper.insert(user);
            return insert == 1 ? R.ok("注册成功") :
                    R.error(ResponseCodeEnum.SERVER_ERROR, REGISTER_FAILED_MSG);
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
                    R.error(ResponseCodeEnum.SERVER_ERROR, REGISTER_FAILED_MSG);
        }
    }

    private static boolean isRegisterDtoValid(UserRegisterDto userRegisterDto) {
        return userRegisterDto.driversPersonalId().isBlank()
                || userRegisterDto.driversLicenseNo().isBlank()
                || userRegisterDto.driversLicenseType().isBlank()
                || userRegisterDto.driversVehicleType().isBlank()
                || userRegisterDto.driversExpireDate() == null
                || userRegisterDto.driversPlateNo().isBlank();
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
            addUserLoginCount();
            return R.ok(result);
        } catch (Exception e) {
            log.error("登录失败", e);
            return R.error(ResponseCodeEnum.SERVER_ERROR, "登录失败");
        }
    }

    private void addUserLoginCount() {
        String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        boolean isLock = tryLock();
        if (isLock){
            String currentLoginCount = stringRedisTemplate.opsForValue().get(
                    LOGIN_COUNT_KEY_PREFIX + currentDate);
            if (StringUtils.hasText(currentLoginCount)){
                stringRedisTemplate.opsForValue().set(
                        LOGIN_COUNT_KEY_PREFIX + currentDate,
                        String.valueOf(Integer.parseInt(currentLoginCount) + 1),
                        LOGIN_COUNT_EXPIRE_TIME,
                        TimeUnit.SECONDS);
            } else {
                stringRedisTemplate.opsForValue().set(
                        LOGIN_COUNT_KEY_PREFIX + currentDate,
                        "1",
                        LOGIN_COUNT_EXPIRE_TIME,
                        TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public R logout(LoginAccount loginAccount) {
        // 将token从redis中删除 然后返回即可
        stringRedisTemplate.delete(RedisConstants.LOGIN_ACCOUNT_KEY_PREFIX + loginAccount.getUsername());
        return R.ok();
    }

    @Override
    public R loginByPhone(CheckSmsCodeDto checkSmsCodeDto) {
        String phone = checkSmsCodeDto.phone();
        String code = checkSmsCodeDto.code();
        String cachedCode = stringRedisTemplate.opsForValue().get(RedisConstants.SMS_CODE_PREFIX + phone);
        if (cachedCode == null){
            return R.error(ResponseCodeEnum.PRE_CHECK_FAILED, "验证码已过期");
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
                addUserLoginCount();
                return R.ok(result);
            } else {
                return R.error(ResponseCodeEnum.FORBIDDEN, "验证码错误");
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
            throw new CarpoolingException(e.getMessage());
        }
        if(tokenResponse.isSuccess()){
            String accessToken = tokenResponse.getAccessToken();
            AlipayUserInfoShareRequest alipayIdRequest = new AlipayUserInfoShareRequest();
            AlipayUserInfoShareResponse alipayIdResponse;
            try {
                alipayIdResponse = alipayClient.execute(alipayIdRequest,accessToken);
            } catch (AlipayApiException e) {
                throw new CarpoolingException(e.getMessage());
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
                    return "redirect:https://carpooling-client.wangminan.me/#/oauth/alipay/failure";
                }
                // 查b -10询LoginAccount
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
                log.info("支付宝登录成功, 用户: {}, token: {}", user.getUsername(), result.get(TOKEN));
                addUserLoginCount();
                // 拼接URL
                return "redirect:https://carpooling-client.wangminan.me/#/oauth/alipay/success" +
                        "?token=" +
                        result.get(TOKEN) +
                        "&id=" +
                        result.get("id");
            } else {
                log.error("调用获取支付宝ID接口失败, resp: {}", alipayIdResponse);
            }
        } else {
            log.error("调用获取支付宝AK接口失败, resp: {}", tokenResponse);
        }
        return "redirect:http://carpooling-client.wangminan.me/#/oauth/alipay/failure";
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
        // token放入redis 用一个hash结构存储token和loginAccount的json字符串形式
        try {
            stringRedisTemplate.opsForHash().put(
                    RedisConstants.LOGIN_ACCOUNT_KEY_PREFIX + username,
                    HASH_TOKEN_KEY, token);
            stringRedisTemplate.opsForHash().put(
                    RedisConstants.LOGIN_ACCOUNT_KEY_PREFIX + username,
                    HASH_LOGIN_ACCOUNT_KEY, objectMapper.writeValueAsString(loginAccount));
            // 设置过期时间LOGIN_ACCOUNT_EXPIRE_TTL
            stringRedisTemplate.expire(
                    RedisConstants.LOGIN_ACCOUNT_KEY_PREFIX + username,
                    LOGIN_ACCOUNT_EXPIRE_TTL,
                    TimeUnit.MILLISECONDS);
        } catch (JsonProcessingException e) {
            throw new CarpoolingException("loginAccount序列化失败");
        }
        // 组织返回结果
        Map<String, Object> result = new HashMap<>();
        if(loginAccount.getRole() == RoleEnum.USER.getValue()){
            // 需要去查user表 拿到id
            User user = userMapper.selectOne(
                    new QueryWrapper<User>().lambda()
                            .eq(User::getUsername, username));
            result.put("id", user.getId());
            result.put("role", 0);
        } else if(loginAccount.getRole() == RoleEnum.ADMIN.getValue()){
            result.put("role", 1);
        }
        result.put(TOKEN, token);
        return result;
    }

    private boolean tryLock() {
        Boolean flag =
                stringRedisTemplate.opsForValue()
                        .setIfAbsent(LOGIN_LOCK_KEY, "1", 1, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }
}




