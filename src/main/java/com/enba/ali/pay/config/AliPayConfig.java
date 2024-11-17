package com.enba.ali.pay.config;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

/** 演示使用alipay-easysdk 初始化参数 */
@Configuration
public class AliPayConfig implements InitializingBean {

  Logger log = LoggerFactory.getLogger(AliPayConfig.class);

  @Override
  public void afterPropertiesSet() throws Exception {

    // 初始化支付宝支付相关配置
    Factory.setOptions(getSandBoxOptions());

    log.info("初始化支付宝支付相关配置初始化完成");
  }

  /**
   * 沙箱环境 证书模式 （！！！这里为了简单演示，参数值直接写上，实际开发中请从环境变量、配置文件等外部存储中读取该值）
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
}
