import com.dylibso.wasm.opa.Opa;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MultipleEntrypointsTest {

    @Test
    public void multipleEntrypointTest() throws Exception {
        var wasmFile = OpaCli.compile("multiple-entrypoints", "example", "example/one", "example/two").resolve("policy.wasm");

        try (var policy = Opa.loadPolicy(wasmFile)) {
            var result = policy.evaluate();
            System.out.println("DEBUG: " + result);
        }

    }
}
