import sys
from .cleaner import clean_all
from .builder import build_schema, build_sdks
from .verifier import verify_all

def main():
    print("="*60)
    print("🚀 Unified SDK Build & Verify (Modular Architecture)")
    print("="*60)
    
    # 1. BUILD SCHEMA (Must happen before clean because Java SDK is a Maven module)
    if not build_schema():
        print("❌ Schema generation failed. Aborting.")
        sys.exit(1)

    # 2. CLEAN
    clean_all()
    
    # 3. BUILD SDKS
    if not build_sdks():
        print("❌ SDK generation or injection failed. Aborting.")
        sys.exit(1)
        
    # 3. VERIFY
    results = verify_all()
    
    # Summary
    print("\n" + "="*60)
    print("📋 FINAL SUMMARY")
    print("="*60)
    all_passed = True
    for name, passed in results.items():
        print(f"{name} SDK: {'✅ PASS' if passed else '❌ FAIL'}")
        if not passed: all_passed = False
    
    if all_passed:
        print("\n🎉 SUCCESS! All SDKs built and verified correctly!")
    else:
        print("\n❌ FAILURE! Some verification steps failed.")
        sys.exit(1)

if __name__ == "__main__":
    main()
