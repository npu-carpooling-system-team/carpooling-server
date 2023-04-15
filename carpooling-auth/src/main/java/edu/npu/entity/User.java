package edu.npu.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * 用户表,用于记录用户的详细信息
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * 用户唯一编号
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户手机号
     */
    private String username;

    /**
     * 用户头像存储URL
     */
    private String userImage;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 支付宝返回ID,用于第三方登录与支付,最后是要unique的
     */
    private String alipayId;

    /**
     * 是否是司机
     */
    private Integer isDriver;

    /**
     * 是否是乘客
     */
    private Integer isPassenger;

    /**
     * 逻辑删除字段,是否被删除,0未删除,1已删除
     */
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 16564151L;
}
