package com.enba.ali.pay.controller;

import com.alipay.v3.ApiException;
import com.alipay.v3.util.AlipaySignature;
import com.google.common.collect.Maps;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/ali-pay")
@RestController
public class AliCallbackController {

  Logger log = LoggerFactory.getLogger(AliCallbackController.class);

  @GetMapping("/test")
  public String web() {
    return "hello web...";
  }

  /**
   * 支付宝回调 参考官方文档（https://opendocs.alipay.com/solution/0df1nk） 注意：post方式 返回值为success
   *
   * @param request r
   * @return r
   * @throws ApiException a
   */
  @PostMapping("/callback")
  public String callback(HttpServletRequest request) throws ApiException {
    if (request.getParameter("trade_status").equals("TRADE_SUCCESS")) {
      Map<String, String> parameters = Maps.newHashMap();
      Map<String, String[]> requestParams = request.getParameterMap();
      for (String name : requestParams.keySet()) {
        parameters.put(name, request.getParameter(name));
      }

      log.info("parameters###{}", parameters);

      boolean pass =
          AlipaySignature.certVerifyV1(parameters, "alipayPublicCert.crt", "utf-8", "RSA2");

      // 验签
      if (pass) {
        log.info("验签成功");
        log.info("通知时间。通知的发送时间。格式为 yyyy-MM-dd HH:mm:ss。###{}", parameters.get("notify_time"));
        log.info("通知类型###{}", parameters.get("notify_type"));
        log.info("通知校验 ID###{}", parameters.get("notify_id"));
        log.info("签名类型###{}", parameters.get("sign_type"));
        log.info("签名###{}", parameters.get("sign"));
        log.info("支付宝交易号。支付宝交易凭证号。###{}", parameters.get("trade_no"));
        log.info("开发者的 app_id###{}", parameters.get("app_id"));
        log.info("商户订单号###{}", parameters.get("out_trade_no"));
        log.info("商家业务号。商家业务 ID，主要是退款通知中返回退款申请的流水号。###{}", parameters.get("out_biz_no"));
        log.info("买家支付宝用户号###{}", parameters.get("buyer_id"));
        log.info("买家支付宝账号###{}", parameters.get("buyer_logon_id"));
        log.info("卖家支付宝用户号###{}", parameters.get("seller_id"));
        log.info("卖家支付宝账号###{}", parameters.get("seller_email"));
        log.info("交易状态###{}", parameters.get("trade_status"));
        log.info("订单金额。本次交易支付的订单金额，单位为人民币（元）。支持小数点后两位。###{}", parameters.get("total_amount"));
        log.info("实收金额。商家在交易中实际收到的款项，单位为人民币（元）。支持小数点后两位。###{}", parameters.get("receipt_amount"));
      } else {
        log.error("验签失败");
      }
    } else {
      log.error("支付失败");
      return "fail";
    }

    return "success";
  }
}
