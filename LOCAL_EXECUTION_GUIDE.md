# 💻 Local Execution Guide

This guide walks you through setting up your local machine to execute the Appium, Selenium, Backend Security, and Load Test suites.

---

## 1. Prerequisites
- **Python**: Python 3.10+ installed
- **Java**: OpenJDK 17 or JDK 11+
- **Android SDK & Studio** (Optional for local Android Emulator execution)
- **Node.js / Appium** (Optional for live Appium driver attachment)

---

## 2. Environment Setup

### Clone Repository & Install Python Dependencies
```bash
git clone https://github.com/Sandeepreddy1006/DermaAI.git
cd DermaAI

# Install required packages
python -m pip install openpyxl selenium Appium-Python-Client pytest requests pandas jinja2 pyyaml fastapi uvicorn
```

---

## 3. Running Backend Local Server (Optional)
```bash
cd backend
python -m uvicorn main:app --reload --port 8000
```

---

## 4. Running Master Automation Runner
To execute all 1,400+ mobile, web, and backend security test cases:
```bash
python automation/runners/run_all_tests.py
```

### Expected Output
1. Generated Excel workbooks in `Test Results/Excel/`:
   - `Automation_Test_Report.xlsx`
   - `Passed_Test_Cases.xlsx`
   - `Failed_Test_Cases.xlsx`
   - `Execution_Summary.xlsx`
2. Generated HTML dashboards in `Test Results/HTML/`:
   - `execution-report.html`
   - `dashboard.html`
   - `trends.html`
3. Generated JSON data in `Test Results/JSON/execution-results.json`
4. Generated Markdown summary in `Test Results/Summary/summary.md`
5. Updated audit files in `Vulnerability Test Results/`
