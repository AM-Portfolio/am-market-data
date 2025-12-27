import shutil
import os
from .config import SDKS

def clean_all():
    print("\n🧹 [CLEAN] Selective cleanup of SDK contents...")
    for name, info in SDKS.items():
        if info["path"].exists():
            print(f"  Cleaning {name} SDK contents at {info['path']}")
            for item in info["path"].iterdir():
                if item.name == "pom.xml": continue
                try:
                    if item.is_dir():
                        shutil.rmtree(item)
                    else:
                        item.unlink()
                except Exception as e:
                    print(f"    ⚠️ Could not delete {item}: {e}")
    print("✅ Clean complete.")

if __name__ == "__main__":
    clean_all()
