import os
import time
import logging

class BasePage:
    def __init__(self, driver=None):
        self.driver = driver
        self.logger = logging.getLogger(self.__class__.__name__)
        logging.basicConfig(level=logging.INFO)

    def find_element(self, by, locator, timeout=10):
        if not self.driver:
            self.logger.info(f"[SIMULATED] Finding element by {by} = {locator}")
            return True
        try:
            from selenium.webdriver.support.ui import WebDriverWait
            from selenium.webdriver.support import expected_conditions as EC
            return WebDriverWait(self.driver, timeout).until(
                EC.presence_of_element_located((by, locator))
            )
        except Exception as e:
            self.logger.error(f"Element not found: {by}={locator}, Error: {e}")
            self.take_screenshot(f"element_not_found_{locator}")
            raise

    def click(self, by, locator):
        if not self.driver:
            self.logger.info(f"[SIMULATED] Clicked element {locator}")
            return
        el = self.find_element(by, locator)
        el.click()

    def type_text(self, by, locator, text):
        if not self.driver:
            self.logger.info(f"[SIMULATED] Typed '{text}' into {locator}")
            return
        el = self.find_element(by, locator)
        el.clear()
        el.send_keys(text)

    def get_text(self, by, locator):
        if not self.driver:
            return "Sample Text"
        el = self.find_element(by, locator)
        return el.text

    def take_screenshot(self, name):
        filename = f"Test Results/Screenshots/{name}_{int(time.time())}.png"
        os.makedirs("Test Results/Screenshots", exist_ok=True)
        if self.driver:
            try:
                self.driver.save_screenshot(filename)
                self.logger.info(f"Saved screenshot: {filename}")
            except Exception as e:
                self.logger.error(f"Failed to capture screenshot: {e}")
        else:
            with open(filename, "wb") as f:
                # 1x1 transparent PNG byte mock
                f.write(b'\x89PNG\r\n\x1a\n\x00\x00\x00\rIHDR\x00\x00\x00\x01\x00\x00\x00\x01\x08\x06\x00\x00\x00\x1f\x15c4\x00\x00\x00\nIDATx\x9cc`\x00\x00\x00\x02\x00\x01H\xaf\xa4q\x00\x00\x00\x00IEND\xaeB`\x82')
        return filename
