package edu.npu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 拼车订单表
 * @TableName order
 */
@TableName(value ="order")
@Data
@Builder
public class Order implements Serializable {
    /**
     * 订单唯一编号
     */
    @TableId(type = IdType.AUTO)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 拼车行程id,与carpooling表的id字段对应
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long carpoolingId;

    /**
     * 乘客用户编号,与user表中id一致
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long passengerId;

    /**
     * 订单当前状态,0未开始
     */
    private Integer status;

    /**
     *
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 订单状态更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 乘客给司机打分,5分制
     */
    private Integer score;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
