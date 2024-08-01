package com.dylibso.wasm.opa.builtins;

import static com.dylibso.wasm.opa.Opa.OpaPolicy.dumpJson;
import static com.dylibso.wasm.opa.Opa.OpaPolicy.loadJson;

import com.dylibso.wasm.opa.Builtin;
import com.dylibso.wasm.opa.OpaWasm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

public class Yaml {

    public static ObjectMapper jsonMapper = new ObjectMapper();
    public static ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    // maybe: .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));

    public static final Builtin.Builtin1 isValid =
            new Builtin.Builtin1(
                    "yaml.is_valid",
                    (OpaWasm instance, int strAddr) -> {
                        var str = dumpJson(instance, strAddr);
                        try {
                            yamlMapper.readTree(str);
                            // true - is valid
                            return loadJson(instance, jsonMapper.writeValueAsString(true));
                        } catch (JsonProcessingException e) {
                            try {
                                return loadJson(instance, jsonMapper.writeValueAsString(false));
                            } catch (JsonProcessingException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    });

    public static final Builtin.Builtin1 unmarshal =
            new Builtin.Builtin1(
                    "yaml.unmarshal",
                    (OpaWasm instance, int strAddr) -> {
                        var str = dumpJson(instance, strAddr);
                        try {
                            var tree = yamlMapper.readTree(jsonMapper.readTree(str).textValue());
                            return loadJson(instance, jsonMapper.writeValueAsString(TextNode.valueOf(yamlMapper.writeValueAsString(tree))));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    });

    public static final Builtin.Builtin1 marshal =
            new Builtin.Builtin1(
                    "yaml.marshal",
                    (OpaWasm instance, int strAddr) -> {
                        var str = dumpJson(instance, strAddr);
                        try {
                            var tree = jsonMapper.readTree(str);
                            return loadJson(instance, yamlMapper.writeValueAsString(tree));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    });

    public static Builtin.IBuiltin[] all() {
        return new Builtin.IBuiltin[] {isValid, marshal, unmarshal};
    }
}
