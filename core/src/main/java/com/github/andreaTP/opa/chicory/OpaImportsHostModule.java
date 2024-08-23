package com.github.andreaTP.opa.chicory;

import com.dylibso.chicory.runtime.HostModule;
import com.dylibso.chicory.wasm.types.ValueType;
import java.util.List;

public class OpaImportsHostModule {
    public static HostModule named(String name) {
        return HostModule.builder(name)
                .withFunctionSignature("opa_abort", List.of(ValueType.I32), List.of())
                .withFunctionSignature("opa_println", List.of(ValueType.I32), List.of())
                .withFunctionSignature(
                        "opa_builtin0",
                        List.of(ValueType.I32, ValueType.I32),
                        List.of(ValueType.I32))
                .withFunctionSignature(
                        "opa_builtin1",
                        List.of(ValueType.I32, ValueType.I32, ValueType.I32),
                        List.of(ValueType.I32))
                .withFunctionSignature(
                        "opa_builtin2",
                        List.of(ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32),
                        List.of(ValueType.I32))
                .withFunctionSignature(
                        "opa_builtin3",
                        List.of(
                                ValueType.I32,
                                ValueType.I32,
                                ValueType.I32,
                                ValueType.I32,
                                ValueType.I32),
                        List.of(ValueType.I32))
                .withFunctionSignature(
                        "opa_builtin4",
                        List.of(
                                ValueType.I32,
                                ValueType.I32,
                                ValueType.I32,
                                ValueType.I32,
                                ValueType.I32,
                                ValueType.I32),
                        List.of(ValueType.I32))
                .build();
    }
}
