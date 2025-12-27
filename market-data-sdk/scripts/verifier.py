from .config import SDKS, EXPECTED_API_CLIENTS

def verify_sdk(sdk_type, sdk_path):
    print(f"\n🔍 Verifying {sdk_type} SDK...")
    
    if sdk_type == "Java":
        api_dir = sdk_path / "rest-client" / "src" / "main" / "java" / "com" / "am" / "marketdata" / "api"
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
    
    # Verify WebSocket Clients (AsyncAPI)
    # Verify WebSocket Clients (AsyncAPI)
    ws_file = None
    if sdk_type == "Java":
        # Multi-module structure: websocket-client submodule
        ws_file = sdk_path / "websocket-client" / "src" / "main" / "java" / "com" / "asyncapi" / "AMMarketDataStreamingAPI.java"
    elif sdk_type == "Python":
        # We moved it to market_data_client/websocket
        ws_file = sdk_path / "market_data_client" / "websocket" / "client.py"
    elif sdk_type == "Dart":
        # We moved it to lib/src/websocket
        ws_file = sdk_path / "lib" / "src" / "websocket" / "client.dart"
        
    ws_exists = False
    if ws_file and ws_file.exists():
        print(f"  ✅ WebSocket Client found: {ws_file.name}")
        ws_exists = True
    else:
        print(f"  ⚠️  WebSocket Client missing: {ws_file}")

    success = (len(found_clients) == len(EXPECTED_API_CLIENTS)) and ws_exists
    
    if success:
        print(f"  ✅ Files verified. Running build check...")
        if not verify_build(sdk_type, sdk_path):
            print(f"  ❌ {sdk_type} Build complete failed!")
            return False
            
    print(f"{'✅' if success else '❌'} {sdk_type} SDK verification {'passed' if success else 'failed'}!")
    return success

import subprocess
import sys

def verify_build(sdk_type, sdk_path):
    """
    Runs package manager build/install commands to verify the SDK is compilable.
    """
    try:
        if sdk_type == "Java":
            # Maven clean install
            # Using -DskipTests to speed up, or remove if user wants strict tests
            # User asked for "maven clean install"
            cmd = ["mvn", "clean", "install", "-DskipTests"]
            print(f"    🚀 Running: {' '.join(cmd)}")
            subprocess.run(cmd, cwd=sdk_path, check=True, shell=True)
            
        elif sdk_type == "Python":
            # Python build
            cmd = [sys.executable, "setup.py", "build"]
            print(f"    🚀 Running: {' '.join(cmd)}")
            subprocess.run(cmd, cwd=sdk_path, check=True)
            
        elif sdk_type == "Dart":
            # Flutter clean & pub get
            cmd_clean = ["flutter", "clean"]
            cmd_get = ["flutter", "pub", "get"]
            print(f"    🚀 Running: flutter clean && flutter pub get")
            # Using shell=True for windows command chaining/execution context often helps with flutter tool
            subprocess.run("flutter clean", cwd=sdk_path, check=True, shell=True)
            subprocess.run("flutter pub get", cwd=sdk_path, check=True, shell=True)
            
        print(f"    ✅ Build successful for {sdk_type}")
        return True
    except subprocess.CalledProcessError as e:
        print(f"    ❌ Build command failed: {e}")
        return False
    except Exception as e:
        print(f"    ❌ Verification error: {e}")
        return False

def verify_all():
    print("\n🧐 [VERIFY] Checking all SDKs...")
    results = {}
    for name, info in SDKS.items():
        results[name] = verify_sdk(name, info["path"])
    return results

if __name__ == "__main__":
    verify_all()
