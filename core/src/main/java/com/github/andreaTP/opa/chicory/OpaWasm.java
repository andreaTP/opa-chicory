package com.github.andreaTP.opa.chicory;

import com.dylibso.chicory.runtime.*;
import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.types.Value;
import com.dylibso.chicory.wasm.types.ValueType;
import java.io.InputStream;
import java.util.List;

// Low level bindings to OPA
// The resulting code is the target to be generated by chicory-bindgen
public class OpaWasm {
    private final OpaImports imports;
    private final Instance instance;

    public OpaWasm(OpaImports imports, InputStream is) {
        this.imports = imports;
        // Imports
        HostMemory memory = new HostMemory("env", "memory", imports.memory());
        OpaImportsAdapter adapter = new OpaImportsAdapter(imports, this);
        HostModuleInstance hostModuleInstance = adapter.toHostModuleInstance("env");

        var m = Parser.parse(is);
        instance =
                Instance.builder(m)
                        .withHostImports(
                                HostImports.builder()
                                        .addMemory(memory)
                                        .withFunctions(List.of(hostModuleInstance.hostFunctions()))
                                        .build())
                        .build()
                        .initialize(true);
    }


    public OpaImports imports() {
        return imports;
    }

    public Memory memory() {
        return instance.memory();
    }

    // exports
    public int opaWasmAbiVersion() {
        return instance.export("opa_wasm_abi_version").apply()[0].asInt();
    }

    public int opaWasmAbiMinorVersion() {
        return instance.export("opa_wasm_abi_minor_version").apply()[0].asInt();
    }

    // Exports
    /*
     * Evaluates the loaded policy with the provided evaluation context. The return value is reserved for future use.
     */
    public OpaErrorCode eval(int ctxAddr) {
        return OpaErrorCode.fromValue(instance.export("eval").apply(Value.i32(ctxAddr))[0].asInt());
    }

    /*
     * Returns the address of a mapping of built-in function names to numeric identifiers that are required by the policy.
     */
    public int builtins() {
        return instance.export("builtins").apply()[0].asInt();
    }

    /*
     * Returns the address of a mapping of entrypoints to numeric identifiers that can be selected when evaluating the policy.
     */
    public int entrypoints() {
        return instance.export("entrypoints").apply()[0].asInt();
    }

    /*
     * Returns the address of a newly allocated evaluation context.
     */
    public int opaEvalCtxNew() {
        return instance.export("opa_eval_ctx_new").apply()[0].asInt();
    }

    /*
     * Set the input value to use during evaluation. This must be called before each eval() call. If the input value is not set before evaluation, references to the input document result produce no results (i.e., they are undefined.)
     */
    public void opaEvalCtxSetInput(int ctxAddr, int valueAddr) {
        instance.export("opa_eval_ctx_set_input").apply(Value.i32(ctxAddr), Value.i32(valueAddr));
    }

    /*
     * Set the data value to use during evaluation. This should be called before each eval() call. If the data value is not set before evaluation, references to base data documents produce no results (i.e., they are undefined.)
     */
    public void opaEvalCtxSetData(int ctxAddr, int valueAddr) {
        instance.export("opa_eval_ctx_set_data").apply(Value.i32(ctxAddr), Value.i32(valueAddr));
    }

    /*
     * Set the data value to use during evaluation. This should be called before each eval() call. If the data value is not set before evaluation, references to base data documents produce no results (i.e., they are undefined.)
     */
    public void opaEvalCtxSetEntrypoint(int ctxAddr, int entrypointId) {
        instance.export("opa_eval_ctx_set_entrypoint")
                .apply(Value.i32(ctxAddr), Value.i32(entrypointId));
    }

    /*
     * Get the result set produced by the evaluation process.
     */
    public int opaEvalCtxGetResult(int ctxAddr) {
        return instance.export("opa_eval_ctx_get_result").apply(Value.i32(ctxAddr))[0].asInt();
    }

    /*
     * Allocates size bytes in the shared memory and returns the starting address.
     */
    public int opaMalloc(int capacity) {
        return instance.export("opa_malloc").apply(Value.i32(capacity))[0].asInt();
    }

    /*
     * Free a pointer. Calls opa_abort on error.
     */
    public void opaFree(int addr) {
        instance.export("opa_free").apply(Value.i32(addr));
    }

    /*
     * Parses the JSON serialized value starting at str_addr of size bytes and returns the address of the parsed value. The parsed value may refer to a null, boolean, number, string, array, or object value.
     */
    public int opaJsonParse(int addr, int size) {
        return instance.export("opa_json_parse").apply(Value.i32(addr), Value.i32(size))[0].asInt();
    }

    /*
     * The same as opa_json_parse except Rego set literals are supported.
     */
    public int opaValueParse(int addr, int size) {
        return instance.export("opa_value_parse")
                .apply(Value.i32(addr), Value.i32(size))[0]
                .asInt();
    }

    /*
     * Dumps the value referred to by value_addr to a null-terminated JSON serialized string and returns the address of the start of the string. Rego sets are serialized as JSON arrays. Non-string Rego object keys are serialized as strings.
     */
    public int opaJsonDump(int addr) {
        return instance.export("opa_json_dump").apply(Value.i32(addr))[0].asInt();
    }

