from .config import SDKS, EXPECTED_API_CLIENTS

def verify_sdk(sdk_type, sdk_path):
    print(f"\n🔍 Verifying {sdk_type} SDK...")
    
    if sdk_type == "Java":
        api_dir = sdk_path / "src" / "main" / "java" / "com" / "am" / "marketdata" / "api"
        ext = ".java"
    elif sdk_type == "Python":
        api_dir = sdk_path / "market_data_client" / "api"
        ext = ".py"
    elif sdk_type == "Dart":
        api_dir = sdk_path / "lib" / "api"
        ext = ".dart"
    else:
        return False

    if not api_dir.exists():
        print(f"  ❌ API directory not found: {api_dir}")
        return False
    
    found_clients = []
    for client_name in EXPECTED_API_CLIENTS:
        filename = client_name
        if sdk_type != "Java":
            filename = "".join(["_" + c.lower() if c.isupper() else c for c in client_name]).lstrip("_")
        
        client_file = api_dir / f"{filename}{ext}"
        if client_file.exists():
            found_clients.append(client_name)
            print(f"  ✅ {filename}{ext}")
        else:
            print(f"  ❌ {filename}{ext} NOT FOUND")
    
    success = len(found_clients) == len(EXPECTED_API_CLIENTS)
    print(f"{'✅' if success else '❌'} {sdk_type} SDK verification {'passed' if success else 'failed'}!")
    return success

def verify_all():
    print("\n🧐 [VERIFY] Checking all SDKs...")
    results = {}
    for name, info in SDKS.items():
        results[name] = verify_sdk(name, info["path"])
    return results

if __name__ == "__main__":
    verify_all()
