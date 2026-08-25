# 🛠️ Troubleshooting Guide

Common issues and resolution strategies for local execution and GitHub Actions CI runs.

---

## 1. UnicodeEncodeError on Windows Console
### Symptom
`UnicodeEncodeError: 'charmap' codec can't encode character...`
### Resolution
Use ASCII indicators (`[+]`, `[SUMMARY]`, `[SUCCESS]`) instead of raw multi-byte unicode emojis in standard stdout streams, or set the environment variable:
```cmd
set PYTHONIOENCODING=utf-8
```

---

## 2. GitHub Pages Deployment Error (404 Not Found)
### Symptom
Selenium tests fail with 404 HTTP status code when accessing `BASE_URL`.
### Resolution
1. Ensure GitHub Pages is enabled under **Repository Settings -> Pages**.
2. Select **Source**: `Deploy from a branch` and set branch to `gh-pages` / `/root`.
3. Verify `web_application/index.html` exists in the repository root or build output.

---

## 3. Appium Server Connection Timeout
### Symptom
`urllib3.exceptions.MaxRetryError: Failed to establish a new connection: [Errno 111] Connection refused`
### Resolution
Ensure Appium server is running on default port `4723`:
```bash
appium --allow-insecure chromedriver_autodownload
```
Or allow the runner to utilize the built-in simulated test adapter for CI environments without headless emulator hardware.

---

## 4. Excel Sheet Title Warning in OpenPyXL
### Symptom
`UserWarning: Title is more than 31 characters`
### Resolution
Sheet names in Excel workbooks are restricted to a maximum of 31 characters. Keep sheet titles under 30 characters (e.g., `Security & Functional Tests`).
