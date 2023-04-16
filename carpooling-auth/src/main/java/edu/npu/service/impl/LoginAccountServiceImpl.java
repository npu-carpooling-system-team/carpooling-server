package edu.npu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.common.RedisConstants;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.common.RoleEnum;
import edu.npu.dto.UserLoginDto;
import edu.npu.dto.UserRegisterDto;
import edu.npu.entity.Driver;
import edu.npu.entity.LoginAccount;
import edu.npu.entity.User;
import edu.npu.mapper.DriverMapper;
import edu.npu.mapper.LoginAccountMapper;
import edu.npu.mapper.UserMapper;
import edu.npu.service.LoginAccountService;
import edu.npu.config.JwtTokenProvider;
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
            String token = jwtTokenProvider.generateToken(loginAccount);
            // token放入redis
            stringRedisTemplate.opsForValue().set(
                    RedisConstants.TOKEN_KEY_PREFIX + userLoginDto.username(),
                    token,
                    RedisConstants.TOKEN_EXPIRE_TTL,
                    TimeUnit.MILLISECONDS);
            // 组织返回结果
            Map<String, Object> result = new HashMap<>();
            if(loginAccount.getRole() == RoleEnum.User.getValue()){
                // 需要去查user表 拿到id
                User user = userMapper.selectOne(
                        new QueryWrapper<User>().lambda()
                        .eq(User::getUsername, userLoginDto.username()));
                result.put("id", user.getId());
                result.put("role", 0);
            } else if(loginAccount.getRole() == RoleEnum.Admin.getValue()){
                result.put("role", 1);
            }
            result.put("token", token);
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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LoginAccount loginAccount = this.getOne(new QueryWrapper<LoginAccount>().lambda()
                .eq(LoginAccount::getUsername, username));
        if (loginAccount == null){
            throw new UsernameNotFoundException("用户名不存在");
        } else {
            return loginAccount;
        }
    }
}




