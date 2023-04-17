package edu.npu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 司机表
 * @TableName driver
 */
@TableName(value ="driver")
@Data
public class Driver implements Serializable {
    /**
     * 司机身份唯一标识符
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 与user表中id一致
     */
    private Long driverId;

    /**
     * 司机身份证号
     */
    private String driversPersonalId;

    /**
     * 司机姓名
     */
    private String driversName;

    /**
     * 司机驾驶证号
     */
    private String driversLicenseNo;

    /**
     * 司机驾驶证类型 C1
     */
    private String driversLicenseType;

    /**
     * 司机车牌号
     */
    private String driversPlateNo;

    /**
     * 车型描述
     */
    private String driversVehicleType;

    /**
     * 证件过期的最早时间
     */
    private Date driversExpireDate;

    /**
     * 司机评分
     */
    private Integer avgScore;

    /**
     * 逻辑删除字段,0未删除,1已删除
     */
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
