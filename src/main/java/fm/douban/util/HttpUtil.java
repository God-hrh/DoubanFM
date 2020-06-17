package fm.douban.util;

import com.alibaba.fastjson.JSON;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Heerh
 * @version 1.0
 * @date 2020/5/30 17:35
 */
public class HttpUtil {
    private static final Logger LOG = LoggerFactory.getLogger(HttpUtil.class);

    // okHttpClient 实例
    private static OkHttpClient okHttpClient;
    //构建必要的http header。也许爬虫有用
    public static Map<String,String> buildHeaderData(String referer,String host){
        Map<String,String> headersMap = new HashMap<>();
        headersMap.put("referer",referer);
        headersMap.put("host",host);
        return headersMap;
    }
    //根据输入的url，读取
    public static String getContent(String url,Map<String,String> headers){
        // okHttpClient 实例
         okHttpClient = new OkHttpClient.Builder().connectTimeout(2, TimeUnit.MINUTES)
                .readTimeout(4, TimeUnit.MINUTES).build();
        // 定义request组装请求头
        Request.Builder builder = new Request.Builder().url(url);
        for (String key:headers.keySet()){
            builder.addHeader(key,headers.get(key));
        }
        Request request = builder.build();
        // 使用client去请求
        Call call = okHttpClient.newCall(request);
        // 返回结果字符串
        String result = null;
        try {
            // 获得返回结果
            result = call.execute().body().string();

        } catch (IOException e) {
            // 抓取异常
            System.out.println("request " + url + " error . ");
            e.printStackTrace();
        }
        return result;
    }
}
