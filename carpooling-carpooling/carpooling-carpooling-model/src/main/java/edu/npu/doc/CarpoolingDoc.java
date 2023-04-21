package edu.npu.doc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.npu.entity.Carpooling;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 拼车行程表
 *
 * @TableName carpooling
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarpoolingDoc {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long driverId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date departureTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date arriveTime;

    private String departurePoint;

    private String arrivePoint;

    private String passingPoint;

    private String description;

    private Integer totalPassengerNo;

    private Integer leftPassengerNo;

    private Integer price;

    public CarpoolingDoc(Carpooling carpooling) {
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

    public CarpoolingDoc(String json) throws JsonProcessingException {
        CarpoolingDoc carpoolingDoc = new ObjectMapper().readValue(json, CarpoolingDoc.class);
        this.id = carpoolingDoc.getId();
        this.driverId = carpoolingDoc.getDriverId();
        this.departureTime = carpoolingDoc.getDepartureTime();
        this.arriveTime = carpoolingDoc.getArriveTime();
        this.departurePoint = carpoolingDoc.getDeparturePoint();
        this.arrivePoint = carpoolingDoc.getArrivePoint();
        this.passingPoint = carpoolingDoc.getPassingPoint();
        this.description = carpoolingDoc.getDescription();
        this.totalPassengerNo = carpoolingDoc.getTotalPassengerNo();
        this.leftPassengerNo = carpoolingDoc.getLeftPassengerNo();
        this.price = carpoolingDoc.getPrice();
    }
}
