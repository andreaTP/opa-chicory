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

    //    private static int loadJson(OpaWasm wasm, String data) {
    //        var dataStrAddr = wasm.opaMalloc(data.length());
    //        wasm.memory().writeCString(dataStrAddr, data);
    //        var dstAddr = wasm.opaJsonParse(dataStrAddr, data.length());
    //        wasm.opaFree(dataStrAddr);
    //        return dstAddr;
    //    }
    //
    //    private static String dumpJson(OpaWasm wasm, int addr) {
    //        int resultStrAddr = wasm.opaJsonDump(addr);
    //        var result = wasm.memory().readCString(resultStrAddr);
    //        wasm.opaFree(resultStrAddr);
    //        return result;
    //    }

    public static final Builtin.Builtin1 isValid =
            new Builtin.Builtin1(
                    "yaml.is_valid",
                    (OpaWasm instance, int strAddr) -> {
                        System.out.println("Yaml is valid");
//                        var str = dumpJson(instance, strAddr);
//                        try {
//                            yamlMapper.readTree(str);
//                            // true - is valid
//                            return loadJson(instance, jsonMapper.writeValueAsString(true));
//                        } catch (JsonProcessingException e) {
//                            try {
//                                return loadJson(instance, jsonMapper.writeValueAsString(false));
//                            } catch (JsonProcessingException ex) {
//                                throw new RuntimeException(ex);
//                            }
//                        }
                        return 1;
                    });

    public static final Builtin.Builtin1 unmarshal =
            new Builtin.Builtin1(
                    "yaml.unmarshal",
                    (OpaWasm instance, int strAddr) -> {
                        System.out.println("Yaml unmarshal");
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
