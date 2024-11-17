package com.enba.ali.pay.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import com.alibaba.fastjson2.JSON;
import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.factory.Factory.Payment;
import com.alipay.easysdk.kernel.Config;
import com.alipay.easysdk.kernel.util.ResponseChecker;
import com.alipay.easysdk.payment.common.models.AlipayDataDataserviceBillDownloadurlQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeCancelResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeCloseResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeRefundResponse;
import com.alipay.easysdk.payment.facetoface.models.AlipayTradePrecreateResponse;
import com.alipay.easysdk.payment.page.models.AlipayTradePagePayResponse;
import com.alipay.easysdk.payment.wap.models.AlipayTradeWapPayResponse;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 支付宝支付-easy sdk使用 */
@RestController
@RequestMapping("/ali-pay")
public class EasySdkController {

  Logger log = LoggerFactory.getLogger(EasySdkController.class);

  /**
   * 电脑网站-生成交易表单，渲染后自动跳转支付宝网站引导用户完成支付
   *
   * @param httpResponse r
   */
  @GetMapping("/page")
  public void page(HttpServletResponse httpResponse) {
    try {
      AlipayTradePagePayResponse response =
          Payment.Page()
              .asyncNotify("http://2cux7a.natappfree.cc/ali-pay/callback")
              .pay("Apple2", UUID.randomUUID().toString(), "1", "https://www.baidu.com");

      log.info("response###{}", JSON.toJSONString(response));

      if (ResponseChecker.success(response)) {
        // TODO 生成交易表单成功，结合自己业务处理
      }

      httpResponse.setContentType("text/html;charset=UTF-8");
      httpResponse.getWriter().write(response.getBody());
      httpResponse.getWriter().flush();
      httpResponse.getWriter().close();
    } catch (Exception e) {
      log.error("page err###{}", e.getMessage(), e);
    }
  }

  /**
   * 当面付-生成交易付款码，待用户扫码付款
   *
   * @return s
   */
  @GetMapping("/face-to-face")
  public void faceToFace(HttpServletResponse httpResponse) {
    // 沙箱环境
    Factory.setOptions(getSandBoxOptions());
    try {
      AlipayTradePrecreateResponse response =
          Payment.FaceToFace()
              .asyncNotify("http://2cux7a.natappfree.cc/ali-pay/callback")
              .preCreate("Apple2", UUID.randomUUID().toString(), "1");

      log.info("response###{}", JSON.toJSONString(response));

      // 3. 处理响应或异常
      if (ResponseChecker.success(response)) {
        QrCodeUtil.generate(response.getQrCode(), 256, 256, "", httpResponse.getOutputStream());
      }

    } catch (Exception e) {
      log.error("faceToFace err###{}", e.getMessage(), e);
    }
  }

  /**
   * 手机网站-生成交易表单，渲染后自动跳转支付宝网站引导用户完成支付
   *
   * @param httpResponse r
   */
  @GetMapping("/wap-pay")
  public void wapPay(HttpServletResponse httpResponse) {
    // 沙箱环境
    Factory.setOptions(getSandBoxOptions());
    try {
      AlipayTradeWapPayResponse response =
          Payment.Wap()
              .asyncNotify("http://2cux7a.natappfree.cc/ali-pay/callback")
              .pay(
                  "Apple2",
                  UUID.randomUUID().toString(),
                  "1",
                  "https://www.taobao.com",
                  "https://www.baidu.com");

      log.info("response###{}", JSON.toJSONString(response));

      // 3. 处理响应或异常
      if (ResponseChecker.success(response)) {
        httpResponse.setContentType("text/html;charset=UTF-8");
        httpResponse.getWriter().write(response.getBody());
        httpResponse.getWriter().flush();
        httpResponse.getWriter().close();
      }
    } catch (Exception e) {
      log.error("wapPay err###{}", e.getMessage(), e);
    }
  }

  /**
   * 查询交易
   *
   * @throws Exception e
   */
  @GetMapping("/query")
  public String query() throws Exception {
    // 沙箱环境
    Factory.setOptions(getSandBoxOptions());

    AlipayTradeQueryResponse response =
        Factory.Payment.Common().query("f323e4d9-a881-463e-92a9-e0da39c45015");

    log.info("response###{}", JSON.toJSONString(response));

    return JSON.toJSONString(response);
  }

