package edu.npu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

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
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 订单表编号
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long orderId;

    /**
     * 拼车行程编号
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long carpoolingId;

    /**
     * 与user表ID一致
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long passengerId;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 4528L;
}
