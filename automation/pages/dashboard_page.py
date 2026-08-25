from automation.pages.base_page import BasePage

class DashboardPage(BasePage):
    WELCOME_HEADER = ".welcome-text, #tv_welcome"
    HEALTH_SCORE_CARD = ".health-score, #cv_health_score"
    SCAN_SKIN_BTN = "#btn_scan_skin, .btn-scan"
    NAV_DOCTORS = "#nav_doctors, #btn_doctors"
    NAV_HISTORY = "#nav_history, #btn_history"
    NAV_PROFILE = "#nav_profile, #btn_profile"

    def navigate_to_scan(self):
        self.click("css selector", self.SCAN_SKIN_BTN)

    def navigate_to_doctors(self):
        self.click("css selector", self.NAV_DOCTORS)

    def navigate_to_history(self):
        self.click("css selector", self.NAV_HISTORY)

    def navigate_to_profile(self):
        self.click("css selector", self.NAV_PROFILE)
