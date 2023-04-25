package edu.npu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 拼车行程表
 * @TableName carpooling
 */
@TableName(value ="carpooling")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Carpooling implements Serializable {
    /**
     * 拼车单唯一编号
     */
    @TableId(type = IdType.AUTO)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 司机在user表中的编号
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long driverId;

    /**
     * 发车时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date departureTime;

    /**
     * 预计到达时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date arriveTime;

    /**
     * 出发地点
     */
    private String departurePoint;

    /**
     * 到达地点
     */
    private String arrivePoint;

    /**
     * 途经点,json数组格式
     */
    private String passingPoint;

    /**
     * 司机对本次行程描述
     */
    private String description;

    /**
     * 该次行程总计可搭载乘客数
     */
    private Integer totalPassengerNo;

    /**
     * 剩余能搭乘的乘客数量
     */
    private Integer leftPassengerNo;

    /**
     * 拼车价格,单位为元,不考虑小数
     */
    private Integer price;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
