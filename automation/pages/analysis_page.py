from automation.pages.base_page import BasePage

class AnalysisPage(BasePage):
    FILE_INPUT = "input[type='file'], #file_upload"
    ANALYZE_BTN = "#btn_analyze, #btn_upload_image"
    RESULT_TITLE = ".result-title, #tv_result_title"
    CONFIDENCE_SCORE = ".confidence-score, #tv_confidence"
    PRECAUTIONS_SECTION = ".precautions-container, #tv_precautions"
    FIRST_AID_SECTION = ".first-aid-container, #tv_first_aid"

    def upload_and_analyze(self, image_path):
        self.type_text("css selector", self.FILE_INPUT, image_path)
        self.click("css selector", self.ANALYZE_BTN)
