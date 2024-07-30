import com.dylibso.wasm.opa.OpaDefaultImports;
import com.dylibso.wasm.opa.OpaErrorCode;
import com.dylibso.wasm.opa.OpaWasm;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OpaTest {

    @Test
    public void lowLevelAPI() {
        var imports = new OpaDefaultImports();
        var opa = new OpaWasm(imports, new File("src/test/resources/opa/policy.wasm"));

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
}
