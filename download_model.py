import os
import shutil
import sys
from pathlib import Path

PROJECT_ROOT = Path(__file__).parent.resolve()
RAW_DIR = PROJECT_ROOT / "app" / "src" / "main" / "res" / "raw"
TARGET_FILE = RAW_DIR / "gemma_2b_it_gpu_int4.task"

def main():
    print("==================================================")
    print("   THUNA ASSISTANT - GEMMA 2B MODEL DOWNLOADER   ")
    print("==================================================")

    RAW_DIR.mkdir(parents=True, exist_ok=True)
    print(f"[✓] Target folder verified: {RAW_DIR}")

    if TARGET_FILE.exists():
        file_size_gb = TARGET_FILE.stat().st_size / (1024 * 1024 * 1024)
        print(f"[!] Model file already exists: {TARGET_FILE}")
        print(f"[!] File size: {file_size_gb:.2f} GB")
        if file_size_gb > 1.0:
            print("[✓] Valid model size confirmed (~1.5 GB). Skipping download!")
            return

    print("\nDownloading Gemma 2B model via KaggleHub...")
    try:
        import kagglehub
        path = kagglehub.model_download("google/gemma/tfLite/gemma-1.1-2b-it-gpu-int4")
        print(f"Downloaded model to path: {path}")

        downloaded_dir = Path(path)
        task_files = list(downloaded_dir.glob("*.task"))
        if not task_files:
            task_files = list(downloaded_dir.rglob("*.task"))

        if task_files:
            src_file = task_files[0]
            print(f"Copying {src_file.name} -> {TARGET_FILE}...")
            shutil.copy(src_file, TARGET_FILE)
        else:
            print("[X] Could not find .task file in downloaded kagglehub directory.")
            sys.exit(1)

    except Exception as e:
        print(f"[X] KaggleHub download failed: {e}")
        sys.exit(1)

    final_size_mb = TARGET_FILE.stat().st_size / (1024 * 1024)
    final_size_gb = final_size_mb / 1024
    print("\n--------------------------------------------------")
    print(f"[✓] SUCCESS! Model saved to: {TARGET_FILE}")
    print(f"[✓] Final File Size: {final_size_mb:.2f} MB ({final_size_gb:.2f} GB)")
    print("--------------------------------------------------")

if __name__ == "__main__":
    main()
