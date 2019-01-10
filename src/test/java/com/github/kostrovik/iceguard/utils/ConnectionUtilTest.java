package com.github.kostrovik.iceguard.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * project: iceguard
 * author:  kostrovik
 * date:    2019-01-10
 * github:  https://github.com/kostrovik/iceguard
 */
public class ConnectionUtilTest {
    ConnectionUtil util;

    @BeforeEach
    void init() {
        util = new ConnectionUtil();
    }

    @Test
    void prepareQueryParamsTest() {
        Map<String, List<String>> urlParams = new HashMap<>();
        List<String> names = new ArrayList<>();
        names.add("test1");
        names.add("test2");
        names.add("test3");

        urlParams.put("name", names);

        Charset charset = Charset.forName("UTF-8");

        String result = util.prepareQueryParams(urlParams, charset);

        assertEquals("name=test1&name=test2&name=test3", result);
    }
}