  /**
   * 交易退款(当交易发生之后一段时间内，由于买家或者卖家的原因需要退款时，卖家可以通过退款接口将支付款退还给买家，支付宝将在收到退款请求并且验证成功之后，按照退款规则将支付款按原路退到买家帐号上。
   * 交易超过约定时间（签约时设置的可退款时间）的订单无法进行退款。
   * 支付宝退款支持单笔交易分多次退款，多次退款需要提交原支付订单的订单号和设置不同的退款请求号。一笔退款失败后重新提交，要保证重试时退款请求号不能变更，防止该笔交易重复退款。
   * 同一笔交易累计提交的退款金额不能超过原始交易总金额。)
   *
   * @return r
   * @throws Exception e
   */
  @GetMapping("/refund")
  public String refund() throws Exception {
    // 沙箱环境
    Factory.setOptions(getSandBoxOptions());

    // 1.部分退款时：检查是否设置out_request_no参数，该参数是标识一次退款请求，同一笔交易多次退款需要保证唯一，且 部分退款，则此参数必传。
    // 2.全部退款：检查该笔交易的支付金额与退款金额是否一致；
    AlipayTradeRefundResponse response =
        Factory.Payment.Common()
            .optional("out_request_no", IdUtil.fastSimpleUUID())
            .refund("f323e4d9-a881-463e-92a9-e0da39c45015", "0.99");

    log.info("response###{}", JSON.toJSONString(response));

    return JSON.toJSONString(response);
  }

  /**
   * 交易退款查询
   *
   * @return r
   * @throws Exception e
   */
  @GetMapping("/refund-query")
  public String refundQuery() throws Exception {
    // 沙箱环境
    Factory.setOptions(getSandBoxOptions());

    AlipayTradeFastpayRefundQueryResponse response =
        Factory.Payment.Common()
            .queryRefund(
                "64628156-f784-4572-9540-485b7c91b850", "64628156-f784-4572-9540-485b7c91b850");

    log.info("response###{}", JSON.toJSONString(response));

    return JSON.toJSONString(response);
  }

  /**
   * 关闭交易-用于交易创建后，用户在一定时间内未进行支付，可调用该接口直接将未付款的交易进行关闭。
   *
   * @return r
   * @throws Exception e
   */
  @GetMapping("/close")
  public String close() throws Exception {
    // 沙箱环境
    Factory.setOptions(getSandBoxOptions());

    AlipayTradeCloseResponse response =
        Factory.Payment.Common().close("e7aa4769-5282-4492-b6b1-3badc086ddf1");

    log.info("response###{}", JSON.toJSONString(response));

    return JSON.toJSONString(response);
  }

  // 撤销交易
  @GetMapping("/cancel")
  public String cancel() throws Exception {
    // 沙箱环境
    Factory.setOptions(getSandBoxOptions());

    AlipayTradeCancelResponse response =
        Factory.Payment.Common().cancel("0cf447dc-69b7-46e1-b5b5-98cf44681a07");

    log.info("response###{}", JSON.toJSONString(response));

    return JSON.toJSONString(response);
  }

  /**
   * 查询对账单下载地址（沙箱只能调用查询对账单下载地址接口，但是无法查看对账单，只有正式环境才可以）
   *
   * @return r
   * @throws Exception e
   */
  @GetMapping("/bill-download-url-query")
  public String billDownloadUrlQuery() throws Exception {
    // 沙箱环境
    Factory.setOptions(getSandBoxOptions());

    AlipayDataDataserviceBillDownloadurlQueryResponse response =
        Factory.Payment.Common().downloadBill("trade", "2024-10");

    log.info("response###{}", JSON.toJSONString(response));

    return JSON.toJSONString(response);
  }

