package org.example.zlib.util;

import okhttp3.OkHttpClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

/**
 * @author kit
 * @version 1.0
 * @description: TODO
 * @date 2024/12/12 10:22
 */
public class HttpUtil {
    /**
     * SocketTimeOut read()方法阻塞的最大时间，也是，两个packet的间隔时间,默认设置3分钟
     * read time out ：是已经建立连接，并开始读取服务端资源。如果到了指定的时间，没有可能的数据被客户端读取，则报异常。
     */
    static int maxSocketTimeOut = 1000 * 300;
    /**
     * http建立链接的最大时间，也就是完成三次握手，建立tcp链接所用的时间
     * connect time out ：如果到了指定的时间，还没建立连接，则报异常。
     */
    static int maxConnectTimeOut = 1000 * 60;
    /**
     * httpclient使用连接池来管理连接，这个时间就是从连接池获取连接的超时时间
     * connect time out ：如果到了指定的时间，还没建立连接，则报异常。
     */
    static int maxConnectRequestTimeout = 1000 * 60;
    public static OkHttpClient getUnsafeOkHttpClient() {
        try {
            X509TrustManager unsafeX509 = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[]{};
                }
            };
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    unsafeX509
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory,unsafeX509);
//            builder.sslSocketFactory(sslSocketFactory);
            builder.hostnameVerifier((hostname, session) -> Boolean.TRUE);
            // 更新超时
            builder.connectTimeout(maxConnectTimeOut, TimeUnit.SECONDS);
            builder.callTimeout(maxConnectRequestTimeout,TimeUnit.SECONDS);
            builder.readTimeout(maxSocketTimeOut,TimeUnit.SECONDS);
            OkHttpClient okHttpClient = builder.build();
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
