package edu.npu.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.CannedAccessControlList;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author : [wangminan]
 * @description : [向阿里云OSS存储与拉取]
 */
@Component
@Slf4j
public class OssUtil {

    @Resource
    private OSS oss;

    @Value("${var.aliyun-oss.endpoint}")
    private String endPoint;

    @Value("${var.aliyun-oss.bucketName}")
    private String bucketName;

    @Value("${var.aliyun-oss.baseDir}")
    private String baseDir;

    @Value("${var.aliyun-oss.baseUrl}")
    private String baseUrl;

    // 阿里云的声明周期操作不支持根据访问时间删除文件 所以只能手动删除

    /**
     * 向Oss上传文件
     * @param file 文件
     * @return 是否上传成功
     */
    public String uploadFile(File file){
        try{
            oss.putObject(bucketName,baseDir+file.getName(),file);
            // 设定文件访问权限为公共读
            oss.setObjectAcl(bucketName,
                    baseDir+file.getName(),
                    CannedAccessControlList.PublicRead);
            return baseUrl + "/"  + baseDir + file.getName();
        } catch (OSSException e){
            log.error("文件:{}上传失败",file.getName());
        }
        return null;
    }

    /**
     * 从Oss删除文件 供XXL定时调用
     * @param fileName 文件名
     * @return 是否删除成功
     */
    public boolean deleteFile(String fileName){
        try{
            oss.deleteObject(bucketName,baseDir+fileName);
            return true;
        } catch (OSSException e){
            log.error("文件:{}删除失败",fileName);
        }
        return false;
    }
}
