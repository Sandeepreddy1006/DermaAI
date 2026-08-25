from automation.pages.base_page import BasePage

class LoginPage(BasePage):
    # Locators for Web & Mobile
    EMAIL_INPUT = "input[type='email'], #email_input, #et_email"
    PASSWORD_INPUT = "input[type='password'], #password_input, #et_password"
    LOGIN_BTN = "#login_button, #btn_login, button[type='submit']"
    SIGNUP_LINK = "#signup_link, #tv_signup"
    FORGOT_PASSWORD_LINK = "#forgot_password, #tv_forgot_password"
    ERROR_MSG = ".error-message, #tv_error, .toast-error"

    def enter_email(self, email):
        self.logger.info(f"Entering email: {email}")
        self.type_text("css selector", self.EMAIL_INPUT, email)

    def enter_password(self, password):
        self.logger.info("Entering password")
        self.type_text("css selector", self.PASSWORD_INPUT, password)

    def click_login(self):
        self.logger.info("Clicking Login button")
        self.click("css selector", self.LOGIN_BTN)

    def perform_login(self, email, password):
        self.enter_email(email)
        self.enter_password(password)
        self.click_login()
