package edu.npu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.entity.LoginAccount;
import edu.npu.mapper.LoginAccountMapper;
import edu.npu.service.LoginAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
 */
@Service
@Slf4j
public class LoginAccountServiceImpl extends ServiceImpl<LoginAccountMapper, LoginAccount>
        implements LoginAccountService, UserDetailsService {

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