    /*
     * The same as opa_json_dump except Rego sets are serialized using the literal syntax and non-string Rego object keys are not serialized as strings.
     */
    public int opaValueDump(int addr) {
        return instance.export("opa_value_dump").apply(Value.i32(addr))[0].asInt();
    }

    /*
     * Set the heap pointer for the next evaluation.
     */
    public void opaHeapPtrSet(int addr) {
        instance.export("opa_heap_ptr_set").apply(Value.i32(addr));
    }

    /*
     * Set the heap pointer for the next evaluation.
     */
    public int opaHeapPtrGet() {
        return instance.export("opa_heap_ptr_get").apply()[0].asInt();
    }

    /*
     * Add the value at the value_addr into the object referenced by base_value_addr at the given path. The path_value_addr must point to an array value with string keys (eg: ["a", "b", "c"]). Existing values will be updated. On success the value at value_addr is no longer owned by the caller, it will be freed with the base value. The path value must be freed by the caller after use by calling opa_value_free. (The original path string passed to opa_json_parse or opa_value_parse to create the value must be freed by calling opa_free.) If an error occurs the base value will remain unchanged. Example: base object {"a": {"b": 123}}, path ["a", "x", "y"], and value {"foo": "bar"} will yield {"a": {"b": 123, "x": {"y": {"foo": "bar"}}}}. Returns an error code (see below).
     */
    public OpaErrorCode opaValueAddPath(int baseValueAddr, int pathValueAddr, int valueAddr) {
        return OpaErrorCode.fromValue(
                instance.export("opa_value_add_path")
                        .apply(
                                Value.i32(baseValueAddr),
                                Value.i32(pathValueAddr),
                                Value.i32(valueAddr))[0]
                        .asInt());
    }

    /*
     * Remove the value from the object referenced by base_value_addr at the given path. Values removed will be freed. The path value must be freed by the caller after use by calling opa_value_free. (The original path string parsed by opa_json_parse or opa_value_parse must be released using opa_free.) The path_value_addr must point to an array value with string keys (eg: ["a", "b", "c"]). Returns an error code (see below).
     */
    public OpaErrorCode opaValueRemovePath(int baseValueAddr, int pathValueAddr) {
        return OpaErrorCode.fromValue(
                instance.export("opa_value_remove_path")
                        .apply(Value.i32(baseValueAddr), Value.i32(pathValueAddr))[0]
                        .asInt());
    }

    /*
     * Free a value such as one generated by opa_value_parse or opa_json_parse reference at value_addr
     */
    public void opaValueFree(int addr) {
        instance.export("opa_value_free").apply(Value.i32(addr));
    }

    /*
     * Stash free heap blocks in a shadow heap to enable eval or opa_eval to allocate only blocks that it can subsequently free with a call to opa_heap_ptr_set. The caller should subsequently call opa_heap_ptr_get and store the value to save before calling opa_heap_bloks_restore
     */
    public void opaHeapBlocksStash() {
        instance.export("opa_heap_blocks_stash").apply();
    }

    /*
     * Restore heap blocks stored by opa_heap_blocks_stash to the heap. This should only be called after a opa_heap_ptr_set to the a heap pointer recorded by opa_heap_ptr_get after the previous call to opa_heap_blocks_stash.
     */
    public void opaHeapBlocksRestore() {
        instance.export("opa_heap_blocks_restore").apply();
    }

    /*
     * Drop all heap blocks saved by opa_heap_blocks_stash. This leaks memory in the VM unless the caller subsequently invokes opa_heap_ptr_set to a value taken prior to calling opa_heap_blocks_stash. (see below)
     */
    public void opaHeapBlocksClear() {
        instance.export("opa_heap_blocks_clear").apply();
    }

    /*
     * One-off policy evaluation method. Its arguments are everything needed to evaluate: entrypoint, address of data in memory, address and length of input JSON string in memory, heap address to use, and the output format (0 is JSON, 1 is “value”, i.e. serialized Rego values). The first argument is reserved for future use and must be 0. Returns the address to the serialised result value.
     */
    public void opaEval(
            int entrypointId, int data, int input, int inputLen, int heapPtr, int format) {
        if (format != 0 && format != 1) {
            throw new IllegalArgumentException("Format must be 0: JSON or 1: \"value\"");
        }
        instance.export("opa_heap_blocks_clear")
                .apply(
                        Value.i32(0), // reserved for future use
                        Value.i32(entrypointId),
                        Value.i32(data),
                        Value.i32(input),
                        Value.i32(inputLen),
                        Value.i32(heapPtr),
                        Value.i32(format));
    }

    // helper functions - can be written by the end user
    public String readString(int addr) {
        int resultAddr = opaJsonDump(addr);
        var resultStr = memory().readCString(resultAddr);
        opaFree(resultAddr);
        return resultStr;
    }

    public int writeResult(String result) {
        var resultStrAddr = opaMalloc(result.length());
        memory().writeCString(resultStrAddr, result);
        var resultAddr = opaJsonParse(resultStrAddr, result.length());
        opaFree(resultStrAddr);
        return resultAddr;
    }
}
