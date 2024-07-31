package com.dylibso.wasm.opa;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;

// final user API
// directly porting:
// https://github.com/open-policy-agent/npm-opa-wasm/blob/main/README.md
public class Opa {

    public static class OpaPolicy implements AutoCloseable {
        private final OpaWasm wasm;
        private int baseHeapPtr = -1;
        private int dataHeapPtr = -1;
        private int dataAddr = -1;
        private int inputAddr = -1;
        private int resultAddr = -1;
        private int entrypoint;

        private OpaPolicy(OpaWasm wasm) {
            this.wasm = wasm;

            if (!(wasm.opaWasmAbiVersion() == 1 && wasm.opaWasmAbiMinorVersion() == 3)) {
                throw new IllegalArgumentException(
                        "Invalid version, supported 1.3, detected "
                                + wasm.opaWasmAbiVersion()
                                + "."
                                + wasm.opaWasmAbiMinorVersion());
            }

            this.baseHeapPtr = wasm.opaHeapPtrGet();
            this.dataHeapPtr = this.baseHeapPtr;
            this.dataAddr = -1;
            wasm.opaHeapPtrSet(this.dataHeapPtr);
        }

        public OpaPolicy setEntrypoint(int entrypoint) {
            this.entrypoint = entrypoint;
            return this;
        }

        private int loadJson(String data) {
            var dataStrAddr = wasm.opaMalloc(data.length());
            wasm.memory().writeCString(dataStrAddr, data);
            var dataAddr = wasm.opaJsonParse(dataStrAddr, data.length());
            wasm.opaFree(dataStrAddr);
            return dataAddr;
        }

        private String dumpJson(int addr) {
            int resultStrAddr = wasm.opaJsonDump(addr);
            var resultStr = wasm.memory().readCString(resultStrAddr);
            wasm.opaFree(resultStrAddr);
            return resultStr;
        }

        // data MUST be a serializable object or ArrayBuffer, which assumed to be a well-formed
        // stringified JSON
        public OpaPolicy setData(String data) {
            wasm.opaHeapPtrSet(this.baseHeapPtr);
            this.dataAddr = loadJson(data);
            this.dataHeapPtr = wasm.opaHeapPtrGet();
            return this;
        }

        public OpaPolicy setInput(String input) {
            this.inputAddr = loadJson(input);
            return this;
        }

        public String evaluate() {
            try {
                var ctxAddr = wasm.opaEvalCtxNew();
                wasm.opaEvalCtxSetInput(ctxAddr, this.inputAddr);
                wasm.opaEvalCtxSetData(ctxAddr, this.dataAddr);
                wasm.opaEvalCtxSetEntrypoint(ctxAddr, this.entrypoint);

                var evalResult = wasm.eval(ctxAddr);
                if (evalResult != OpaErrorCode.OPA_ERR_OK) {
                    throw new RuntimeException(
                            "Error evaluating the Opa Policy, returned code is: " + evalResult);
                }

                this.resultAddr = wasm.opaEvalCtxGetResult(ctxAddr);
                var result = dumpJson(resultAddr);
                return result;
            } finally {
                close();
            }
        }

        public String evaluate(String input) {
            setInput(input);
            return evaluate();
        }

        private void free(int addr) {
            if (addr != -1) {
                wasm.opaValueFree(addr);
            }
        }

        @Override
        public void close() {
            free(inputAddr);
            free(dataAddr);
            free(resultAddr);
        }
    }

    public static OpaPolicy loadPolicy(InputStream input) {
        return new OpaPolicy(new OpaWasm(new OpaDefaultImports(), input));
    }

    public static OpaPolicy loadPolicy(ByteBuffer buffer) {
        return loadPolicy(buffer.array());
    }

    public static OpaPolicy loadPolicy(byte[] buffer) {
        return loadPolicy(new ByteArrayInputStream(buffer));
    }

    public static OpaPolicy loadPolicy(Path path) {
        return loadPolicy(path.toFile());
    }

    public static OpaPolicy loadPolicy(File file) {
        try {
            return loadPolicy(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("File not found at path: " + file.getPath(), e);
        }
    }
}
