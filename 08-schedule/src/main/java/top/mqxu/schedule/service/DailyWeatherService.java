package top.mqxu.schedule.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

/**
 * 每日天气推送服务
 *
 * 对外方法（供 WeatherController 调用）：
 * - getTodayWeather()  查询今日天气，返回 fastjson2 JSONObject，失败返回 null
 * - sendWeatherEmail() 发送天气邮件，成功返回 true，失败返回 false
 *
 * 注意：和风天气 API 响应是 GZIP 压缩的（Content-Encoding: gzip），
 * 必须先解压再按 UTF-8 解码，否则解析 JSON 会报 Unrecognized token。
 */
@Service
public class DailyWeatherService {

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36";

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.city}")
    private String city;

    @Value("${weather.url}")
    private String url;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${mail.to}")
    private String to;

    @Resource
    private JavaMailSender mailSender;

    private final RestTemplate restTemplate;

    public DailyWeatherService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(10_000);
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * 每天 09:53:30 自动执行；改成早上 7 点用 "0 0 7 * * ?"，调试用 "0/30 * * * * ?"
     */
    @Scheduled(cron = "30 16 11 * * ?")
    public void sendDailyWeather() {
        sendWeatherEmail();
    }

    /**
     * 查询今日天气（只查数据，不发邮件）
     *
     * @return daily 数组第 0 项；失败返回 null
     */
    public JSONObject getTodayWeather() throws IOException {
        String weatherUrl = url + "?key=" + apiKey + "&location=" + city;

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, USER_AGENT);
        headers.set(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<byte[]> resp = restTemplate.exchange(
                weatherUrl, HttpMethod.GET, entity, byte[].class);

        byte[] body = resp.getBody() == null ? new byte[0] : resp.getBody();
        if (body.length == 0) {
            System.err.println("❌ 天气接口返回空响应，url=" + weatherUrl);
            return null;
        }

        // 核心：先解压 gzip，再解码
        String contentEncoding = resp.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING);
        String response = decodeBody(body, contentEncoding);

        JSONObject root = JSON.parseObject(response);
        String code = root.getString("code");
        if (!"200".equals(code)) {
            System.err.println("❌ 和风天气返回错误，code=" + code + "，响应=" + response);
            return null;
        }

        JSONArray daily = root.getJSONArray("daily");
        if (daily == null || daily.isEmpty()) {
            System.err.println("❌ 天气数据为空，响应=" + response);
            return null;
        }
        return daily.getJSONObject(0);
    }

    /**
     * 发送今日天气邮件
     *
     * @return true 发送成功 / false 发送失败
     */
    public boolean sendWeatherEmail() {
        try {
            JSONObject today = getTodayWeather();
            if (today == null) {
                return false;
            }

            String fxDate = today.getString("fxDate");
            String tempMax = today.getString("tempMax");
            String tempMin = today.getString("tempMin");
            String textDay = today.getString("textDay");
            String windDirDay = today.getString("windDirDay");
            String windScaleDay = today.getString("windScaleDay");
            String humidity = today.getString("humidity");
            String uvIndex = today.getString("uvIndex");

            String content =
                    "☀️ 早安！今日天气播报 ☀️\n\n" +
                            "━━━━━━━━━━━━━━━━━\n" +
                            "📅 日期：" + fxDate + "\n" +
                            "🌤 天气：" + textDay + "\n" +
                            "🌡 温度：" + tempMin + "℃ ~ " + tempMax + "℃\n" +
                            "🌬 风向：" + windDirDay + "\n" +
                            "💨 风力：" + windScaleDay + " 级\n" +
                            "💧 湿度：" + humidity + "%\n" +
                            "🕶 紫外线：" + uvIndex + "\n" +
                            "━━━━━━━━━━━━━━━━━\n" +
                            "温馨提示：请根据天气适当增减衣物，注意防寒~";

            String[] recipients = Arrays.stream(to.split("[,;\\s]+"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toArray(String[]::new);
            if (recipients.length == 0) {
                System.err.println("❌ 收件人列表为空，请检查 application.yml 中的 mail.to");
                return false;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(recipients);
            message.setSubject("🌤 每日天气推送 - "
                    + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            message.setText(content);

            mailSender.send(message);
            System.out.println("✅ 邮件发送成功！发往 " + String.join(", ", recipients));
            return true;
        } catch (Exception e) {
            System.err.println("❌ 天气推送失败：" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 字节解码：先判断 gzip（响应头或 0x1F 0x8B 魔数），解压后再按 UTF-8 转字符串
     */
    private String decodeBody(byte[] body, String contentEncoding) throws IOException {
        boolean isGzip = (contentEncoding != null && contentEncoding.toLowerCase().contains("gzip"))
                || (body.length > 2 && (body[0] & 0xFF) == 0x1F && (body[1] & 0xFF) == 0x8B);

        String text;
        if (isGzip) {
            try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(body))) {
                text = new String(gzip.readAllBytes(), StandardCharsets.UTF_8);
            }
        } else {
            text = new String(body, StandardCharsets.UTF_8);
        }

        if (!text.isEmpty() && text.charAt(0) == '\uFEFF') {
            text = text.substring(1);
        }
        return text.trim();
    }
}
