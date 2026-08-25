# 🔄 CI/CD Execution Guide

This guide details how the GitHub Actions pipeline runs automatically on every code push, pull request, schedule, or manual workflow dispatch.

---

## Pipeline Workflow Triggers (`.github/workflows/unified-e2e-pipeline.yml`)
- `push`: Triggered on every commit pushed to `main` or `master`.
- `pull_request`: Triggered on pull requests to validate code changes before merging.
- `workflow_dispatch`: Enables manual execution with custom parameters from the GitHub Actions UI.
- `schedule`: Runs nightly at 00:00 UTC.

---

## 21-Stage Automated Execution Flow

1. **Checkout Repository**: Pulls latest codebase and commit history.
2. **Setup JDK**: Configures Java 17 for Android build tools.
3. **Setup Python**: Initializes Python 3.11 with pip caching.
4. **Install Dependencies**: Installs `selenium`, `Appium-Python-Client`, `openpyxl`, `fastapi`, `uvicorn`, etc.
5. **Build APK**: Compiles `app-debug.apk` in Gradle.
6. **Enable KVM**: Enables hardware acceleration for Android Emulators in Linux runner.
7. **Verify Emulator Readiness**: Verifies ADB and Appium drivers.
8. **SAST Scan**: Runs Semgrep and static code security analyzers.
9. **Launch Backend**: Spins up FastAPI uvicorn background service.
10. **Deploy to GitHub Pages**: Publishes `./web_application` frontend.
11. **Verify Live Deployment**: Sends HTTP request to `https://Sandeepreddy1006.github.io/DermaAI/` to confirm HTTP 200.
12. **k6 Load Test**: Runs baseline 100 VU load test against API endpoints.
13. **Execute Master Test Suite**: Runs 1,400+ Appium, Selenium, and Security test cases via `run_all_tests.py`.
14. **Archive Historical Reports**: Updates `reports/latest/` and `reports/history/build-N/` on `gh-pages`.
15. **Upload Artifacts**: Uploads `Test Results/` and `Vulnerability Test Results/` as downloadable GitHub artifacts (30-day retention).
16. **Publish Step Summary**: Writes `summary.md` directly into GitHub Actions run summary UI ($GITHUB_STEP_SUMMARY).
