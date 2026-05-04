# Example Policy Bundle

This directory contains a simple OPA policy and a pre-built IR bundle for use with the SDK examples.

## Policy

`policy.rego` defines a rule `example/allow` that grants access when:
- The user is `"admin"`, **or**
- The action is `"read"` and the user is in the `authorized_readers` list

`data.json` provides the static data:
```json
{
    "authorized_readers": ["alice", "bob", "charlie"]
}
```

## Example Evaluations

| Input | Result | Reason |
|-------|--------|--------|
| `{"user": "admin", "action": "write"}` | `true` | admin always allowed |
| `{"user": "alice", "action": "read"}` | `true` | alice is an authorized reader |
| `{"user": "alice", "action": "write"}` | `false` | alice can only read |
| `{"user": "eve", "action": "read"}` | `false` | eve is not an authorized reader |

## Rebuilding the Bundle

If you modify the policy or data, rebuild with:

```bash
cd examples
opa build -t plan -e 'example/allow' -b . -o bundle.tar.gz
```
