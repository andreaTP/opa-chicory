#!/bin/bash
opa build -t wasm -o bundle.tar.gz -e opa/wasm/test/allowed policy.rego
tar xzf bundle.tar.gz --directory=opa /policy.wasm
rm -f bundle.tar.gz
