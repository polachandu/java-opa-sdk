# This is the Rego source that was compiled to produce authz-policy.json.
#
# To regenerate the IR plan, compile with:
#   opa build -t plan -e authz/allow -o bundle.tar.gz authz-policy.rego
# Then extract plan.json from the bundle tarball.
#
# Test data:
#   input (authz-input.json):  {"user": {"id": "alicex", "groups": ["super"]}}
#   data  (authz-data.json):   {"groups": {"super": {"privileged": true}}}

package authz

default allow := false

allow if {
	input.user.id in {"alice", "kurt"}
}

allow if {
	some group in input.user.groups
	data.groups[group].privileged
}
