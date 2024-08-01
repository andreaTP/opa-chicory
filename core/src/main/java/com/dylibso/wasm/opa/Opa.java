package com.dylibso.wasm.opa;

import com.dylibso.chicory.runtime.Memory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

// final user API
// directly porting:
// https://github.com/open-policy-agent/npm-opa-wasm/blob/main/README.md
public class Opa {

    public static class OpaPolicy {
        private final OpaWasm wasm;
        private final ObjectMapper mapper;
        private int baseHeapPtr = -1;
        private int dataHeapPtr = -1;
        private int dataAddr = -1;
        private int inputAddr = -1;
        private int resultAddr = -1;
        private int entrypoint;

        private OpaPolicy(OpaWasm wasm) {
            mapper = new ObjectMapper();
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

            // map the builtins
            try {
                var mappings = new HashMap<String, Integer>();
                var fields = mapper.readTree(dumpJson(wasm.builtins())).fields();
                while (fields.hasNext()) {
                    var field = fields.next();
                    mappings.put(field.getKey(), field.getValue().intValue());
                }
                wasm.imports().initializeBuiltins(mappings);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        public OpaPolicy entrypoint(int entrypoint) {
            this.entrypoint = entrypoint;
            return this;
        }

        public OpaPolicy entrypoint(String entrypoint) {
            this.entrypoint = findEntrypoint(entrypoint);
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
        public OpaPolicy data(String data) {
            wasm.opaHeapPtrSet(this.baseHeapPtr);
            this.dataAddr = loadJson(data);
            this.dataHeapPtr = wasm.opaHeapPtrGet();
            return this;
        }

        public OpaPolicy input(String input) {
            var inputLen = input.getBytes().length;
            var delta = this.dataHeapPtr + inputLen - (wasm.memory().pages() * Memory.PAGE_SIZE);
            if (delta > 0) {
                // TODO: similar logic might go into Chicory itself?
                var pageSize = (int) Math.ceil(delta / Memory.PAGE_SIZE);
                var grown = wasm.memory().grow(pageSize);
                if (grown == -1) {
                    throw new RuntimeException("Maximum memory size exceeded");
                }
            }

            this.inputAddr = loadJson(input);
            return this;
        }

        public int findEntrypoint(String name) {
            var x = dumpJson(wasm.entrypoints());
            try {
                var json = dumpJson(wasm.entrypoints());
                // So far, this is the only place we actually use Jackson, let's review if it's
                // really needed at the end
                var entrypoints = mapper.readTree(json);
                if (!entrypoints.has(name)) {
                    throw new IllegalArgumentException(
                            "entrypoint " + name + " is not valid in this instance");
                }
                return entrypoints.findValue(name).asInt();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(
                        "Failed to parse the response from \"entrypoints()\"", e);
            }
        }

        public String evaluate() {
            var ctxAddr = wasm.opaEvalCtxNew();
            if (this.dataAddr == -1) {
                data("{}");
            }
            wasm.opaEvalCtxSetData(ctxAddr, this.dataAddr);
            if (this.inputAddr == -1) {
                input("{}");
            }
            wasm.opaEvalCtxSetInput(ctxAddr, this.inputAddr);
            wasm.opaEvalCtxSetEntrypoint(ctxAddr, this.entrypoint);

            var evalResult = wasm.eval(ctxAddr);
            if (evalResult != OpaErrorCode.OPA_ERR_OK) {
                throw new RuntimeException(
                        "Error evaluating the Opa Policy, returned code is: " + evalResult);
            }

            this.resultAddr = wasm.opaEvalCtxGetResult(ctxAddr);
            var result = dumpJson(resultAddr);
            return result;
        }

        public String evaluate(String input) {
            input(input);
            return evaluate();
        }

        private void free(int addr) {
            if (addr != -1) {
                wasm.opaValueFree(addr);
            }
        }
    }

    public static OpaPolicy loadPolicy(InputStream input) {
        return loadPolicy(input, new OpaDefaultImports());
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

    public static OpaPolicy loadPolicy(InputStream input, OpaImports imports) {
        return new OpaPolicy(new OpaWasm(imports, input));
    }

    public static OpaPolicy loadPolicy(ByteBuffer buffer, OpaImports imports) {
        return loadPolicy(buffer.array(), imports);
    }

    public static OpaPolicy loadPolicy(byte[] buffer, OpaImports imports) {
        return loadPolicy(new ByteArrayInputStream(buffer), imports);
    }

    public static OpaPolicy loadPolicy(Path path, OpaImports imports) {
        return loadPolicy(path.toFile(), imports);
    }

    public static OpaPolicy loadPolicy(File file, OpaImports imports) {
        try {
            return loadPolicy(new FileInputStream(file), imports);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("File not found at path: " + file.getPath(), e);
        }
    }
}