  /**
   * 沙箱环境 证书模式
   *
   * @return r
   */
  private Config getSandBoxOptions() {
    Config config = new Config();
    config.protocol = "https";
    config.gatewayHost = "openapi-sandbox.dl.alipaydev.com";
    config.signType = "RSA2";
    config.appId = "9021000141623931";
    // 为避免私钥随源码泄露，推荐从文件中读取私钥字符串而不是写入源码中
    config.merchantPrivateKey =
        "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCJNJsLy/L+SrrEXnI5Gx3Kw5LcWR4ZzyGfz2r94euqYCf8yIzXGU26Ey/Fzpf6fx70aW6aQMg+BgLaehyX8wG+m44s7qidNN/Uhh9ertsWHA89ZOBpVqV4F7G5tQVAesaQowIMANNfsvRbf+w02+FdvB2Fp3on+PQH1EPcqKo+SkaPsNpiNTKaOVfCd7m9YONEjwg70/Ni/2QZBDfV34hM5v7T1YxNz+VRfDsdLzQ3Q8Pr+v5XhQcl5FWrnwlf99eSIXeccio3hYyNUyTd1XibfVl22WH/+zfx2g7JHVhHRoLS7d0ZzoznVlJSiIkDa3mmruVPz0iswxsR42En7eJ5AgMBAAECggEBAIe7S9q6szNPOD5E9Jh7s9g/64wieT2tBCV1avGnypxsrYJL39B2zsbSbkMwIncDxNqjZnAgwDdjePUrMhg7pTEr79WRb6q9gORcnrHrJv9TWNwybDKpLd6FCiOd6YklLEQvjjnTo0eb4FKYVVLhZqx1CXuL97ONe9Q2779d97TQCfhTUpciaJ7+CVvXybo2Z3fi6AmYoh+frUSTFzQkfCgVED0mLpOy7Tvj+DEPIs4OPZ8sHtrZ2t1ElRtQjPueZSf3uESy4qGqTygchFvODk5U4R+1wN2VCVustP6+m70BuRD39K/8o736V9GmBO0j8Nk+vw5Chh74/5M6NEveyJECgYEA44rUO0Z5DgVONvXj5ywTrDW1XNjlXTwyIhraX2GGckPep/aY3sA5TlHjgFWQ9NJhSGxESHSV8R5K6z7foPkNaRCp+Gsh2LnDGtWACCu1FFW/AaFFVWU8dxUO1T9VRJqWwIrgz03ub+ztF4dcZUG+5I363weo8u8d9zksckxyXxUCgYEAml17sQNycnNaS7mTfxoYvENaeSuHxxv8wovuzIddnzEcLQQa/lOEnrf63CxKYDXPKcTc252R5Wfzj5juFES1HVEm8cB8VAQkiEFK8EfWGCJFwDilOC4SbLDwu989Ja2CjxXE9CV+5EURKCP1HRMqf+NGts3kBUEgE+WwyXSzLtUCgYEAifVnXhUJWnXfCTWmm0e/Gb9qmcOrtQ7FOqZbVk80C92Yhu/dmdikJhdCP1Ih1D4l25pBAEknjAyY0e7J+bhm6BfBZivWVqeyYnel685MOOsQJvXXqsH5mh27Y5HFqhWsZ0sMqMQQV+4nhgd8/quRCNQIkeb8CWbvHTCeASLWiU0CgYBlt64XAY97rVlVxs5Tla2w+Kz4ZV/OKzoONElCCvz9Nk47t5XV+tDMCa3LQYAtD6gX/PoP2S/XO/15pStUvcWesOf5q34Bms0739JyrNN+Ca9ur6TndSx85Mds9PiFCGWcxZqHyuFnp79bdP5Cj2uU4/2n3dogQD7T/anVxgmAlQKBgF2DJJdE+pJ0rphKtzUlFy6NemL2hst+nq6hnJx+8ltYXSrsNYIl5wTxuktV1oszm+PFMgH3dCAw4igpr6SmFyIs3Meqf2mBVKf9xVddX1Q53DBmwQge5uMl4E6TW5VVmt1TOwhjhV9cCDPU9BzQeHUkXJiWyUd1Olu85Hfl+dTb";
    // 注：证书文件路径支持设置为文件系统中的路径或CLASS_PATH中的路径，优先从文件系统中加载，加载失败后会继续尝试从CLASS_PATH中加载
    config.merchantCertPath = "appPublicCert.crt";
    config.alipayCertPath = "alipayPublicCert.crt";
    config.alipayRootCertPath = "alipayRootCert.crt";
    // 注：如果采用非证书模式，则无需赋值上面的三个证书路径，改为赋值如下的支付宝公钥字符串即可
    // config.alipayPublicKey = "<-- 请填写您的支付宝公钥，例如：MIIBIjANBg... -->";
    // 可设置异步通知接收服务地址（可选）
    config.notifyUrl = "http://ft572j.natappfree.cc/web/callback";
    // 可设置AES密钥，调用AES加解密相关接口时需要（可选）
    config.encryptKey = "";
    return config;
  }

