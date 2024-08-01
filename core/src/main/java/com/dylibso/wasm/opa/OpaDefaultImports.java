package com.dylibso.wasm.opa;

import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.runtime.Memory;
import com.dylibso.chicory.wasm.types.MemoryLimits;

import java.util.List;
import java.util.Map;

// to be implemented by the user
public class OpaDefaultImports implements OpaImports {
    // TODO: review the default min, max limits
    // hold a reference to the runtime memory
    protected final Memory memory;
    protected Builtin.IBuiltin[] builtins = new Builtin.IBuiltin[0];

    public OpaDefaultImports() {
        this(10, MemoryLimits.MAX_PAGES);
    }

    public OpaDefaultImports(Builtin.IBuiltin... builtins) {
        this(10, MemoryLimits.MAX_PAGES, builtins);
    }

    public OpaDefaultImports(int initial, int maximum) {
        this.memory = new Memory(new MemoryLimits(initial, maximum));
    }

    public OpaDefaultImports(int initial, int maximum, Builtin.IBuiltin... builtins) {
        this.builtins = builtins;
        this.memory = new Memory(new MemoryLimits(initial, maximum));
    }

    @Override
    public void initializeBuiltins(Map<String, Integer> mappings) {
        var result = new Builtin.IBuiltin[mappings.size()];
        // Default initialization to have proper error messages
        for (var m: mappings.entrySet()) {
            result[m.getValue()] = () -> m.getKey();
        }
        for (var builtin: this.builtins) {
            if (mappings.containsKey(builtin.name())) {
                result[mappings.get(builtin.name())] = builtin;
            }
        }
        this.builtins = result;
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
        if (builtinId > 0 && builtinId < builtins.length && builtins[builtinId] != null) {
            return builtins[builtinId].asBuiltin0(instance);
        }
        throw new RuntimeException("opa_builtin0 - " + builtinId + "- not implemented");
    }

    @Override
    public int opaBuiltin1(Instance instance, int builtinId, int ctx, int _1) {
        if (builtinId > 0 && builtinId < builtins.length && builtins[builtinId] != null) {
            return builtins[builtinId].asBuiltin1(instance, _1);
        }
        throw new RuntimeException("opa_builtin1 - " + builtinId + " - not implemented");
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
