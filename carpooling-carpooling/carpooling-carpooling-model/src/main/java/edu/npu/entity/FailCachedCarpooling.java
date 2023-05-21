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

/**
 * 同步失败的缓存记录在这张表中
 * @TableName fail_cached_carpooling
 */
@TableName(value ="fail_cached_carpooling")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FailCachedCarpooling implements Serializable {
    /**
     * 唯一ID 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 与carpooling表ID一致
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long carpoolingId;

    /**
     * 操作类型
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Integer operationType;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 256L;

    public FailCachedCarpooling(Long carpoolingId, Integer operationType) {
        this.carpoolingId = carpoolingId;
        this.operationType = operationType;
    }
}
