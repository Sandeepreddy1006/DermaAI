import os
import sys
import shutil
import time

# Ensure project root is in sys.path
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "../..")))

from automation.config.config import Config
from automation.tests.test_appium_suite import AppiumTestSuite
from automation.tests.test_selenium_suite import SeleniumTestSuite
from automation.tests.test_api_security_suite import APISecurityTestSuite
from automation.utils.excel_generator import ExcelReportGenerator
from automation.utils.html_generator import HTMLReportGenerator
from automation.utils.json_markdown_generator import JSONMarkdownGenerator

def main():
    print("=" * 80)
    print("[+] DERMAAI ENTERPRISE AUTOMATION & SECURITY AUDIT TEST RUNNER")
    print("=" * 80)
    print(f"Target Live Deployment URL: {Config.BASE_URL}")
    print(f"Target Backend API URL:    {Config.API_URL}")
    print("-" * 80)
    
    # 1. Instantiate suites
    appium_suite = AppiumTestSuite()
    selenium_suite = SeleniumTestSuite()
    sec_suite = APISecurityTestSuite()
    
    # 2. Execute test suites
    print("\n[1/4] Executing Mobile Appium E2E Suite (450 Test Cases)...")
    appium_results = appium_suite.execute_all()
    print(f"      Completed {len(appium_results)} Appium mobile tests.")
    
    print("\n[2/4] Executing Live Web Selenium E2E Suite (440 Test Cases)...")
    selenium_results = selenium_suite.execute_all()
    print(f"      Completed {len(selenium_results)} Selenium web tests.")
    
    print("\n[3/4] Executing Backend Security SAST/DAST/API Suite (420 Test Cases)...")
    sec_results = sec_suite.execute_all()
    print(f"      Completed {len(sec_results)} Security & API tests.")
    
    all_results = appium_results + selenium_results + sec_results
    total_count = len(all_results)
    passed_count = len([t for t in all_results if t.get("status") == "PASSED"])
    failed_count = len([t for t in all_results if t.get("status") == "FAILED"])
    skipped_count = len([t for t in all_results if t.get("status") == "SKIPPED"])
    pass_rate = round((passed_count / total_count * 100), 2)
    
    print("\n" + "=" * 80)
    print(f"[SUMMARY] OVERALL EXECUTION RESULTS")
    print(f"Total Tests Executed: {total_count}")
    print(f"Passed:              {passed_count} ({pass_rate}%)")
    print(f"Failed:              {failed_count}")
    print(f"Skipped:             {skipped_count}")
    print("=" * 80)
    
    # 3. Generate Reports
    print("\n[4/4] Generating Excel, HTML, JSON, and Markdown Reports...")
    
    # Excel
    excel_gen = ExcelReportGenerator(output_dir="Test Results/Excel")
    excel_gen.generate_automation_test_report(all_results)
    
    # Data structures for Vulnerability Test Results
    endpoints = [
        {"endpoint": "/signup", "method": "POST", "auth_required": False, "roles": "Public", "controller": "backend/main.py"},
        {"endpoint": "/token", "method": "POST", "auth_required": False, "roles": "Public", "controller": "backend/main.py"},
        {"endpoint": "/reset-password", "method": "POST", "auth_required": False, "roles": "Public", "controller": "backend/main.py"},
        {"endpoint": "/verify-code", "method": "POST", "auth_required": False, "roles": "Public", "controller": "backend/main.py"},
        {"endpoint": "/new-password", "method": "POST", "auth_required": False, "roles": "Public", "controller": "backend/main.py"},
        {"endpoint": "/update", "method": "POST", "auth_required": True, "roles": "User", "controller": "backend/main.py"},
        {"endpoint": "/users/me", "method": "GET", "auth_required": True, "roles": "User", "controller": "backend/main.py"},
        {"endpoint": "/users/me/avatar", "method": "POST", "auth_required": True, "roles": "User", "controller": "backend/main.py"},
        {"endpoint": "/analyze", "method": "POST", "auth_required": True, "roles": "User", "controller": "backend/main.py"},
        {"endpoint": "/history", "method": "GET", "auth_required": True, "roles": "User", "controller": "backend/main.py"},
        {"endpoint": "/analysis/{id}", "method": "GET", "auth_required": True, "roles": "User", "controller": "backend/main.py"},
        {"endpoint": "/history/{id}", "method": "DELETE", "auth_required": True, "roles": "User", "controller": "backend/main.py"},
        {"endpoint": "/doctors", "method": "GET", "auth_required": False, "roles": "Public", "controller": "backend/main.py"},
        {"endpoint": "/help", "method": "GET", "auth_required": False, "roles": "Public", "controller": "backend/main.py"},
        {"endpoint": "/privacy", "method": "GET", "auth_required": False, "roles": "Public", "controller": "backend/main.py"}
    ]
    
    findings = [
        {
            "id": "SEC-FIND-001",
            "severity": "Medium",
            "title": "Permissive CORS Policy (allow_origins=['*'])",
            "cwe": "CWE-942",
            "owasp": "A05:2021-Security Misconfiguration",
            "endpoint": "backend/main.py:57",
            "description": "CORS configuration allows requests from any origin. Recommended to restrict to trusted frontend origins."
        },
        {
            "id": "SEC-FIND-002",
            "severity": "Low",
            "title": "Local Reset Code Backup File Write",
            "cwe": "CWE-532",
            "owasp": "A09:2021-Security Logging and Monitoring Failures",
            "endpoint": "backend/main.py:173",
            "description": "Password reset code written to local plaintext file PASSWORD_RESET_CODE.txt for developer debugging."
        },
        {
            "id": "SEC-FIND-003",
            "severity": "Low",
            "title": "Missing Explicit Rate Limiting on /signup & /token",
            "cwe": "CWE-307",
            "owasp": "A07:2021-Identification and Authentication Failures",
            "endpoint": "/token",
            "description": "Authentication endpoints lack request rate limiting middleware."
        }
    ]
    
    excel_gen.generate_vulnerability_excel_reports(
        vuln_dir="Vulnerability Test Results",
        endpoint_list=endpoints,
        security_findings=findings,
        test_cases=sec_results,
        dep_vulns=[],
        perf_results=[]
    )
    
    # HTML
    html_gen = HTMLReportGenerator(output_dir="Test Results/HTML")
    html_gen.generate_all_reports(all_results)
    
    # JSON & Markdown
    jm_gen = JSONMarkdownGenerator(json_dir="Test Results/JSON", summary_dir="Test Results/Summary")
    jm_gen.generate_json_results(all_results)
    summary_md = jm_gen.generate_markdown_summary(all_results)
    
    # Copy generated HTML reports into Vulnerability Test Results as well
    os.makedirs("Vulnerability Test Results", exist_ok=True)
    shutil.copy("Test Results/HTML/execution-report.html", "Vulnerability Test Results/execution-report.html")
    shutil.copy("Test Results/HTML/dashboard.html", "Vulnerability Test Results/dashboard.html")
    
    # Write GITHUB_STEP_SUMMARY if running inside GitHub Actions
    gh_summary = os.getenv("GITHUB_STEP_SUMMARY")
    if gh_summary:
        with open(gh_summary, "a", encoding="utf-8") as f:
            f.write("\n" + summary_md + "\n")
            
    print("\n[SUCCESS] All automation reports generated successfully!")

if __name__ == "__main__":
    main()
