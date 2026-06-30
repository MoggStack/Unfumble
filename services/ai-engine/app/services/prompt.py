from app.models.job import UseCase

_PROMPTS: dict[UseCase, str] = {
    UseCase.passport: (
        "Edit this photo into a passport-style headshot: plain white background, "
        "neutral expression, even lighting, centered crop from chest up."
    ),
    UseCase.linkedin: (
        "Edit this photo into a professional LinkedIn headshot: softly blurred "
        "office-style background, business-casual look, warm even lighting."
    ),
    UseCase.instagram: (
        "Edit this photo into a vibrant, well-lit Instagram-ready portrait with "
        "natural color grading and a flattering crop."
    ),
    UseCase.job_application: (
        "Edit this photo into a clean, professional job-application photo: neutral "
        "background, formal attire look, sharp focus, even studio lighting."
    ),
}


def build_prompt(use_case: UseCase) -> str:
    return _PROMPTS[use_case]
