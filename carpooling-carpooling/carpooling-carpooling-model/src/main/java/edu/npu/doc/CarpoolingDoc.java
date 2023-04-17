package edu.npu.doc;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import edu.npu.entity.Carpooling;
import jakarta.validation.constraints.NotNull;
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
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarpoolingDoc {

    private Long id;

    private Long driverId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date departureTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date arriveTime;

    private String departurePoint;

    private String arrivePoint;

    private String passingPoint;

    private String description;

    private Integer totalPassengerNo;

    private Integer leftPassengerNo;

    private Integer price;

    public CarpoolingDoc(Carpooling carpooling){
        this.id = carpooling.getId();
        this.driverId = carpooling.getDriverId();
        this.departureTime = carpooling.getDepartureTime();
        this.arriveTime = carpooling.getArriveTime();
        this.departurePoint = carpooling.getDeparturePoint();
        this.arrivePoint = carpooling.getArrivePoint();
        this.passingPoint = carpooling.getPassingPoint();
        this.description = carpooling.getDescription();
        this.totalPassengerNo = carpooling.getTotalPassengerNo();
        this.leftPassengerNo = carpooling.getLeftPassengerNo();
        this.price = carpooling.getPrice();
    }
}
