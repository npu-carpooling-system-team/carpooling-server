package edu.npu.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.npu.common.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 用于用户名密码登录所需表格
 * @TableName login_account
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(
        value = {"accountNonExpired", "accountNonLocked",
                "credentialsNonExpired", "enabled", "authorities"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginAccount implements Serializable, UserDetails {
    /**
     * 用户登录时唯一编号
     */
    private Long id;

    /**
     * 用户登录名/用户手机号
     */
    private String username;

    /**
     * 用户登录密码
     */
    private String password;

    /**
     * 用户角色:司机/乘客,管理员
     */
    private int role;

    /**
     * 逻辑删除字段,0未删除,1已删除
     */
    private Integer isDeleted;

    @Serial
    private static final long serialVersionUID = 184688L;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(
                Objects.requireNonNull(RoleEnum.fromValue(role)).name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isDeleted == 0;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // 需要手动准备如下构造函数供Jackson调用 否则报Cannot construct instance of
    public LoginAccount(String json) throws IOException{
        LoginAccount loginAccount = new ObjectMapper().readValue(json, LoginAccount.class);
        this.id = loginAccount.getId();
        this.username = loginAccount.getUsername();
        this.password = loginAccount.getPassword();
        this.role = loginAccount.getRole();
        this.isDeleted = loginAccount.getIsDeleted();
    }
}
