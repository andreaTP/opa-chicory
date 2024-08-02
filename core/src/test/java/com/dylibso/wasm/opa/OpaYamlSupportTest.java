package com.dylibso.wasm.opa;

import com.dylibso.wasm.opa.builtins.Yaml;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.dylibso.wasm.opa.Utils.getResult;

public class OpaYamlSupportTest {
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

    //    @Test
    //    public void shouldUnmarshallYamlStrings() {
    //        var result = getResult(policy.entrypoint("yaml/support/canParseYAML").evaluate("{}"));
    //        assertTrue(result.asBoolean());
    //    }

    //    it("should unmarshall YAML strings", () => {
    //    const result = policy.evaluate({}, "yaml/support/canParseYAML");
    //        expect(result.length).not.toBe(0);
    //        expect(result[0]).toMatchObject({ result: true });
    //    });
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
        public void shouldMarshalYaml() {
            var result = getResult(policy.entrypoint("yaml/support/canMarshalYAML").evaluate("[{ \"foo\": [1, 2, 3] }]"));
            System.out.println("Result: " + result);
        }
    //    it("should marshal yaml", () => {
    //    const result = policy.evaluate(
    //                [{ foo: [1, 2, 3] }],
    //        "yaml/support/canMarshalYAML",
    //    );
    //        expect(result.length).toBe(1);
    //        expect(result[0]).toMatchObject({ result: [[{ foo: [1, 2, 3] }]] });
    //    });
    //
    //    it("should validate yaml", () => {
    //    const result = policy.evaluate({}, "yaml/support/isValidYAML");
    //        expect(result.length).toBe(1);
    //        expect(result[0]).toMatchObject({ result: true });
    //    });
}
