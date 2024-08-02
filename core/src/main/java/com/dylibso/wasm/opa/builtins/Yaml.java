package com.dylibso.wasm.opa.builtins;

import com.dylibso.wasm.opa.OpaBuiltin;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class Yaml {

    public static ObjectMapper jsonMapper = new ObjectMapper();
    public static ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    // maybe: .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));

    private static String isValidImpl(String boxedYamlStr) {
        boolean result = false;
        try {
            var boxedYaml = jsonMapper.readTree(boxedYamlStr);
            if (!boxedYaml.isTextual()) {
                result = false;
            } else {
                try {
                    yamlMapper.readTree(boxedYaml.asText());
                    result = true;
                } catch (JsonProcessingException e) {
                    result = false;
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        var jsonStr = "";
        try {
            jsonStr = jsonMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return jsonStr;
    }

    public static final OpaBuiltin.Builtin isValid =
            OpaBuiltin.from("yaml.is_valid", Yaml::isValidImpl);

    private static String unmarshalImpl(String boxedYamlStr) {
        try {
            var boxedYaml = jsonMapper.readTree(boxedYamlStr);
            if (!boxedYaml.isTextual()) {
                throw new RuntimeException("yaml is not correctly boxed in a Json string");
            } else {
                try {
                    var tree = yamlMapper.readTree(boxedYaml.asText());
                    var jsonStr = jsonMapper.writeValueAsString(tree);
                    return jsonStr;
                } catch (JsonProcessingException e) {
                    // should ignore errors here ...
                    return "";
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static final OpaBuiltin.Builtin unmarshal =
            OpaBuiltin.from("yaml.unmarshal", Yaml::unmarshalImpl);

    public static String marshalImpl(String jsonStr) {
        try {
            var json = jsonMapper.readTree(jsonStr);
            var resultStr =
                    jsonMapper.writeValueAsString(
                            TextNode.valueOf(yamlMapper.writeValueAsString(json)));
            return resultStr;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static final OpaBuiltin.Builtin marshal =
            OpaBuiltin.from("yaml.marshal", Yaml::marshalImpl);

    public static OpaBuiltin.Builtin[] all() {
        return new OpaBuiltin.Builtin[] {isValid, marshal, unmarshal};
    }
}
