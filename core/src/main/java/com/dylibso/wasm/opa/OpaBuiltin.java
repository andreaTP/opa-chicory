package com.dylibso.wasm.opa;

public class OpaBuiltin {
    private OpaBuiltin() {}

    public interface Builtin {
        String name();

        default int asBuiltin0(OpaWasm instance) {
            throw new RuntimeException("opa_builtin - " + name() + " doesn't implement builtin0");
        }

        default int asBuiltin1(OpaWasm instance, int _1) {
            throw new RuntimeException("opa_builtin - " + name() + " doesn't implement builtin1");
        }

        default int asBuiltin2(OpaWasm instance, int _1, int _2) {
            throw new RuntimeException("opa_builtin - " + name() + " doesn't implement builtin2");
        }

        default int asBuiltin3(OpaWasm instance, int _1, int _2, int _3) {
            throw new RuntimeException("opa_builtin - " + name() + " doesn't implement builtin3");
        }

        default int asBuiltin4(OpaWasm instance, int _1, int _2, int _3, int _4) {
            throw new RuntimeException("opa_builtin - " + name() + " doesn't implement builtin4");
        }
    }

    @FunctionalInterface
    public interface AsBuiltin0 {
        int apply(OpaWasm instance);
    }

    @FunctionalInterface
    public interface AsBuiltin1 {
        int apply(OpaWasm instance, int _1);
    }

    @FunctionalInterface
    public interface AsBuiltin2 {
        int apply(OpaWasm instance, int _1, int _2);
    }

    @FunctionalInterface
    public interface AsBuiltin3 {
        int apply(OpaWasm instance, int _1, int _2, int _3);
    }

    @FunctionalInterface
    public interface AsBuiltin4 {
        int apply(OpaWasm instance, int _1, int _2, int _3, int _4);
    }

    public class Builtin0 implements Builtin {
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
        public int asBuiltin0(OpaWasm instance) {
            return fn.apply(instance);
        }
    }

    public static class Builtin1 implements Builtin {
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
        public int asBuiltin1(OpaWasm instance, int _1) {
            return fn.apply(instance, _1);
        }
    }

    public static class Builtin2 implements Builtin {
        private final String name;
        private final AsBuiltin2 fn;

        public Builtin2(String name, AsBuiltin2 fn) {
            this.name = name;
            this.fn = fn;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public int asBuiltin2(OpaWasm instance, int _1, int _2) {
            return fn.apply(instance, _1, _2);
        }
    }

    public static class Builtin3 implements Builtin {
        private final String name;
        private final AsBuiltin3 fn;

        public Builtin3(String name, AsBuiltin3 fn) {
            this.name = name;
            this.fn = fn;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public int asBuiltin3(OpaWasm instance, int _1, int _2, int _3) {
            return fn.apply(instance, _1, _2, _3);
        }
    }

    public static class Builtin4 implements Builtin {
        private final String name;
        private final AsBuiltin4 fn;

        public Builtin4(String name, AsBuiltin4 fn) {
            this.name = name;
            this.fn = fn;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public int asBuiltin4(OpaWasm instance, int _1, int _2, int _3, int _4) {
            return fn.apply(instance, _1, _2, _3, _4);
        }
    }
}
