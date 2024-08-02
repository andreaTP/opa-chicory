package com.dylibso.wasm.opa;

import static com.dylibso.wasm.opa.Utils.getResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dylibso.wasm.opa.builtins.Yaml;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class OpaYamlSupportTest {
    static ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    static Opa.OpaPolicy policy;

    @BeforeAll
    public static void beforeAll() throws Exception {
        policy =
                Opa.loadPolicy(
                        OpaCli.compile(
                                        "yaml-support",
                                        "yaml/support/canParseYAML",
                                        "yaml/support/hasSyntaxError",
                                        "yaml/support/hasSemanticError",
                                        "yaml/support/hasReferenceError",
                                        "yaml/support/hasYAMLWarning",
                                        "yaml/support/canMarshalYAML",
                                        "yaml/support/isValidYAML")
                                .resolve("policy.wasm"),
                        new OpaDefaultImports(Yaml.all()));
    }

    @Test
    public void shouldUnmarshallYamlStrings() {
        var result = getResult(policy.entrypoint("yaml/support/canParseYAML").evaluate("{}"));
        assertTrue(result.asBoolean());
    }

    //
    //    it("should ignore YAML syntax errors", () => {
    //        expect(() => policy.evaluate({}, "yaml/support/hasSyntaxError")).not
    //                .toThrow();
    //    const result = policy.evaluate({}, "yaml/support/hasSyntaxError");
    //        expect(result.length).toBe(0);
    //    });
    //
    //    it("should ignore YAML semantic errors", () => {
    //        expect(() => policy.evaluate({}, "yaml/support/hasSemanticError")).not
    //                .toThrow();
    //    const result = policy.evaluate({}, "yaml/support/hasSemanticError");
    //        expect(result.length).toBe(0);
    //    });
    //
    //    it("should ignore YAML reference errors", () => {
    //        expect(() => policy.evaluate({}, "yaml/support/hasReferenceError")).not
    //                .toThrow();
    //    const result = policy.evaluate({}, "yaml/support/hasReferenceError");
    //        expect(result.length).toBe(0);
    //    });
    //
    //    it("should ignore YAML warnings", () => {
    //        expect(() => policy.evaluate({}, "yaml/support/hasYAMLWarning")).not
    //                .toThrow();
    //    const result = policy.evaluate({}, "yaml/support/hasYAMLWarning");
    //        expect(result.length).toBe(0);
    //    });

    @Test
    public void shouldMarshalYaml() throws Exception {
        var result =
                getResult(
                        policy.entrypoint("yaml/support/canMarshalYAML")
                                .evaluate("[{ \"foo\": [1, 2, 3] }]"));
        var array = result.elements().next();
        var foo = array.findValue("foo").elements();
        assertEquals(1, foo.next().asInt());
        assertEquals(2, foo.next().asInt());
        assertEquals(3, foo.next().asInt());
    }

    @Test
    public void shouldValidateYaml() throws Exception {
        var result = getResult(policy.entrypoint("yaml/support/isValidYAML").evaluate("{}"));
        assertTrue(result.asBoolean());
    }
}
