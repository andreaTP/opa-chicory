package com.dylibso.wasm.opa;

import static com.dylibso.wasm.opa.Utils.rootCauseMessage;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dylibso.chicory.runtime.exceptions.WASMMachineException;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MemoryTest {
    static Path wasmFile;

    @BeforeAll
    public static void beforeAll() throws Exception {
        wasmFile = OpaCli.compile("memory", "test/allow").resolve("policy.wasm");
    }

    @Test
    public void inputExceedsMemoryHostFailsToGrowIt() {
        var policy = Opa.loadPolicy(wasmFile, new OpaDefaultImports(2, 2));
        var input = new String(new char[2 * 65536]).replace("\0", "a");
        var exception = assertThrows(RuntimeException.class, () -> policy.evaluate(input));
        assertEquals("Maximum memory size exceeded", exception.getMessage());
    }

    @Test
    public void parsingInputExceedsMemory() {
        var policy = Opa.loadPolicy(wasmFile, new OpaDefaultImports(3, 4));
        var input = new String(new char[2 * 65536]).replace("\0", "a");
        var exception = assertThrows(WASMMachineException.class, () -> policy.input(input));
        assertEquals("opa_abort - opa_malloc: failed", rootCauseMessage(exception));
    }

    @Test
    public void largeInputHostAndGuestGrowSuccessfully() {
        var policy = Opa.loadPolicy(wasmFile, new OpaDefaultImports(2, 8));
        var input = new String(new char[2 * 65536]).replace("\0", "a");
        assertDoesNotThrow(() -> policy.evaluate(input));
    }

    @Test
    public void doesNotLeakMemoryEvaluatingTheSamePolicyMultipleTimes() {
        var policy = Opa.loadPolicy(wasmFile, new OpaDefaultImports(2, 8));
        var input = new String(new char[2 * 65536]).replace("\0", "a");
        for (int i = 0; i < 16; i++) {
            assertDoesNotThrow(() -> policy.evaluate(input));
        }
    }
}
