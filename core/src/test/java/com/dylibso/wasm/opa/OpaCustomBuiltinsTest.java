package com.dylibso.wasm.opa;

import static com.dylibso.wasm.opa.Utils.getResult;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class OpaCustomBuiltinsTest {
    static ObjectMapper jsonMapper = new ObjectMapper();
    static Opa.OpaPolicy policy;

    //          "custom.zeroArgBuiltin": () => `hello`,
    //            "custom.oneArgBuiltin": (arg0) => `hello ${arg0}`,
    //            "custom.twoArgBuiltin": (arg0, arg1) => `hello ${arg0}, ${arg1}`,
    //            "custom.threeArgBuiltin": (arg0, arg1, arg2) => (
    //            `hello ${arg0}, ${arg1}, ${arg2}`
    //            ),
    //            "custom.fourArgBuiltin": (arg0, arg1, arg2, arg3) => (
    //            `hello ${arg0}, ${arg1}, ${arg2}, ${arg3}`
    //            ),
    //            "json.is_valid": () => {
    //        throw new Error("should never happen");
    //    },

    static OpaBuiltin.Builtin[] customBuiltins =
            new OpaBuiltin.Builtin[] {
                OpaBuiltin.from("custom.zeroArgBuiltin", () -> TextNode.valueOf("hello")),
                OpaBuiltin.from(
                        "custom.oneArgBuiltin",
                        (arg0) -> TextNode.valueOf("hello " + arg0.asText())),
                OpaBuiltin.from(
                        "custom.twoArgBuiltin",
                        (arg0, arg1) ->
                                TextNode.valueOf("hello " + arg0.asText() + ", " + arg1.asText())),
                OpaBuiltin.from(
                        "custom.threeArgBuiltin",
                        (arg0, arg1, arg2) ->
                                TextNode.valueOf(
                                        "hello "
                                                + arg0.asText()
                                                + ", "
                                                + arg1.asText()
                                                + ", "
                                                + arg2.asText())),
                OpaBuiltin.from(
                        "custom.fourArgBuiltin",
                        (arg0, arg1, arg2, arg3) ->
                                TextNode.valueOf(
                                        "hello "
                                                + arg0.asText()
                                                + ", "
                                                + arg1.asText()
                                                + ", "
                                                + arg2.asText()
                                                + ", "
                                                + arg3.asText()))
            };

    @BeforeAll
    public static void beforeAll() throws Exception {
        policy =
                Opa.loadPolicy(
                        OpaCli.compile(
                                        "custom-builtins",
                                        true,
                                        "custom_builtins/zero_arg",
                                        "custom_builtins/one_arg",
                                        "custom_builtins/two_arg",
                                        "custom_builtins/three_arg",
                                        "custom_builtins/four_arg")
                                .resolve("policy.wasm"),
                        new OpaDefaultImports(customBuiltins));
    }

    @Test
    public void shouldCallZeroArgBuiltin() {
        var result = getResult(policy.entrypoint("custom_builtins/zero_arg").evaluate());
        assertEquals("hello", result.asText());
    }

    @Test
    public void shouldCallACustomOneArgBuiltin() {
        var result =
                getResult(
                        policy.entrypoint("custom_builtins/one_arg")
                                .evaluate("{ \"args\": [\"arg0\"] }"));
        assertEquals("hello arg0", result.asText());
    }

    @Test
    public void shouldCallACustomTwoArgBuiltin() {
        var result =
                getResult(
                        policy.entrypoint("custom_builtins/two_arg")
                                .evaluate("{ \"args\": [\"arg0\", \"arg1\"] }"));
        assertEquals("hello arg0, arg1", result.asText());
    }

    @Test
    public void shouldCallACustomThreeArgBuiltin() {
        var result =
                getResult(
                        policy.entrypoint("custom_builtins/three_arg")
                                .evaluate("{ \"args\": [\"arg0\", \"arg1\", \"arg2\"] }"));
        assertEquals("hello arg0, arg1, arg2", result.asText());
    }

    @Test
    public void shouldCallACustomFourArgBuiltin() {
        var result =
                getResult(
                        policy.entrypoint("custom_builtins/four_arg")
                                .evaluate(
                                        "{ \"args\": [\"arg0\", \"arg1\", \"arg2\", \"arg3\"] }"));
        assertEquals("hello arg0, arg1, arg2, arg3", result.asText());
    }
    //
    //    it("should call a custom three-arg builtin", () => {
    //    const result = policy.evaluate(
    //                { args: ["arg0", "arg1", "arg2"] },
    //        "custom_builtins/three_arg",
    //    );
    //        expect(result.length).not.toBe(0);
    //        expect(result[0]).toMatchObject({
    //                result: "hello arg0, arg1, arg2",
    //    });
    //    });
    //
    //    it("should call a custom four-arg builtin", () => {
    //    const result = policy.evaluate(
    //                { args: ["arg0", "arg1", "arg2", "arg3"] },
    //        "custom_builtins/four_arg",
    //    );
    //        expect(result.length).not.toBe(0);
    //        expect(result[0]).toMatchObject({
    //                result: "hello arg0, arg1, arg2, arg3",
    //    });
    //    });
    //
    //    it("should call a provided builtin over a custom builtin", () => {
    //    const result = policy.evaluate({}, "custom_builtins/valid_json");
    //        expect(result.length).not.toBe(0);
    //        expect(result[0]).toMatchObject({ result: true });
    //    });
}
