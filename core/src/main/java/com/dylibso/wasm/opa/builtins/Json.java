package com.dylibso.wasm.opa.builtins;

import static com.dylibso.wasm.opa.Opa.jsonMapper;

import com.dylibso.wasm.opa.OpaBuiltin;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;

public class Json {

    private static JsonNode isValidImpl(JsonNode boxedJson) {
        if (!boxedJson.isTextual()) {
            return BooleanNode.getFalse();
        } else {
            try {
                jsonMapper.readTree(boxedJson.asText());
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
