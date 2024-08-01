package com.dylibso.wasm.opa.builtins;

import com.dylibso.chicory.runtime.Instance;
import com.dylibso.wasm.opa.Builtin;

import java.util.List;

public class Yaml {

    public static final Builtin.Builtin1 isValid = new Builtin.Builtin1("yaml.is_valid",
            (Instance instance, int strAddr) -> {
                System.out.println("YAML is valid");
                return 1;
            });

    public static final Builtin.Builtin1 unmarshal = new Builtin.Builtin1("yaml.unmarshal",
            (Instance instance, int strAddr) -> {
        System.out.println("YAML unmarshal " + strAddr);
        var x = instance.memory().readCString(strAddr);
                System.out.println("YAML unmarshal " + x.length());
        System.out.println("YAML unmarshal " + x);
        return 1;
    });

    public static final Builtin.Builtin1 marshal = new Builtin.Builtin1("yaml.marshal",
            (Instance instance, int strAddr) -> {
                System.out.println("YAML marshal");
                return 1;
            });

    public static Builtin.IBuiltin[] all() {
        return new Builtin.IBuiltin[] {
            isValid,
                marshal,
                unmarshal
        };
    }
}
