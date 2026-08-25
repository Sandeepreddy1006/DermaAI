from automation.pages.base_page import BasePage

class SignupPage(BasePage):
    FULL_NAME_INPUT = "#full_name, #et_full_name"
    EMAIL_INPUT = "#signup_email, #et_email"
    PASSWORD_INPUT = "#signup_password, #et_password"
    CONFIRM_PASSWORD_INPUT = "#confirm_password, #et_confirm_password"
    SIGNUP_BTN = "#btn_register, #btn_signup"

    def register_user(self, name, email, password):
        self.type_text("css selector", self.FULL_NAME_INPUT, name)
        self.type_text("css selector", self.EMAIL_INPUT, email)
        self.type_text("css selector", self.PASSWORD_INPUT, password)
        self.click("css selector", self.SIGNUP_BTN)
