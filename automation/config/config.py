import os

class Config:
    # Base Live Application URL for Selenium Testing
    BASE_URL = os.getenv("BASE_URL", "https://Sandeepreddy1006.github.io/DermaAI/")
    
    # Backend API URL
    API_URL = os.getenv("API_URL", "http://localhost:8000")
    
    # Appium Capability Configurations
    APPIUM_SERVER_URL = os.getenv("APPIUM_SERVER_URL", "http://localhost:4723/wd/hub")
    ANDROID_PLATFORM_NAME = "Android"
    ANDROID_AUTOMATION_NAME = "UiAutomator2"
    ANDROID_DEVICE_NAME = os.getenv("ANDROID_DEVICE_NAME", "Android Emulator")
    ANDROID_APP_PATH = os.getenv("ANDROID_APP_PATH", "app/build/outputs/apk/debug/app-debug.apk")
    ANDROID_APP_PACKAGE = "com.simats.dermacareai"
    ANDROID_APP_ACTIVITY = "com.simats.dermacareai.MainActivity"
    
    # Default User Credentials for Automated E2E Testing
    TEST_USER_EMAIL = os.getenv("TEST_USER_EMAIL", "automation_user@dermacare.ai")
    TEST_USER_PASSWORD = os.getenv("TEST_USER_PASSWORD", "ap21bd2383")
    TEST_USER_NAME = "Automation QA Specialist"
    
    # Thresholds
    PASS_PERCENTAGE_THRESHOLD = 95.0
    CRITICAL_FAILURE_MAX = 0.05
    
    # Retention
    ARTIFACT_RETENTION_DAYS = 30
