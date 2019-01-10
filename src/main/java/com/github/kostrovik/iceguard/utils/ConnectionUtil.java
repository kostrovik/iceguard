package com.github.kostrovik.iceguard.utils;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * project: iceguard
 * author:  kostrovik
 * date:    2019-01-10
 * github:  https://github.com/kostrovik/iceguard
 */
public class ConnectionUtil {
    public URI buildUri(String apiUrl, Map<String, List<String>> urlParams, Charset charset) {
        return URI.create(apiUrl + prepareQueryParams(urlParams, charset));
    }

    public String prepareQueryParams(Map<String, List<String>> urlParams, Charset charset) {
        if (Objects.isNull(urlParams)) {
            return "";
        }
        return urlParams.keySet().stream().map(key -> {
            if (urlParams.get(key).size() == 1) {
                return String.format("%s=%s", key, encodeValue(urlParams.get(key).get(0), charset));
            } else {
                return urlParams.get(key).stream().map(val -> String.format("%s=%s", key, encodeValue(val, charset))).collect(Collectors.joining("&"));
            }
        }).collect(Collectors.joining("&"));
    }

    private String encodeValue(String value, Charset charset) {
        Objects.requireNonNull(value);
        return URLEncoder.encode(value, charset);
    }
}
