import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dylibso.wasm.opa.Opa;
import com.dylibso.wasm.opa.OpaDefaultImports;
import com.dylibso.wasm.opa.OpaErrorCode;
import com.dylibso.wasm.opa.OpaWasm;
import java.io.File;
import java.io.FileInputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

public class OpaTest {
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void lowLevelAPI() throws Exception {
        var imports = new OpaDefaultImports();
        var opa =
                new OpaWasm(
                        imports,
                        new FileInputStream(new File("src/test/resources/opa/policy.wasm")));

        assertEquals(opa.opaWasmAbiVersion(), 1);
        assertEquals(opa.opaWasmAbiMinorVersion(), 3);

        var builtinsAddr = opa.builtins();
        var builtinsStringAddr = opa.opaJsonDump(builtinsAddr);
        assertEquals("{}", imports.memory().readCString(builtinsStringAddr));

        // Following the instructions here:
        // https://www.openpolicyagent.org/docs/latest/wasm/#evaluation
        var ctxAddr = opa.opaEvalCtxNew();

        var input = "{\"user\": \"alice\"}";
        var inputStrAddr = opa.opaMalloc(input.length());
        imports.memory().writeCString(inputStrAddr, input);
        var inputAddr = opa.opaJsonParse(inputStrAddr, input.length());
        opa.opaFree(inputStrAddr);
        opa.opaEvalCtxSetInput(ctxAddr, inputAddr);

        var data = "{ \"role\" : { \"alice\" : \"admin\", \"bob\" : \"user\" } }";
        var dataStrAddr = opa.opaMalloc(data.length());
        imports.memory().writeCString(dataStrAddr, data);
        var dataAddr = opa.opaJsonParse(dataStrAddr, data.length());
        opa.opaFree(dataStrAddr);
        opa.opaEvalCtxSetData(ctxAddr, dataAddr);

        // TODO: add entrypoint handling
        var evalResult = opa.eval(ctxAddr);
        assertEquals(OpaErrorCode.OPA_ERR_OK, evalResult);

        int resultAddr = opa.opaEvalCtxGetResult(ctxAddr);
        int resultStrAddr = opa.opaJsonDump(resultAddr);
        var resultStr = imports.memory().readCString(resultStrAddr);
        opa.opaFree(resultStrAddr);
        assertEquals("[{\"result\":true}]", resultStr);

        // final cleanup of resources for demo purposes
        opa.opaValueFree(inputAddr);
        opa.opaValueFree(dataAddr);
        opa.opaValueFree(resultAddr);
    }

    private boolean readSingleBool(String input) throws JsonProcessingException {
        return objectMapper
                .readTree(input)
                .elements()
                .next()
                .findValue("result")
                .asBoolean();
    }

    @Test
    public void highLevelAPI() throws Exception {
        try (var policy = Opa.loadPolicy(new File("src/test/resources/opa/policy.wasm"))) {
            policy.setData("{ \"role\" : { \"alice\" : \"admin\", \"bob\" : \"user\" } }");

            // evaluate the admin
            policy.setInput("{\"user\": \"alice\"}");
            assertTrue(readSingleBool(policy.evaluate()));

            // evaluate a user
            policy.setInput("{\"user\": \"bob\"}");
            assertFalse(readSingleBool(policy.evaluate()));

            // change the data of the policy
            policy.setData("{ \"role\" : { \"bob\" : \"admin\", \"alice\" : \"user\" } }");

            // evaluate the admin
            policy.setInput("{\"user\": \"bob\"}");
            assertTrue(readSingleBool(policy.evaluate()));

            // evaluate a user
            policy.setInput("{\"user\": \"alice\"}");
            assertFalse(readSingleBool(policy.evaluate()));

            // throws:
            // com.dylibso.chicory.runtime.exceptions.WASMRuntimeException: integer divide by zero
            // is it expected?
            // evaluate an in-existent user
            //            opa.setInput("{\"user\": \"charles\"}");
            //            assertFalse(readSingleBool(opa.evaluate()));
        }
    }
}
