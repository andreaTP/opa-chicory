package com.github.andreaTP.opa.chicory.builtins;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.github.andreaTP.opa.chicory.Opa;
import com.github.andreaTP.opa.chicory.OpaBuiltin;

public class Json {

    private static JsonNode isValidImpl(JsonNode boxedJson) {
        if (!boxedJson.isTextual()) {
            return BooleanNode.getFalse();
        } else {
            try {
                Opa.jsonMapper.readTree(boxedJson.asText());
                return BooleanNode.getTrue();
            } catch (JsonProcessingException e) {
                return BooleanNode.getFalse();
            }
        }
    }

    public static final OpaBuiltin.Builtin isValid =
            OpaBuiltin.from("json.is_valid", Json::isValidImpl);

    public static OpaBuiltin.Builtin[] all() {
        return new OpaBuiltin.Builtin[] {isValid};
    }
}
