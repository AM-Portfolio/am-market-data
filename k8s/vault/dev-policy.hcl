# Allow pods to read any secret in the kv/preprod path
path "kv/preprod/*" {
  capabilities = ["read", "list"]
}