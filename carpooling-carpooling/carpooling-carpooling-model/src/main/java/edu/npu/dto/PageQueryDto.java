package edu.npu.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author : [wangminan]
 * @description : [分页查询拼车行程Dto]
 * 我思考了一下因为会有空参 所以这玩意不能用record
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageQueryDto {
    @NotNull
    Integer pageNum;
    @NotNull
    Integer pageSize;
    String query;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    Date departureTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    Date arriveTime;
}
