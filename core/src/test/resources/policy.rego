package opa.wasm.test

default allowed = false

allowed {
    user := input.user
    data.role[user] == "admin"
}
