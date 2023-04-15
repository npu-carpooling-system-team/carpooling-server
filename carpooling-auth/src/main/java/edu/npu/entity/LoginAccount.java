package edu.npu.entity;

import com.baomidou.mybatisplus.annotation.*;
import edu.npu.common.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 用于用户名密码登录所需表格
 * @TableName login_account
 */
@TableName(value ="login_account")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginAccount implements Serializable, UserDetails {
    /**
     * 用户登录时唯一编号
     */
    @TableId(type = IdType.AUTO)
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
    private RoleEnum role;

    /**
     * 逻辑删除字段,0未删除,1已删除
     */
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 184688L;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
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
}
