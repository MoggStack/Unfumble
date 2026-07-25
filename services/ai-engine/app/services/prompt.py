from pydantic import BaseModel

from app.models.job import UseCase, CreateJobRequest
from app.services.prompt_templates import (
    BASE_MODIFIERS,
    BASE_NEGATIVE_PROMPT,
    USE_CASE_CONFIGS,
)


class GenerationPayload(BaseModel):
    positive_prompt: str
    negative_prompt: str
    guidance_scale: float
    num_inference_steps: int


class PromptBuilder:
    @staticmethod
    def build_payload(request: CreateJobRequest) -> GenerationPayload:
        config = USE_CASE_CONFIGS[request.use_case]

        # Extract context and parameters
        context = config["context"]
        guidance_scale = config["guidance_scale"]
        num_inference_steps = config["num_inference_steps"]

        # If we have dynamic metadata (e.g., gender, outfit), inject it here
        subject = "A professional person"
        if request.metadata:
            # Example metadata usage
            gender = request.metadata.get("gender", "person")
            outfit = request.metadata.get("outfit", "")
            subject = f"A professional {gender}"
            if outfit:
                subject += f" wearing a {outfit}"

        # Combine subject, context, and base modifiers for the final prompt
        positive_prompt = f"{subject}, {context}, {BASE_MODIFIERS}"

        return GenerationPayload(
            positive_prompt=positive_prompt,
            negative_prompt=BASE_NEGATIVE_PROMPT,
            guidance_scale=guidance_scale,
            num_inference_steps=num_inference_steps,
        )


def build_prompt(request: CreateJobRequest) -> GenerationPayload:
    """Convenience function to build the prompt payload"""
    return PromptBuilder.build_payload(request)
