package com.jwb.orders.model.dto;

import com.jwb.orders.model.po.JwbPayRecord;
import lombok.Data;
import lombok.ToString;

/**
 * @description 支付记录dto
 */
@Data
@ToString
public class PayRecordDto extends JwbPayRecord {

    //二维码
    private String qrcode;

}
