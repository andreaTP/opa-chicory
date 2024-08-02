package com.dylibso.wasm.opa.builtins;

import com.dylibso.wasm.opa.Builtin;
import com.dylibso.wasm.opa.OpaWasm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class Yaml {

    public static ObjectMapper jsonMapper = new ObjectMapper();
    public static ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    // maybe: .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));

    public static final Builtin.Builtin1 isValid =
            new Builtin.Builtin1(
                    "yaml.is_valid",
                    (OpaWasm instance, int strAddr) -> {
                        var boxedYamlStr = instance.readString(strAddr);
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
                        return instance.writeResult(jsonStr);
                    });

    public static final Builtin.Builtin1 unmarshal =
            new Builtin.Builtin1(
                    "yaml.unmarshal",
                    (OpaWasm instance, int strAddr) -> {
                        var boxedYamlStr = instance.readString(strAddr);
                        try {
                            var boxedYaml = jsonMapper.readTree(boxedYamlStr);
                            if (!boxedYaml.isTextual()) {
                                throw new RuntimeException(
                                        "yaml is not correctly boxed in a Json string");
                            } else {
                                try {
                                    var tree = yamlMapper.readTree(boxedYaml.asText());
                                    var jsonStr = jsonMapper.writeValueAsString(tree);
                                    return instance.writeResult(jsonStr);
                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    });

    public static final Builtin.Builtin1 marshal =
            new Builtin.Builtin1(
                    "yaml.marshal",
                    (OpaWasm instance, int strAddr) -> {
                        var jsonStr = instance.readString(strAddr);
                        try {
                            var json = jsonMapper.readTree(jsonStr);
                            var resultStr =
                                    jsonMapper.writeValueAsString(
                                            TextNode.valueOf(yamlMapper.writeValueAsString(json)));
                            return instance.writeResult(resultStr);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    });

    public static Builtin.IBuiltin[] all() {
        return new Builtin.IBuiltin[] {isValid, marshal, unmarshal};
    }
}
