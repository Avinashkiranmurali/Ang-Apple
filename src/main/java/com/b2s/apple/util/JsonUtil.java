package com.b2s.apple.util;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class JsonUtil {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);
    public static final String CONFIG_DATA = "configData";
    private static final String ANON = "anon";
    public static final String TEMPLATES = "templates";
    public static final String HEADER = "header";
    public static final String SUPPESS_LOGO_TEMPLATE  = "suppressLogoTemplate";
    public static final String PATH_DELIMITER = "/";
    /**
     * Given the configData JSON removes the header element
     *
     * @param json expects the ConfigData Json
     * @return JSON String
     */
    public static String removeHeader(String json) {

        if (StringUtils.isNotBlank(json) && json.contains(CONFIG_DATA) && json.contains(HEADER)) {
            logger.debug("removing header...");
            JSONObject jsonObject = new JSONObject(json);
            jsonObject.getJSONObject(CONFIG_DATA).getJSONObject(TEMPLATES).remove(HEADER);
            jsonObject.getJSONObject(ANON).getJSONObject(CONFIG_DATA).getJSONObject(TEMPLATES).remove(HEADER);
            return jsonObject.toString();
        }

        return json;
    }

    /**
     * Given the json, value, and path to root element reference adds the new element to the root element
     * @param json
     * @param pathToRootElement
     * @param newElement
     * @param value
     * @return
     */
    public static String addElementTo(String json, String pathToRootElement, String newElement, Object value) {

        logger.debug("adding element: {} with value: {} to element {}...", newElement, value, pathToRootElement);
        JSONObject jsonObject = new JSONObject(json);
        if (pathToRootElement.contains(PATH_DELIMITER)) {
            AtomicReference<JSONObject> lastElement = new AtomicReference<JSONObject>();
            Arrays.stream(pathToRootElement.split(PATH_DELIMITER)).forEach(key -> {
                if (lastElement.get() == null) {
                    lastElement.set(jsonObject.getJSONObject(key));
                } else {
                    lastElement.set(lastElement.get().getJSONObject(key));
                }
            });
            if (Objects.nonNull(lastElement.get())) {
                lastElement.get().put(newElement, value);
            }
            return jsonObject.toString();
        }

        jsonObject.getJSONObject(pathToRootElement).put(newElement, value);

        return jsonObject.toString();
    }
}