  /**
   * 公钥模式
   *
   * @return c
   */
  private static Config getOptions() {
    Config config = new Config();
    config.protocol = "https";
    config.gatewayHost = "openapi-sandbox.dl.alipaydev.com";
    config.signType = "RSA2";
    config.appId = "9021000141623931";
    // 为避免私钥随源码泄露，推荐从文件中读取私钥字符串而不是写入源码中
    config.merchantPrivateKey =
        "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCJNJsLy/L+SrrEXnI5Gx3Kw5LcWR4ZzyGfz2r94euqYCf8yIzXGU26Ey/Fzpf6fx70aW6aQMg+BgLaehyX8wG+m44s7qidNN/Uhh9ertsWHA89ZOBpVqV4F7G5tQVAesaQowIMANNfsvRbf+w02+FdvB2Fp3on+PQH1EPcqKo+SkaPsNpiNTKaOVfCd7m9YONEjwg70/Ni/2QZBDfV34hM5v7T1YxNz+VRfDsdLzQ3Q8Pr+v5XhQcl5FWrnwlf99eSIXeccio3hYyNUyTd1XibfVl22WH/+zfx2g7JHVhHRoLS7d0ZzoznVlJSiIkDa3mmruVPz0iswxsR42En7eJ5AgMBAAECggEBAIe7S9q6szNPOD5E9Jh7s9g/64wieT2tBCV1avGnypxsrYJL39B2zsbSbkMwIncDxNqjZnAgwDdjePUrMhg7pTEr79WRb6q9gORcnrHrJv9TWNwybDKpLd6FCiOd6YklLEQvjjnTo0eb4FKYVVLhZqx1CXuL97ONe9Q2779d97TQCfhTUpciaJ7+CVvXybo2Z3fi6AmYoh+frUSTFzQkfCgVED0mLpOy7Tvj+DEPIs4OPZ8sHtrZ2t1ElRtQjPueZSf3uESy4qGqTygchFvODk5U4R+1wN2VCVustP6+m70BuRD39K/8o736V9GmBO0j8Nk+vw5Chh74/5M6NEveyJECgYEA44rUO0Z5DgVONvXj5ywTrDW1XNjlXTwyIhraX2GGckPep/aY3sA5TlHjgFWQ9NJhSGxESHSV8R5K6z7foPkNaRCp+Gsh2LnDGtWACCu1FFW/AaFFVWU8dxUO1T9VRJqWwIrgz03ub+ztF4dcZUG+5I363weo8u8d9zksckxyXxUCgYEAml17sQNycnNaS7mTfxoYvENaeSuHxxv8wovuzIddnzEcLQQa/lOEnrf63CxKYDXPKcTc252R5Wfzj5juFES1HVEm8cB8VAQkiEFK8EfWGCJFwDilOC4SbLDwu989Ja2CjxXE9CV+5EURKCP1HRMqf+NGts3kBUEgE+WwyXSzLtUCgYEAifVnXhUJWnXfCTWmm0e/Gb9qmcOrtQ7FOqZbVk80C92Yhu/dmdikJhdCP1Ih1D4l25pBAEknjAyY0e7J+bhm6BfBZivWVqeyYnel685MOOsQJvXXqsH5mh27Y5HFqhWsZ0sMqMQQV+4nhgd8/quRCNQIkeb8CWbvHTCeASLWiU0CgYBlt64XAY97rVlVxs5Tla2w+Kz4ZV/OKzoONElCCvz9Nk47t5XV+tDMCa3LQYAtD6gX/PoP2S/XO/15pStUvcWesOf5q34Bms0739JyrNN+Ca9ur6TndSx85Mds9PiFCGWcxZqHyuFnp79bdP5Cj2uU4/2n3dogQD7T/anVxgmAlQKBgF2DJJdE+pJ0rphKtzUlFy6NemL2hst+nq6hnJx+8ltYXSrsNYIl5wTxuktV1oszm+PFMgH3dCAw4igpr6SmFyIs3Meqf2mBVKf9xVddX1Q53DBmwQge5uMl4E6TW5VVmt1TOwhjhV9cCDPU9BzQeHUkXJiWyUd1Olu85Hfl+dTb";
    // 注：证书文件路径支持设置为文件系统中的路径或CLASS_PATH中的路径，优先从文件系统中加载，加载失败后会继续尝试从CLASS_PATH中加载
    // config.merchantCertPath ="<-- 请填写您的应用公钥证书文件路径，例如：/foo/appCertPublicKey_2019051064521003.crt
    // -->";
    // config.alipayCertPath = "<-- 请填写您的支付宝公钥证书文件路径，例如：/foo/alipayCertPublicKey_RSA2.crt -->";
    // config.alipayRootCertPath = "<-- 请填写您的支付宝根证书文件路径，例如：/foo/alipayRootCert.crt -->";
    // 注：如果采用非证书模式，则无需赋值上面的三个证书路径，改为赋值如下的支付宝公钥字符串即可
    config.alipayPublicKey =
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAph62/spNEP8bO3qWjadxxx97bS+jJepEC1y+Rwk63BL9vTCZ4U3E0fy4lCLOqaWzY1JSxxVsIf5NePaBRE9mzRoYZCQX/SCwCaU6EdcJaUE9qyjbMWJ93F9sbKg46+NaeaRw8pj6J56j0Ca4lDIPWJpRAGP5RUFT2TROcybCHYQScseY2mDwrQwExjNp2CEBo06fCnjXkzHhlFgZS3b/8jjorcXRFmGLctUEpHm8BI0onRENPIbkveXS6Z3mjTEvZPTddSEhL+GfdpNvqKAUDIlK1Is1zkgGKLHjat30XsOhLilvcJBFx0VUvPfbYRAau2Ea4SAdSOvW7hMl/7OkkQIDAQAB";
    // 可设置异步通知接收服务地址（可选）
    config.notifyUrl = "";
    // 可设置AES密钥，调用AES加解密相关接口时需要（可选）
    config.encryptKey = "";
    return config;
  }
}
