package com.dylibso.wasm.opa;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

// TODO: implement me
// this should iterate over "testcases"
// testcases.tar.gz
public class OpaTestCasesTest {

    private static Stream<Arguments> walkTestcasesFolder() throws IOException {
        return Stream.of(Arguments.of(Path.of(".")));
        // return Files.list(new File("testcases")).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("walkTestcasesFolder")
    void externalTestcases(Path target) {
        assertEquals(true, true);
    }
}
