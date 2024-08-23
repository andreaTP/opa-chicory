package com.github.andreaTP.opa.chicory;

import com.dylibso.chicory.runtime.HostModuleInstance;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasm.types.Value;

public class OpaImportsAdapter {

    private final OpaImports opaImports;
    private final OpaWasm opaWasm;

    public OpaImportsAdapter(OpaImports opaImports, OpaWasm opaWasm) {
        this.opaImports = opaImports;
        this.opaWasm = opaWasm;
    }

    public HostModuleInstance toHostModuleInstance(String name) {
        OpaImportsAdapter adapter = new OpaImportsAdapter(opaImports, opaWasm);
        return HostModuleInstance.builder(OpaImportsHostModule.named(name))
                .bind("opa_abort", adapter::opaAbort)
                .bind("opa_println", adapter::opaPrintln)
                .bind("opa_builtin0", adapter::opaBuiltin0)
                .bind("opa_builtin1", adapter::opaBuiltin1)
                .bind("opa_builtin2", adapter::opaBuiltin2)
                .bind("opa_builtin3", adapter::opaBuiltin3)
                .bind("opa_builtin4", adapter::opaBuiltin4)
                .build();
    }

    private Value[] opaAbort(Instance instance, Value... args) {
        this.opaImports.opaAbort(this.opaWasm, args[0].asInt());
        return new Value[]{};
    }

    private Value[] opaPrintln(Instance instance, Value... args) {
        this.opaImports.opaPrintln(this.opaWasm, args[0].asInt());
        return new Value[]{};
    }

    private Value[] opaBuiltin0(Instance instance, Value... args) {
        return new Value[]{
                Value.i32(this.opaImports.opaBuiltin0(this.opaWasm, args[0].asInt(), args[1].asInt()))
        };
    }

    private Value[] opaBuiltin1(Instance instance, Value... args) {
        return new Value[]{
                Value.i32(
                        this.opaImports.opaBuiltin1(
                                this.opaWasm, args[0].asInt(), args[1].asInt(), args[2].asInt()))
        };
    }

    private Value[] opaBuiltin2(Instance instance, Value... args) {
        return new Value[]{
                Value.i32(
                        this.opaImports.opaBuiltin2(
                                this.opaWasm,
                                args[0].asInt(),
                                args[1].asInt(),
                                args[2].asInt(),
                                args[3].asInt()))
        };
    }

    private Value[] opaBuiltin3(Instance instance, Value... args) {
        return new Value[]{
                Value.i32(
                        this.opaImports.opaBuiltin3(
                                this.opaWasm,
                                args[0].asInt(),
                                args[1].asInt(),
                                args[2].asInt(),
                                args[3].asInt(),
                                args[4].asInt()))
        };
    }

    private Value[] opaBuiltin4(Instance instance, Value... args) {
        return new Value[]{
                Value.i32(
                        this.opaImports.opaBuiltin4(
                                this.opaWasm,
                                args[0].asInt(),
                                args[1].asInt(),
                                args[2].asInt(),
                                args[3].asInt(),
                                args[4].asInt(),
                                args[5].asInt()))
        };
    }

}
