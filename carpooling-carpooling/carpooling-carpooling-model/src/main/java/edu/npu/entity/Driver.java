package edu.npu.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 司机表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Driver implements Serializable {
    /**
     * 司机身份唯一标识符
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 与user表中id一致
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
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
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date driversExpireDate;

    /**
     * 司机评分
     */
    private Long avgScore;

    /**
     * 逻辑删除字段,0未删除,1已删除
     */
    private Integer isDeleted;

    @Serial
    private static final long serialVersionUID = 15213L;
}
