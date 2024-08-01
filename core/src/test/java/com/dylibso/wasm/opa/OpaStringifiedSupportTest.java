package com.dylibso.wasm.opa;

import static com.dylibso.wasm.opa.Utils.getResult;
import static com.dylibso.wasm.opa.Utils.jsonPrettyPrint;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class OpaStringifiedSupportTest {
    static Path wasmFile;
    static String data;

    @BeforeAll
    public static void beforeAll() throws Exception {
        var targetFolder =
                OpaCli.compile(
                        "stringified-support",
                        "stringified/support",
                        "stringified/support/plainInputBoolean",
                        "stringified/support/plainInputNumber",
                        "stringified/support/plainInputString");
        wasmFile = targetFolder.resolve("policy.wasm");
        data =
                new String(
                        Files.readAllBytes(
                                OpaCli.baseSourceFolder
                                        .resolve("stringified-support")
                                        .resolve("stringified-support-data.json")));
    }

    @Test
    public void shouldAcceptStringifiedData() {
        var policy = Opa.loadPolicy(wasmFile);
        policy.data(data);

        var positiveResult = getResult(policy.evaluate("{ \"secret\" : \"secret\" }"));
        assertTrue(positiveResult.findValue("hasPermission").asBoolean());

        var negativeResult = getResult(policy.evaluate("{ \"secret\" : \"wrong\" }"));
        assertFalse(negativeResult.findValue("hasPermission").asBoolean());
    }

    @Test
    public void shouldAcceptStringifiedInputObject() {
        var policy = Opa.loadPolicy(wasmFile);
        policy.data(data);

        var positiveResult =
                getResult(
                        policy.evaluate(
                                jsonPrettyPrint(
                                        "{ \"permissions\" : [\"view:account-billing\"] }")));
        assertTrue(positiveResult.findValue("hasPermission").asBoolean());

        var negativeResult =
                getResult(policy.evaluate(jsonPrettyPrint("{ \"secret\" : \"wrong\" }")));
        assertFalse(negativeResult.findValue("hasPermission").asBoolean());
    }

    @Test
    public void shouldAcceptStringifiedInputPlainBoolean() {
        var policy = Opa.loadPolicy(wasmFile);
        policy.entrypoint("stringified/support/plainInputBoolean");

        var positiveResult = getResult(policy.evaluate(jsonPrettyPrint("true")));
        assertTrue(positiveResult.asBoolean());

        var negativeResult = getResult(policy.evaluate(jsonPrettyPrint("false")));
        assertFalse(negativeResult.asBoolean());
    }

    @Test
    public void shouldAcceptStringifiedInputPlainNumber() {
        var policy = Opa.loadPolicy(wasmFile);
        policy.entrypoint("stringified/support/plainInputNumber");

        var positiveResult = getResult(policy.evaluate(jsonPrettyPrint("5")));
        assertTrue(positiveResult.asBoolean());

        var negativeResult = getResult(policy.evaluate(jsonPrettyPrint("6")));
        assertFalse(negativeResult.asBoolean());
    }

    @Test
    public void shouldAcceptStringifiedInputPlainString() {
        var policy = Opa.loadPolicy(wasmFile);
        policy.entrypoint("stringified/support/plainInputString");

        var positiveResult = getResult(policy.evaluate(jsonPrettyPrint("\"test\"")));
        assertTrue(positiveResult.asBoolean());

        var negativeResult = getResult(policy.evaluate(jsonPrettyPrint("\"invalid\"")));
        assertFalse(negativeResult.asBoolean());
    }
}
