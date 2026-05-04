package example

import rego.v1

default allow := false

allow if {
    input.user == "admin"
}

allow if {
    input.action == "read"
    input.user in data.authorized_readers
}
