package edu.npu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import lombok.Builder;
import lombok.Data;

/**
 * 用户与司机交流表
 * @TableName chat
 */
@TableName(value ="chat")
@Data
@Builder
public class Chat implements Serializable {
    /**
     * 唯一标号,主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发送用户的ID,与user表id列一致
     */
    private Long fromUserId;

    /**
     * 接收用户的id,与user表中的一致
     */
    private Long toUserId;

    /**
     * 留言内容
     */
    private String message;

    /**
     *
     */
    private Date sendTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
