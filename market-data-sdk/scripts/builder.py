import subprocess
import sys
from .config import PROJECT_ROOT, SCHEMA_PATH, SDKS
from .injector import inject_all

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
    print("\n[BUILD] Generating SDKs...")
    if not SCHEMA_PATH.exists():
        print(f"❌ Schema not found at {SCHEMA_PATH}")
        return False

    for name, info in SDKS.items():
        gen_cmd = f"openapi-generator-cli generate -i {SCHEMA_PATH} -g {info['gen']} -o {info['path']} {info['props']}"
        if not run_command(gen_cmd, show_output=False):
            print(f"❌ Generation failed for {name} SDK")
            return False
        print(f"  ✅ Generated {name} SDK")
    
    inject_all(SDKS)
    return True

if __name__ == "__main__":
    if build_schema():
        build_sdks()
