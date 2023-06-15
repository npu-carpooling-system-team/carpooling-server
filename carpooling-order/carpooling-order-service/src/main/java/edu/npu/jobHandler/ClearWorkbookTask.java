package edu.npu.jobHandler;

import com.xxl.job.core.handler.annotation.XxlJob;
import edu.npu.util.OssUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static edu.npu.common.RedisConstants.UPLOAD_FILE_KEY_PREFIX;

/**
 * @author : [wangminan]
 * @description : [定时清理Excel工作簿]
 */
@Slf4j
@Component
public class ClearWorkbookTask {

    @Resource
    private OssUtil ossUtil;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @XxlJob("ClearWorkbookJobHandler")
    public void clearWorkbookJobHandler() {
        log.info("XXL>>>>>清理Excel工作簿的请求");
        List<String> fileNames = new ArrayList<>();
        String yesterday = new SimpleDateFormat("yyyy-MM-dd")
                .format(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        String theDayBeforeYesterday = new SimpleDateFormat("yyyy-MM-dd")
                .format(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000);
        // 获取UPLOAD_FILE_KEY_PREFIX + yesterday的和theDayBeforeYesterday的list中的所有文件名
        fileNames.addAll(
            Objects.requireNonNull(
                stringRedisTemplate.opsForList()
                    .range(UPLOAD_FILE_KEY_PREFIX + yesterday, 0, -1)));
        fileNames.addAll(
            Objects.requireNonNull(
                stringRedisTemplate.opsForList()
                    .range(UPLOAD_FILE_KEY_PREFIX + theDayBeforeYesterday, 0, -1)));
        log.info("需要删除的文件名:" + fileNames);
        // 删除OSS中的文件
        fileNames.forEach(fileName -> {
            boolean deleted = ossUtil.deleteFile(fileName);
            if (!deleted) {
                log.error("OSS删除文件:{}失败", fileName);
            }
        });
    }
}
