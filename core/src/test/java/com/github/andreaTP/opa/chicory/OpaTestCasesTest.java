package com.github.andreaTP.opa.chicory;

import static com.github.andreaTP.opa.chicory.OpaCli.testcasesDestFolder;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

// TODO: implement me
// this should iterate over "testcases"
// testcases.tar.gz
@Disabled
public class OpaTestCasesTest {
    static ObjectMapper mapper = new ObjectMapper();

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
        public JsonNode wantError;

        @JsonProperty("data")
        public JsonNode data;

        @JsonProperty("want_result")
        public JsonNode[] wantResult;

        @JsonProperty("wasm")
        public String wasm;

        Case() {}
    }

    @JsonIgnoreProperties
    public static class Testcases {
        @JsonProperty("cases")
        public Case[] cases;

        Testcases() {}
    }

    @BeforeAll
    public static void beforeAll() throws Exception {
        OpaCli.prepareTestcases();
    }

    private static Stream<Arguments> walkTestcasesFolder() throws IOException {
        return Arrays.stream(
                        testcasesDestFolder
                                .toFile()
                                .list((ignored, name) -> name.endsWith(".json")))
                .map(f -> Arguments.of(testcasesDestFolder.resolve(f)));
    }

    @ParameterizedTest
    @MethodSource("walkTestcasesFolder")
    void externalTestcases(Path target) throws Exception {
        System.out.println("processing " + target.toFile().getAbsolutePath());
        var testName = target.toFile().getName().split("\\.")[0];

        System.out.println("testname " + testName);
        var testcases = mapper.readValue(target.toFile(), Testcases.class);

        for (var testcase : testcases.cases) {
            System.out.println("note " + testcase.note);

            // is it the entire bundle???
            // testcase.wasm.getBytes()

            // policy.evaluate();

            // Go on from here!
        }

        assertEquals(true, true);
    }
}
