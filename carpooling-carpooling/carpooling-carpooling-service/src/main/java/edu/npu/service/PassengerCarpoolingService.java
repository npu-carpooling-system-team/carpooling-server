package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.dto.PageQueryDto;
import edu.npu.entity.Carpooling;
import edu.npu.vo.R;

public interface PassengerCarpoolingService extends IService<Carpooling> {
    R getCarpoolingList(PageQueryDto pageQueryDto);
}
