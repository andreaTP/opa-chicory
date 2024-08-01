package com.dylibso.wasm.opa;

import com.dylibso.chicory.runtime.Instance;

public class Builtin {
    private Builtin() {};

    public interface IBuiltin {
        String name();

        default int asBuiltin0(Instance instance) {
            throw new RuntimeException("opa_builtin - " + name() + " doesn't implement builtin0");
        }

        default int asBuiltin1(Instance instance, int _1) {
            throw new RuntimeException("opa_builtin - " + name() + " doesn't implement builtin1");
        }

        default int asBuiltin2(Instance instance, int _1, int _2) {
            throw new RuntimeException("opa_builtin - " + name() + " doesn't implement builtin2");
        }

        default int asBuiltin3(Instance instance, int _1, int _2, int _3) {
            throw new RuntimeException("opa_builtin - " + name() + " doesn't implement builtin3");
        }

        default int asBuiltin4(Instance instance, int _1, int _2, int _3, int _4) {
            throw new RuntimeException("opa_builtin - " + name() + " doesn't implement builtin4");
        }
    }

    @FunctionalInterface
    public interface AsBuiltin0 {
        int apply(Instance instance);
    }

    @FunctionalInterface
    public interface AsBuiltin1 {
        int apply(Instance instance, int _1);
    }

    public class Builtin0 implements IBuiltin {
        private final String name;
        private final AsBuiltin0 fn;

        Builtin0(String name, AsBuiltin0 fn) {
            this.name = name;
            this.fn = fn;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public int asBuiltin0(Instance instance) {
            return fn.apply(instance);
        }
    }

    public static class Builtin1 implements IBuiltin {
        private final String name;
        private final AsBuiltin1 fn;

        public Builtin1(String name, AsBuiltin1 fn) {
            this.name = name;
            this.fn = fn;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public int asBuiltin1(Instance instance, int _1) {
            return fn.apply(instance, _1);
        }
    }
}
