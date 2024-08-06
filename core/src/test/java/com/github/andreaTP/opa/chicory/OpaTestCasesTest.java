package com.github.andreaTP.opa.chicory;

import static com.github.andreaTP.opa.chicory.OpaCli.baseDestFolder;
import static com.github.andreaTP.opa.chicory.OpaCli.testcasesDestFolder;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dylibso.chicory.runtime.exceptions.WASMMachineException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

// TODO: implement me
// this should iterate over "testcases"
// testcases.tar.gz
// @Disabled
public class OpaTestCasesTest {
    static ObjectMapper mapper = new ObjectMapper();

    static {
        // for reliable tests results
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    public static class Case {
        @JsonProperty("note")
        public String note;

        @JsonProperty("query")
        public String query;

        @JsonProperty("input")
        public JsonNode input;

        @JsonProperty("modules")
        public JsonNode modules;

        @JsonProperty("want_defined")
        public boolean wantDefined;

        @JsonProperty("want_error")
        public String wantError;

        @JsonProperty("data")
        public JsonNode data;

        @JsonProperty("want_result")
        public JsonNode[] wantResult;

        @JsonProperty("wasm")
        public String wasm;

        Case() {}

        @Override
        public String toString() {
            return "Case{"
                    + "note='"
                    + note
                    + '\''
                    + ", query='"
                    + query
                    + '\''
                    + ", input="
                    + input
                    + ", modules="
                    + modules
                    + ", wantDefined="
                    + wantDefined
                    + ", wantError='"
                    + wantError
                    + '\''
                    + ", data="
                    + data
                    + ", wantResult="
                    + Arrays.toString(wantResult)
                    + ", wasm='"
                    + wasm
                    + '\''
                    + '}';
        }
    }

    @JsonIgnoreProperties
    public static class Testcases {
        @JsonProperty("cases")
        public Case[] cases;

        Testcases() {}
    }

    public static class TestCaseData {
        private final Path policy;
        private final Case caze;

        TestCaseData(Path policy, Case c) {
            this.caze = c;
            this.policy = policy;
        }

        public Case getCase() {
            return caze;
        }

        public Path getPolicy() {
            return policy;
        }

        @Override
        public String toString() {
            return "TestCaseData{" + "policy=" + policy + ", caze=" + caze + '}';
        }
    }

    @BeforeAll
    public static void beforeAll() throws Exception {
        OpaCli.prepareTestcases();
    }

    private static Stream<Arguments> walkTestcasesFolder() throws IOException {
        var allTests = new ArrayList<TestCaseData>();
        var files = testcasesDestFolder.toFile().list((ignored, name) -> name.endsWith(".json"));

        for (var file : files) {
            var testName = file.split("\\.")[0];
            var testcases =
                    mapper.readValue(testcasesDestFolder.resolve(file).toFile(), Testcases.class);

            for (int i = 0; i < testcases.cases.length; i++) {
                var testcaseDestFolderName = testName + "_" + i;
                var testcaseDestFolder = baseDestFolder.resolve(testcaseDestFolderName);
                testcaseDestFolder.toFile().mkdirs();

                var policy = testcaseDestFolder.resolve("policy.wasm");
                Files.write(policy, Base64.getDecoder().decode(testcases.cases[i].wasm.getBytes()));
                Files.copy(
                        Path.of("src", "test", "resources", "capabilities.json"),
                        testcaseDestFolder.resolve("capabilities.json"),
                        REPLACE_EXISTING);

                allTests.add(new TestCaseData(policy, testcases.cases[i]));
            }
        }
        return allTests.stream().map(f -> Arguments.of(f));
    }

    @ParameterizedTest
    @MethodSource("walkTestcasesFolder")
    void externalTestcases(TestCaseData data) throws Exception {
        var policy = Opa.loadPolicy(data.getPolicy());

        // verify
        assertEquals(1, policy.entrypoints().size());

        policy.data(data.getCase().data);

        if (data.getCase().wantError != null) {
            // NOTE TO SELF: I think that this is the expected behavior, we should not catch user
            // defined Exceptions:
            // var exception = assertThrows(OpaAbortException.class, () ->
            // policy.evaluate(data.getCase().input));
            // assertTrue(exception.getMessage().contains(data.getCase().wantError), "The exception
            // with message: " + exception.getMessage() + " doesn't contain the expected text: " +
            // data.getCase().wantError);

            var wasmException =
                    assertThrows(
                            WASMMachineException.class,
                            () -> policy.evaluate(data.getCase().input));
            var exception = wasmException.getCause().getCause();
            assertTrue(wasmException.getCause().getCause() instanceof OpaAbortException);
            assertTrue(
                    exception.getMessage().contains(data.getCase().wantError),
                    "The exception with message: "
                            + exception.getMessage()
                            + " doesn't contain the expected text: "
                            + data.getCase().wantError);
        } else if (data.getCase().wantResult != null && data.getCase().wantResult.length > 0) {
            var expected = data.getCase().wantResult;
            var sortedExpected =
                    mapper.writeValueAsString(mapper.treeToValue(expected[0], Object.class));

            var result = policy.evaluate(data.getCase().input);

            var firstResult =
                    mapper.writeValueAsString(
                            mapper.treeToValue(
                                    mapper.readTree(result).elements().next(), Object.class));
            assertEquals(sortedExpected, firstResult);
        } else if (data.getCase().wantDefined) {
            var result = policy.evaluate(data.getCase().input);

            assertFalse(mapper.readTree(result).isNull());
        } else {
            var result = policy.evaluate(data.getCase().input);
            // TODO: is this the expected output in this case?
            assertEquals("[]", result);
        }
    }
}
