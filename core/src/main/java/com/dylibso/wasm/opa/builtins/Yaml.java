package com.dylibso.wasm.opa.builtins;

import com.dylibso.wasm.opa.Builtin;
import com.dylibso.wasm.opa.OpaWasm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class Yaml {

    public static ObjectMapper jsonMapper = new ObjectMapper();
    public static ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    // maybe: .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));

    public static final Builtin.Builtin1 isValid =
            new Builtin.Builtin1(
                    "yaml.is_valid",
                    (OpaWasm instance, int strAddr) -> {
                        int resultYamlAddr = instance.opaJsonDump(strAddr);
                        var boxedYamlStr = instance.memory().readCString(resultYamlAddr);
                        instance.opaFree(resultYamlAddr);
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
                        var jsonAddr = instance.opaMalloc(jsonStr.length());
                        instance.memory().writeCString(jsonAddr, jsonStr);
                        var resultAddr = instance.opaJsonParse(jsonAddr, jsonStr.length());
                        instance.opaFree(jsonAddr);
                        return resultAddr;
                    });

    public static final Builtin.Builtin1 unmarshal =
            new Builtin.Builtin1(
                    "yaml.unmarshal",
                    (OpaWasm instance, int strAddr) -> {
                        var yamlStr = instance.memory().readCString(strAddr);
                        try {
                            var tree = yamlMapper.readTree(yamlStr);
                            var jsonStr = jsonMapper.writeValueAsString(tree);
                            var jsonAddr = instance.opaMalloc(jsonStr.length());
                            instance.memory().writeCString(jsonAddr, jsonStr);
                            var resultAddr = instance.opaJsonParse(jsonAddr, jsonStr.length());
                            instance.opaFree(jsonAddr);
                            return resultAddr;
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    });

    public static final Builtin.Builtin1 marshal =
            new Builtin.Builtin1(
                    "yaml.marshal",
                    (OpaWasm instance, int strAddr) -> {
                        int resultYamlAddr = instance.opaJsonDump(strAddr);
                        var yamlStr = instance.memory().readCString(resultYamlAddr);
                        instance.opaFree(resultYamlAddr);
                        try {
                            var yaml = yamlMapper.readTree(yamlStr);
                            var resultStr = yamlMapper.writeValueAsString(yaml);
                            var resultStrAddr = instance.opaMalloc(resultStr.length());
                            instance.memory().writeCString(resultStrAddr, resultStr);
                            return resultStrAddr;
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    });

    public static Builtin.IBuiltin[] all() {
        return new Builtin.IBuiltin[] {isValid, marshal, unmarshal};
    }
}
