package edu.npu.service.impl;

import com.aliyun.ocr_api20210707.Client;
import com.aliyun.ocr_api20210707.models.*;
import com.aliyun.teautil.models.RuntimeOptions;
import edu.npu.exception.CarpoolingException;
import edu.npu.service.OcrService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author : [wangminan]
 * @description : [调用阿里OCR接口实现类]
 */
@Service
public class OcrServiceImpl implements OcrService {

    @Resource
    private Client client;

    private static final String FAILED_TRANSFER = "文件转换失败";


    @Override
    public R ocrIdCard(MultipartFile multipartFile) {
        // 将MultipartFile转换为File
        try {
            File file = transferMultipartFileToFile(multipartFile);
            if (file == null) {
                return R.error(FAILED_TRANSFER);
            }
            RecognizeIdcardRequest recognizeIdcardRequest =
                    new RecognizeIdcardRequest()
                            .setBody(new FileInputStream(file))
                            .setOutputQualityInfo(true);
            RuntimeOptions runtime = new RuntimeOptions();
            RecognizeIdcardResponse recognizeIdcardResponse =
                    client.recognizeIdcardWithOptions(recognizeIdcardRequest, runtime);
            file.deleteOnExit();
            return R.ok().put("data", recognizeIdcardResponse.getBody().data);
        } catch (FileNotFoundException e) {
            throw new CarpoolingException("文件不存在");
        } catch (Exception e) {
            throw new CarpoolingException("OCR身份证失败");
        }
    }

    @Override
    public R ocrDrivingLicense(MultipartFile multipartFile) {
        try{
            File file = transferMultipartFileToFile(multipartFile);
            if (file == null) {
                return R.error(FAILED_TRANSFER);
            }
            RecognizeDrivingLicenseRequest recognizeDrivingLicenseRequest =
                    new RecognizeDrivingLicenseRequest()
                            .setBody(new FileInputStream(file));
            RuntimeOptions runtime = new RuntimeOptions();
            RecognizeDrivingLicenseResponse recognizeDrivingLicenseResponse =
                    client.recognizeDrivingLicenseWithOptions(
                            recognizeDrivingLicenseRequest, runtime);
            file.deleteOnExit();
            return R.ok().put("data", recognizeDrivingLicenseResponse.getBody().data);
        } catch (Exception e) {
            throw new CarpoolingException("OCR驾驶证失败");
        }
    }

    @Override
    public R ocrVehicleLicense(MultipartFile multipartFile) {
        try{
            File file = transferMultipartFileToFile(multipartFile);
            if (file == null) {
                return R.error(FAILED_TRANSFER);
            }
            RecognizeVehicleLicenseRequest recognizeVehicleLicenseRequest =
                    new RecognizeVehicleLicenseRequest()
                            .setBody(new FileInputStream(file));
            RuntimeOptions runtime = new RuntimeOptions();
            RecognizeVehicleLicenseResponse recognizeVehicleLicenseResponse =
                    client.recognizeVehicleLicenseWithOptions(
                            recognizeVehicleLicenseRequest, runtime);
            file.deleteOnExit();
            return R.ok().put("data", recognizeVehicleLicenseResponse.getBody().data);
        } catch (Exception e) {
            throw new CarpoolingException("OCR行驶证失败");
        }
    }

    /**
     * 将MultipartFile转换为File
     *
     * @param multipartFile MultipartFile
     * @return File
     */
    private File transferMultipartFileToFile(MultipartFile multipartFile) {
        File file ;
        String originalFilename = multipartFile.getOriginalFilename();
        String[] filename;
        if (originalFilename != null) {
            filename = originalFilename.split("\\.");
        } else {
            return null;
        }
        try {
            file = File.createTempFile(filename[0], filename[1]);
            multipartFile.transferTo(file);
        } catch (IOException e) {
            throw new CarpoolingException(e.getMessage());
        }
        return file;
    }
}
