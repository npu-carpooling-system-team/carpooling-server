package edu.npu.service;

public interface NoticeMailService {

    /**
     * 根据行程查询订单并向乘车用户发送邮件通知
     * @param carpoolingId 行程id
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    void sendNoticeMailToUser(Long carpoolingId, String subject, String content);
}
