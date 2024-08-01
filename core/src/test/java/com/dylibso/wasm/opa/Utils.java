package com.dylibso.wasm.opa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Utils {
    static ObjectMapper objectMapper = new ObjectMapper();

    public static JsonNode getResult(String jsonStr) {
        JsonNode json;
        try {
            json = objectMapper.readTree(jsonStr);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return json.elements().next().findValue("result");
    }

    public static String rootCauseMessage(Throwable e) {
        if (e.getCause() == null) {
            return  e.getMessage();
        } else {
            return rootCauseMessage(e.getCause());
        }
    }
}
