package edu.npu.service;

import edu.npu.vo.R;
import org.springframework.web.multipart.MultipartFile;

public interface OcrService {
    R ocrIdCard(MultipartFile file);

    R ocrDrivingLicense(MultipartFile file);

    R ocrVehicleLicense(MultipartFile file);
}
