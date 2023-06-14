package edu.npu.service;

import edu.npu.entity.Carpooling;
import edu.npu.service.impl.EsService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
 */
@SpringBootTest
public class CarpoolingDocumentTest {
    @Resource
    private EsService esService;

    @Resource
    private PassengerCarpoolingService passengerCarpoolingService;

    @Test
    void testUpdateEs() {
        List<Carpooling> carpoolingList = passengerCarpoolingService.list();

        for (Carpooling carpooling : carpoolingList) {
            esService.updateCarpoolingToEs(carpooling);
        }
    }
}
