package com.mro.web.service;

import com.mro.web.config.DingTalkProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DingTalkAlertService {

    private static final Logger log = LoggerFactory.getLogger(DingTalkAlertService.class);
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));
    private static final int MAX_STACK_LINES = 8;

    private final DingTalkProperties props;
    private final RestClient restClient = RestClient.create();

    @Async("logTaskExecutor")
    public void sendAlert(Exception ex, HttpServletRequest request) {
        if (!props.isEnabled() || props.getWebhook().isBlank()) {
            return;
        }
        try {
            String url = buildUrl();
            String body = buildBody(ex, request);
            restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("钉钉告警发送失败: {}", e.getMessage());
        }
    }

    private String buildUrl() throws Exception {
        if (props.getSecret().isBlank()) {
            return props.getWebhook();
        }
        long timestamp = System.currentTimeMillis();
        String stringToSign = timestamp + "\n" + props.getSecret();
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(props.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signBytes = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        String sign = URLEncoder.encode(Base64.getEncoder().encodeToString(signBytes), StandardCharsets.UTF_8);
        return props.getWebhook() + "&timestamp=" + timestamp + "&sign=" + sign;
    }

    private String buildBody(Exception ex, HttpServletRequest request) {
        String requestId = MDC.get("requestId");
        String time = FMT.format(Instant.now());
        String uri = request.getMethod() + " " + request.getRequestURI();
        String stackTrace = Arrays.stream(ex.getStackTrace())
                .limit(MAX_STACK_LINES)
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\\n"));

        String text = "## 系统异常告警\\n\\n"
                + "- **时间**: " + time + "\\n"
                + "- **服务**: manage-web\\n"
                + "- **接口**: " + uri + "\\n"
                + "- **RequestId**: " + (requestId != null ? requestId : "-") + "\\n"
                + "- **异常类型**: " + ex.getClass().getName() + "\\n"
                + "- **异常信息**: " + escapeJson(ex.getMessage()) + "\\n"
                + "- **堆栈摘要**:\\n```\\n" + stackTrace + "\\n```";

        return """
                {
                  "msgtype": "markdown",
                  "markdown": {
                    "title": "系统异常告警",
                    "text": "%s"
                  }
                }
                """.formatted(text);
    }

    private String escapeJson(String s) {
        if (s == null) return "null";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
