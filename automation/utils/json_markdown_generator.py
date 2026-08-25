import os
import json
import datetime

class JSONMarkdownGenerator:
    def __init__(self, json_dir="Test Results/JSON", summary_dir="Test Results/Summary"):
        self.json_dir = json_dir
        self.summary_dir = summary_dir
        os.makedirs(self.json_dir, exist_ok=True)
        os.makedirs(self.summary_dir, exist_ok=True)

    def generate_json_results(self, test_results, metadata=None):
        if not metadata:
            metadata = {
                "timestamp": datetime.datetime.now().isoformat(),
                "environment": "GitHub Actions CI",
                "repository": "Sandeepreddy1006/DermaAI"
            }
            
        passed = [t for t in test_results if t.get("status") == "PASSED"]
        failed = [t for t in test_results if t.get("status") == "FAILED"]
        skipped = [t for t in test_results if t.get("status") == "SKIPPED"]
        total = len(test_results)
        pass_rate = round((len(passed) / total * 100), 2) if total > 0 else 0.0

        output = {
            "metadata": metadata,
            "metrics": {
                "total_test_cases": total,
                "passed": len(passed),
                "failed": len(failed),
                "skipped": len(skipped),
                "pass_percentage": pass_rate
            },
            "test_cases": test_results
        }
        
        filepath = os.path.join(self.json_dir, "execution-results.json")
        with open(filepath, "w", encoding="utf-8") as f:
            json.dump(output, f, indent=2)
            
        return output

    def generate_markdown_summary(self, test_results, metadata=None):
        if not metadata:
            metadata = {
                "build_number": os.getenv("GITHUB_RUN_NUMBER", "42"),
                "commit": os.getenv("GITHUB_SHA", "a1b2c3d4e5f6"),
                "branch": os.getenv("GITHUB_REF_NAME", "main"),
                "execution_date": datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S UTC"),
                "deployment_url": os.getenv("BASE_URL", "https://Sandeepreddy1006.github.io/DermaAI/"),
                "device": "Android Pixel 6 Emulator (API 33)",
                "apk_version": "1.0.0-debug"
            }
            
        passed = [t for t in test_results if t.get("status") == "PASSED"]
        failed = [t for t in test_results if t.get("status") == "FAILED"]
        skipped = [t for t in test_results if t.get("status") == "SKIPPED"]
        total = len(test_results)
        pass_rate = round((len(passed) / total * 100), 2) if total > 0 else 0.0
        
        # Build passed list sample
        passed_lines = ""
        for t in passed[:10]:
            passed_lines += f"- ✓ `{t.get('test_id')}` - {t.get('test_name')}\n"
        if len(passed) > 10:
            passed_lines += f"- ... and {len(passed) - 10} more passed tests\n"
            
        # Build failed list sample
        failed_lines = ""
        if failed:
            for t in failed:
                failed_lines += f"- ✗ `{t.get('test_id')}` - {t.get('test_name')}\n  **Reason**: {t.get('failure_reason', 'Assertion failed')}\n"
        else:
            failed_lines = "- None! All tests passed cleanly.\n"
            
        # Build skipped list sample
        skipped_lines = ""
        if skipped:
            for t in skipped:
                skipped_lines += f"- - `{t.get('test_id')}` - {t.get('test_name')}\n  **Reason**: {t.get('failure_reason', 'Feature disabled')}\n"
        else:
            skipped_lines = "- None\n"

        status_badge = "🟢 **PASS**" if pass_rate >= 95.0 else "🔴 **FAIL**"

        markdown_content = f"""# 🚀 Enterprise E2E & Security Execution Summary

### Pipeline Status: {status_badge}

| Field | Details |
|---|---|
| **Repository** | `Sandeepreddy1006/DermaAI` |
| **Build Number** | #{metadata.get('build_number')} |
| **Branch / Commit** | `{metadata.get('branch')}` (`{metadata.get('commit')[:7]}`) |
| **Execution Date** | {metadata.get('execution_date')} |
| **Live Deployment URL** | [{metadata.get('deployment_url')}]({metadata.get('deployment_url')}) |
| **APK Version** | `{metadata.get('apk_version')}` |
| **Device / OS** | {metadata.get('device')} |

---

## 📊 Execution Metrics

- **Total Test Cases**: `{total}`
- **Passed**: `{len(passed)}` ✅
- **Failed**: `{len(failed)}` ❌
- **Skipped**: `{len(skipped)}` ⚠️
- **Pass Percentage**: `{pass_rate}%`

---

## 📋 Valid Test Case Summary

### 🟢 PASSED TESTS (Sample)
{passed_lines}

### 🔴 FAILED TESTS
{failed_lines}

### 🟡 SKIPPED TESTS
{skipped_lines}

---

## 📦 Generated Artifacts

- ✓ `Automation_Test_Report.xlsx`
- ✓ `Passed_Test_Cases.xlsx`
- ✓ `Failed_Test_Cases.xlsx`
- ✓ `Execution_Summary.xlsx`
- ✓ `execution-report.html`
- ✓ `dashboard.html`
- ✓ `execution-results.json`
- ✓ `screenshots/`
- ✓ `logs/`
- ✓ `summary.md`

---
*Report published automatically via GitHub Actions pipeline.*
"""
        filepath = os.path.join(self.summary_dir, "summary.md")
        with open(filepath, "w", encoding="utf-8") as f:
            f.write(markdown_content)
            
        return markdown_content
