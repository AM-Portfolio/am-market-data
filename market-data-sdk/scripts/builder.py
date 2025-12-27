import subprocess
import shutil
import sys
from .config import PROJECT_ROOT, SCHEMA_PATH, ASYNC_SCHEMA_PATH, SDKS, SDK_DIR

def run_command(cmd, cwd=None, show_output=True):
    if show_output:
        print(f"\n🔧 Running: {cmd}")
    result = subprocess.run(
        cmd, shell=True, cwd=cwd,
        capture_output=not show_output, text=True
    )
    if result.returncode != 0:
        if not show_output:
            print(f"❌ Command failed: {result.stderr}")
        return False
    return True

def build_schema():
    print("\n[BUILD] Regenerating OpenAPI Schema...")
    return run_command("mvn test -pl market-data-api -Dtest=OpenApiGeneratorTest", cwd=PROJECT_ROOT)

def build_sdks():
    print(f"\n[BUILD] Generating SDKs (Standard OpenAPI + AsyncAPI)...")
    if not SCHEMA_PATH.exists():
        print(f"❌ Schema not found at {SCHEMA_PATH}")
        return False

    for name, info in SDKS.items():
        # 1. OpenAPI REST Client Generation
        print(f"\n  [{name}] Generating REST Client (OpenAPI)...")
        
        output_path = info['path']
        # JAVA SPECIAL HANDLING: Multi-module output
        if name == "Java":
            output_path = info['path'] / "rest-client"
            output_path.mkdir(parents=True, exist_ok=True)
            
        gen_cmd = f"openapi-generator-cli generate -i {SCHEMA_PATH} -g {info['gen']} -o {output_path} {info['props']}"
        if not run_command(gen_cmd, show_output=False):
            print(f"❌ REST Generation failed for {name} SDK")
            return False
        
        # 2. AsyncAPI WebSocket Client Generation
        if ASYNC_SCHEMA_PATH.exists():
            async_lang = None
            if name == "Java": async_lang = "java"
            elif name == "Python": async_lang = "python"
            elif name == "Dart": async_lang = "dart"
            
            if async_lang:
                print(f"  [{name}] Generating WebSocket Client (AsyncAPI)...")
                
                async_output = info['path']
                # JAVA SPECIAL HANDLING: Multi-module output
                if name == "Java":
                    async_output = info['path'] / "websocket-client"
                    async_output.mkdir(parents=True, exist_ok=True)

                # Simple standard generation command
                # Using --force-write to overwrite if needed
                # Using -p server=dev to ensure server selection
                async_cmd = f"asyncapi generate client {async_lang} {ASYNC_SCHEMA_PATH} -o {async_output} -p server=dev --force-write"
                
                if not run_command(async_cmd, show_output=False):
                    print(f"⚠️  AsyncAPI Generation failed for {name} (non-blocking)")
                else:
                    # Essential Cleanup: Move root-dumped files to package structure
                    if name == "Python":
                        # Move client.py & models to market_data_client/websocket
                        dest_dir = info['path'] / "market_data_client" / "websocket"
                        dest_dir.mkdir(parents=True, exist_ok=True)
                        (dest_dir / "__init__.py").touch()
                        
                        for f in info['path'].glob("*.py"):
                             if f.name != "setup.py":
                                 shutil.move(str(f), str(dest_dir / f.name))
                                 print(f"    🐍 Moved {f.name} -> {dest_dir.name}")

                    elif name == "Dart":
                         # Move client.dart to lib/src/websocket
                         dest_dir = info['path'] / "lib" / "src" / "websocket"
                         dest_dir.mkdir(parents=True, exist_ok=True)
                         
                         client_file = info['path'] / "client.dart"
                         if client_file.exists():
                             shutil.move(str(client_file), str(dest_dir / "client.dart"))
                             print(f"    🎯 Moved client.dart -> {dest_dir.name}")
                             
                    print(f"  ✅ {name} WebSocket Client generated")
        
        if name == "Java":
            create_java_root_pom(info['path'])
            print(f"  ✅ Java Multi-Module POM created")
        
        print(f"  ✅ {name} SDK generation complete")
    
    return True

def create_java_root_pom(path):
    pom_content = """<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.am.marketdata</groupId>
  <artifactId>market-data-sdk-java-parent</artifactId>
  <version>1.0.0-SNAPSHOT</version>
  <packaging>pom</packaging>
  <name>Market Data SDK Java Parent</name>
  <modules>
    <module>rest-client</module>
    <module>websocket-client</module>
  </modules>
</project>
"""
    (path / "pom.xml").write_text(pom_content, encoding="utf-8")

if __name__ == "__main__":
    if build_schema():
        build_sdks()
