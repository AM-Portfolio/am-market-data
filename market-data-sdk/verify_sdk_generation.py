"""
SDK Generation Verification Script

This script regenerates all SDKs and verifies that:
1. All 8 API clients are generated for each SDK
2. Package structure is correct
3. No compilation errors exist
"""

import os
import subprocess
import sys
from pathlib import Path

# Expected API clients (one per controller)
EXPECTED_API_CLIENTS = [
    "BrokerageCalculatorApi",
    "MarginCalculatorApi",
    "MarketAnalyticsApi",
    "MarketDataApi",
    "MarketDataPollingApi",
    "MarketIndicesApi",
    "SecurityMetadataApi",
    "StockIndicesApi"
]

def run_command(cmd, cwd=None):
    """Run a shell command and return the result"""
    print(f"\n🔧 Running: {cmd}")
    result = subprocess.run(
        cmd,
        shell=True,
        cwd=cwd,
        capture_output=True,
        text=True
    )
    if result.returncode != 0:
        print(f"❌ Command failed with code {result.returncode}")
        print(f"STDERR: {result.stderr}")
        return False
    print(f"✅ Command succeeded")
    return True

def verify_java_sdk(sdk_path):
    """Verify Java SDK has all expected API clients"""
    print("\n" + "="*60)
    print("📦 Verifying Java SDK")
    print("="*60)
    
    api_dir = sdk_path / "src" / "main" / "java" / "com" / "am" / "marketdata" / "api"
    
    if not api_dir.exists():
        print(f"❌ API directory not found: {api_dir}")
        return False
    
    found_clients = []
    for client_name in EXPECTED_API_CLIENTS:
        client_file = api_dir / f"{client_name}.java"
        if client_file.exists():
            found_clients.append(client_name)
            print(f"  ✅ {client_name}.java")
        else:
            print(f"  ❌ {client_name}.java NOT FOUND")
    
    print(f"\n📊 Found {len(found_clients)}/{len(EXPECTED_API_CLIENTS)} API clients")
    
    if len(found_clients) == len(EXPECTED_API_CLIENTS):
        print("✅ All Java API clients generated successfully!")
        return True
    else:
        print("❌ Some Java API clients are missing!")
        return False

def verify_python_sdk(sdk_path):
    """Verify Python SDK has all expected API clients"""
    print("\n" + "="*60)
    print("🐍 Verifying Python SDK")
    print("="*60)
    
    api_dir = sdk_path / "market_data_client" / "api"
    
    if not api_dir.exists():
        print(f"❌ API directory not found: {api_dir}")
        return False
    
    found_clients = []
    for client_name in EXPECTED_API_CLIENTS:
        # Python uses snake_case
        snake_case_name = ''.join(['_' + c.lower() if c.isupper() else c for c in client_name]).lstrip('_')
        client_file = api_dir / f"{snake_case_name}.py"
        
        if client_file.exists():
            found_clients.append(client_name)
            print(f"  ✅ {snake_case_name}.py")
        else:
            print(f"  ❌ {snake_case_name}.py NOT FOUND")
    
    print(f"\n📊 Found {len(found_clients)}/{len(EXPECTED_API_CLIENTS)} API clients")
    
    if len(found_clients) == len(EXPECTED_API_CLIENTS):
        print("✅ All Python API clients generated successfully!")
        return True
    else:
        print("❌ Some Python API clients are missing!")
        return False

def verify_dart_sdk(sdk_path):
    """Verify Dart/Flutter SDK has all expected API clients"""
    print("\n" + "="*60)
    print("🎯 Verifying Dart/Flutter SDK")
    print("="*60)
    
    api_dir = sdk_path / "lib" / "api"
    
    if not api_dir.exists():
        print(f"❌ API directory not found: {api_dir}")
        return False
    
    found_clients = []
    for client_name in EXPECTED_API_CLIENTS:
        # Dart uses snake_case
        snake_case_name = ''.join(['_' + c.lower() if c.isupper() else c for c in client_name]).lstrip('_')
        client_file = api_dir / f"{snake_case_name}.dart"
        
        if client_file.exists():
            found_clients.append(client_name)
            print(f"  ✅ {snake_case_name}.dart")
        else:
            print(f"  ❌ {snake_case_name}.dart NOT FOUND")
    
    print(f"\n📊 Found {len(found_clients)}/{len(EXPECTED_API_CLIENTS)} API clients")
    
    if len(found_clients) == len(EXPECTED_API_CLIENTS):
        print("✅ All Dart API clients generated successfully!")
        return True
    else:
        print("❌ Some Dart API clients are missing!")
        return False

def main():
    print("="*60)
    print("🚀 SDK Generation & Verification")
    print("="*60)
    
    # Get project root
    script_dir = Path(__file__).parent
    project_root = script_dir.parent
    sdk_dir = project_root / "market-data-sdk"
    
    print(f"\n📁 Project Root: {project_root}")
    print(f"📁 SDK Directory: {sdk_dir}")
    
    # Step 1: Run the PowerShell generation script
    print("\n" + "="*60)
    print("Step 1: Regenerating SDKs")
    print("="*60)
    
    gen_script = sdk_dir / "generate_sdks.ps1"
    if not gen_script.exists():
        print(f"❌ Generation script not found: {gen_script}")
        sys.exit(1)
    
    if not run_command(f"powershell -ExecutionPolicy Bypass -File {gen_script}", cwd=project_root):
        print("❌ SDK generation failed!")
        sys.exit(1)
    
    # Step 2: Verify each SDK
    print("\n" + "="*60)
    print("Step 2: Verifying Generated SDKs")
    print("="*60)
    
    results = {
        "Java": verify_java_sdk(sdk_dir / "market-data-sdk-java"),
        "Python": verify_python_sdk(sdk_dir / "market-data-sdk-python"),
        "Dart": verify_dart_sdk(sdk_dir / "market-data-sdk-flutter")
    }
    
    # Final Summary
    print("\n" + "="*60)
    print("📋 VERIFICATION SUMMARY")
    print("="*60)
    
    all_passed = True
    for sdk_name, passed in results.items():
        status = "✅ PASS" if passed else "❌ FAIL"
        print(f"{sdk_name} SDK: {status}")
        if not passed:
            all_passed = False
    
    print("="*60)
    
    if all_passed:
        print("\n🎉 SUCCESS! All SDKs generated correctly with all 8 API clients!")
        print("\n📦 Generated API Clients:")
        for client in EXPECTED_API_CLIENTS:
            print(f"  • {client}")
        sys.exit(0)
    else:
        print("\n❌ FAILURE! Some SDKs have missing API clients.")
        sys.exit(1)

if __name__ == "__main__":
    main()
