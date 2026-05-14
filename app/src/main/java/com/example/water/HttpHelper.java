package com.example.water;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 网络请求工具类 — 异步 GET 请求，解析 JSON 并回调
 */
public class HttpHelper {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface Callback {
        void onSuccess(String tip);
        void onError(String error);
    }

    /**
     * 异步发起 GET 请求获取一条健康贴士
     *
     * 示例接口：一言（Hitokoto）— 返回 JSON 格式的名言/句子
     * URL: https://v1.hitokoto.cn/?c=d&c=f   (励志/人生)
     *
     * 返回 JSON 示例：
     * {
     *   "hitokoto": "多喝水，身体好",
     *   "from": "健康百科"
     * }
     */
    public static void fetchHealthTip(Callback callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("https://v1.hitokoto.cn/?c=d&c=f");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                connection.setRequestProperty("Accept", "application/json");

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    postError(callback, "服务器响应异常: " + responseCode);
                    return;
                }

                // 读取响应流
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                );
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // 解析 JSON
                JSONObject json = new JSONObject(response.toString());
                String content = json.optString("hitokoto", "");
                String from = json.optString("from_who", "");
                if (from.isEmpty()) {
                    from = json.optString("from", "未知");
                }

                String tip = content + (from.isEmpty() ? "" : " —— " + from);
                postSuccess(callback, tip);

            } catch (java.net.SocketTimeoutException e) {
                postError(callback, "网络请求超时，请检查网络连接");
            } catch (JSONException e) {
                postError(callback, "数据解析失败");
            } catch (Exception e) {
                postError(callback, "网络请求失败: " + e.getLocalizedMessage());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    /**
     * 如果有 JSON 字段规范不同，可在此解析通用接口
     * 示例备用接口：https://api.btstu.cn/yan/api.php?charset=utf-8&type=json
     */
    public static void fetchHealthTipFromYan(Callback callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("https://api.btstu.cn/yan/api.php?charset=utf-8&type=json");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    postError(callback, "服务器响应异常: " + responseCode);
                    return;
                }

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "UTF-8")
                );
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(response.toString());
                String tip = json.optString("text", "多喝水有益健康！");
                postSuccess(callback, tip);

            } catch (Exception e) {
                postError(callback, "请求失败: " + e.getLocalizedMessage());
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    private static void postSuccess(Callback callback, String tip) {
        mainHandler.post(() -> {
            if (callback != null) callback.onSuccess(tip);
        });
    }

    private static void postError(Callback callback, String error) {
        mainHandler.post(() -> {
            if (callback != null) callback.onError(error);
        });
    }
}
