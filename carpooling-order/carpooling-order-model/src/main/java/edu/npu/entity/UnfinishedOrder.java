package edu.npu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName unfinished_order
 */
@TableName(value ="unfinished_order")
@Data
public class UnfinishedOrder implements Serializable {
    /**
     * 唯一主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单表编号
     */
    private Long orderId;

    /**
     * 拼车行程编号
     */
    private Long carpoolingId;

    /**
     * 与user表ID一致
     */
    private Long passengerId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}