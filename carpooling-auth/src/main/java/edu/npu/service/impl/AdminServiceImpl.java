package edu.npu.service.impl;

import edu.npu.common.ResponseCodeEnum;
import edu.npu.service.AdminService;
import edu.npu.vo.CountUserVo;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static edu.npu.common.RedisConstants.LOGIN_COUNT_KEY_PREFIX;

/**
 * @author : [wangminan]
 * @description : [管理员的业务处理]
 */
@Service
public class AdminServiceImpl implements AdminService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public R getLoginCount() {
        // 获取从当日起倒数七天内的登录次数
        String[] keys = new String[7];
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        for (int i = 0; i < 7; i++) {
            keys[i] = LOGIN_COUNT_KEY_PREFIX + sdf.format(
                    System.currentTimeMillis() - i * 24 * 60 * 60 * 1000);
        }
        // 到Redis中查询对应键值
        String[] values = Objects.requireNonNull(
                stringRedisTemplate.opsForValue().multiGet(List.of(keys)))
                .toArray(new String[0]);
        if (values.length == 0) {
            return R.error(ResponseCodeEnum.CREATION_ERROR,"获取登录次数失败,暂无登录数据");
        }
        List<CountUserVo> countUserVoList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            if (values[i] == null){
                break;
            }
            countUserVoList.add(new CountUserVo(
                    sdf.format(System.currentTimeMillis() -
                            (long) i * 24 * 60 * 60 * 1000),
                    Integer.parseInt(values[i])));
        }
        return R.ok().put("result", countUserVoList);
    }
}
