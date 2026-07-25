from typing import Dict, Any

from app.models.job import UseCase

# Global positive keywords applied to all generations for high quality
BASE_MODIFIERS = (
    "8k resolution, highly detailed, photorealistic, raw photo, "
    "sharp focus, professional photography, Fujifilm XT4"
)

# Global negative keywords to ban bad generations
BASE_NEGATIVE_PROMPT = (
    "cartoon, illustration, 3d render, low resolution, deformed, bad anatomy, "
    "bad lighting, overexposed, underexposed, extra fingers, text, watermark, "
    "sunglasses, blurry, pixelated"
)

# Configuration for each UseCase (positive context and generation parameters)
USE_CASE_CONFIGS: Dict[UseCase, Dict[str, Any]] = {
    UseCase.linkedin: {
        "context": "cinematic office lighting, softly blurred modern office background, professional business attire, confident smile",
        "guidance_scale": 7.5,
        "num_inference_steps": 30,
    },
    UseCase.passport: {
        "context": "flat studio lighting, pure white background, neutral expression, shoulders squared to camera, no shadows on face, official document photo",
        "guidance_scale": 8.0,
        "num_inference_steps": 30,
    },
    UseCase.instagram: {
        "context": "golden hour natural lighting, vibrant colors, shallow depth of field, trendy aesthetic background, candid casual but stylish",
        "guidance_scale": 7.0,
        "num_inference_steps": 35,
    },
    UseCase.job_application: {
        "context": "bright even studio lighting, subtle grey seamless paper background, formal business attire, approachable but serious expression",
        "guidance_scale": 7.5,
        "num_inference_steps": 30,
    },
}
