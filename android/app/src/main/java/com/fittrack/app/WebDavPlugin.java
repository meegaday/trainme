package com.fittrack.app;

import android.util.Base64;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 直连坚果云 WebDAV（原生 HTTP，绕过 WebView CORS）。
 * 凭据由 JS 传入，存在本机（localStorage），不再经过 Cloudflare Worker。
 */
@CapacitorPlugin(name = "WebDav")
public class WebDavPlugin extends Plugin {

    private static final int TIMEOUT = 20000;

    @PluginMethod
    public void getData(PluginCall call) {
        String url = call.getString("url");
        String user = call.getString("user");
        String pass = call.getString("pass");
        if (url == null || user == null || pass == null) {
            call.reject("missing url/user/pass");
            return;
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            conn.setRequestProperty("Authorization", basicAuth(user, pass));
            int code = conn.getResponseCode();
            if (code == 404) {
                JSObject r = new JSObject();
                r.put("empty", true);
                call.resolve(r);
                conn.disconnect();
                return;
            }
            if (code == 401 || code == 403) {
                JSObject r = new JSObject();
                r.put("error", "auth");
                r.put("status", code);
                call.resolve(r);
                conn.disconnect();
                return;
            }
            if (code < 200 || code >= 300) {
                JSObject r = new JSObject();
                r.put("error", "http");
                r.put("status", code);
                call.resolve(r);
                conn.disconnect();
                return;
            }
            String text = readStream(conn.getInputStream());
            JSObject r = new JSObject();
            r.put("ok", true);
            r.put("text", text);
            call.resolve(r);
            conn.disconnect();
        } catch (Exception e) {
            call.reject("request failed: " + e.getMessage());
        }
    }

    @PluginMethod
    public void putData(PluginCall call) {
        String url = call.getString("url");
        String user = call.getString("user");
        String pass = call.getString("pass");
        String body = call.getString("body");
        if (url == null || user == null || pass == null || body == null) {
            call.reject("missing params");
            return;
        }
        try {
            // 确保目录存在（坚果云：已存在返回 405，忽略即可）
            String dir = url.substring(0, url.lastIndexOf('/'));
            mkcol(dir, user, pass);

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("PUT");
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", basicAuth(user, pass));
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
            int code = conn.getResponseCode();
            if (code == 401 || code == 403) {
                JSObject r = new JSObject();
                r.put("error", "auth");
                r.put("status", code);
                call.resolve(r);
                conn.disconnect();
                return;
            }
            if (code < 200 || code >= 300) {
                JSObject r = new JSObject();
                r.put("error", "http");
                r.put("status", code);
                call.resolve(r);
                conn.disconnect();
                return;
            }
            JSObject r = new JSObject();
            r.put("ok", true);
            call.resolve(r);
            conn.disconnect();
        } catch (Exception e) {
            call.reject("request failed: " + e.getMessage());
        }
    }

    private void mkcol(String dir, String user, String pass) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(dir).openConnection();
            conn.setRequestMethod("MKCOL");
            conn.setConnectTimeout(TIMEOUT);
            conn.setRequestProperty("Authorization", basicAuth(user, pass));
            try {
                conn.getResponseCode();
            } catch (Exception ignore) {
            }
            conn.disconnect();
        } catch (Exception ignore) {
        }
    }

    private String basicAuth(String user, String pass) {
        String cred = user + ":" + pass;
        return "Basic " + Base64.encodeToString(cred.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
    }

    private String readStream(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }
}
