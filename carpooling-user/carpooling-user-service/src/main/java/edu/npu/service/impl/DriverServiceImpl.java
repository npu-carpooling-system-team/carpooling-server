package edu.npu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.entity.Driver;
import edu.npu.entity.User;
import edu.npu.mapper.DriverMapper;
import edu.npu.mapper.UserMapper;
import edu.npu.service.DriverService;
import edu.npu.vo.DriverListItemVo;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
* @author wangminan
* @description 针对表【driver(司机表)】的数据库操作Service实现
* @createDate 2023-04-17 16:24:23
*/
@Service
public class DriverServiceImpl extends ServiceImpl<DriverMapper, Driver>
    implements DriverService{

    @Resource
    private UserMapper userMapper;

    @Override
    public R genDriverSimpleList() {
        List<DriverListItemVo> driverList = new ArrayList<>();
        List<Driver> drivers = this.list();
        drivers.forEach(driver -> {
            DriverListItemVo driverListItemVo = DriverListItemVo.builder()
                    .driverId(driver.getDriverId())
                    .username(userMapper.selectOne(
                            new LambdaQueryWrapper<User>()
                                    .eq(User::getId, driver.getDriverId())
                    ).getUsername())
                    .driversName(driver.getDriversName())
                    .build();
            driverList.add(driverListItemVo);
        });
        return R.ok().put("driverList", driverList);
    }
}




