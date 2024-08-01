package com.dylibso.wasm.opa;

import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.runtime.Memory;
import com.dylibso.chicory.wasm.types.MemoryLimits;

// to be implemented by the user
public class OpaDefaultImports implements OpaImports {
    // TODO: review the default min, max limits
    // hold a reference to the runtime memory
    protected final Memory memory;

    public OpaDefaultImports() {
        this(10, MemoryLimits.MAX_PAGES);
    }

    public OpaDefaultImports(int initial, int maximum) {
        memory = new Memory(new MemoryLimits(initial, maximum));
    }

    public Memory memory() {
        return memory;
    }

    @Override
    public void opaPrintln(Instance instance, int ptr) {
        var message = instance.memory().readCString(ptr);
        System.out.println("opa_println - " + message);
    }

    @Override
    public void opaAbort(Instance instance, int ptr) {
        var errorMessage = instance.memory().readCString(ptr);
        throw new RuntimeException("opa_abort - " + errorMessage);
    }

    @Override
    public int opaBuiltin0(Instance instance, int builtinId, int ctx) {
        throw new RuntimeException("opa_builtin0 - not implemented");
    }

    @Override
    public int opaBuiltin1(Instance instance, int builtinId, int ctx, int _1) {
        throw new RuntimeException("opa_builtin1 - not implemented");
    }

    @Override
    public int opaBuiltin2(Instance instance, int builtinId, int ctx, int _1, int _2) {
        throw new RuntimeException("opa_builtin2 - not implemented");
    }

    @Override
    public int opaBuiltin3(Instance instance, int builtinId, int ctx, int _1, int _2, int _3) {
        throw new RuntimeException("opa_builtin3 - not implemented");
    }

    @Override
    public int opaBuiltin4(
            Instance instance, int builtinId, int ctx, int _1, int _2, int _3, int _4) {
        throw new RuntimeException("opa_builtin4 - not implemented");
    }
}
