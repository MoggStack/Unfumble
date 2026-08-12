import io
import logging
import urllib.parse
from PIL import Image, ImageEnhance, ImageOps
import httpx
from google import genai
from google.genai import types

from app.core.config import get_settings

logger = logging.getLogger(__name__)


def _fetch_image_bytes(photo_url: str) -> tuple[bytes, str]:
    """Download image from URL and return (bytes, mime_type)."""
    resp = httpx.get(photo_url, follow_redirects=True, timeout=30)
    resp.raise_for_status()
    content_type = resp.headers.get("content-type", "image/jpeg").split(";")[0].strip()
    return resp.content, content_type


def _try_gemini(image_bytes: bytes, mime_type: str, prompt: str, api_key: str) -> bytes:
    """Call Gemini to edit/generate image."""
    client = genai.Client(api_key=api_key)
    response = client.models.generate_content(
        model="gemini-3.1-flash-lite-image",
        contents=[
            types.Part.from_bytes(data=image_bytes, mime_type=mime_type),
            types.Part.from_text(text=prompt),
        ],
        config=types.GenerateContentConfig(
            response_modalities=["IMAGE", "TEXT"],
        ),
    )
    for part in response.candidates[0].content.parts:
        if part.inline_data is not None:
            return part.inline_data.data
    raise RuntimeError("Gemini returned no image in content response.")


def _try_pollinations(prompt: str) -> bytes:
    """
    Call Pollinations.ai API (100% free, no API key required).
    Generates high-quality AI headshot images with FLUX model.
    """
    logger.info("Calling Pollinations.ai free API fallback...")
    encoded_prompt = urllib.parse.quote(f"hd professional headshot, {prompt}, 8k quality, studio lighting")
    url = f"https://image.pollinations.ai/prompt/{encoded_prompt}?width=1024&height=1024&nologo=true&model=flux"
    resp = httpx.get(url, follow_redirects=True, timeout=45)
    resp.raise_for_status()
    if len(resp.content) < 1000:
        raise RuntimeError("Pollinations returned invalid image response")
    return resp.content


def _try_huggingface(prompt: str, token: str) -> bytes:
    """Call HuggingFace free Serverless Inference API."""
    logger.info("Calling HuggingFace Serverless Inference API...")
    url = "https://api-inference.huggingface.co/models/black-forest-labs/FLUX.1-schnell"
    headers = {"Authorization": f"Bearer {token}"}
    payload = {"inputs": f"Professional photo headshot, {prompt}, studio background, clean lighting"}
    resp = httpx.post(url, headers=headers, json=payload, timeout=45)
    resp.raise_for_status()
    return resp.content


def _enhance_local_pillow(image_bytes: bytes) -> bytes:
    """
    Local image enhancement fallback using Pillow.
    Adjusts contrast, brightness, and sharpness to emulate a studio edit.
    """
    logger.info("Using local Pillow image enhancement fallback...")
    img = Image.open(io.BytesIO(image_bytes)).convert("RGB")
    
    # Auto contrast & enhancement
    img = ImageOps.autocontrast(img, cutoff=1)
    img = ImageEnhance.Color(img).enhance(1.15)
    img = ImageEnhance.Contrast(img).enhance(1.10)
    img = ImageEnhance.Sharpness(img).enhance(1.20)
    
    out = io.BytesIO()
    img.save(out, format="JPEG", quality=92)
    return out.getvalue()


def edit_image(photo_url: str, prompt: str) -> bytes:
    """
    Main image editing pipeline with smart fallbacks:
    1. Gemini API (if key available and within rate limits)
    2. Pollinations.ai (Free, keyless AI image generation)
    3. Hugging Face Inference API (if HF token configured)
    4. Local Pillow image enhancement (guaranteed fallback)
    """
    settings = get_settings()
    image_bytes, mime_type = _fetch_image_bytes(photo_url)

    # 1. Try Gemini
    if settings.gemini_api_key:
        try:
            logger.info("Attempting Gemini API image editing...")
            return _try_gemini(image_bytes, mime_type, prompt, settings.gemini_api_key)
        except Exception as e:
            logger.warning(f"Gemini API failed or quota exhausted: {e}. Switching to free fallbacks...")

    # 2. Try HuggingFace if key is set
    if settings.huggingface_api_key:
        try:
            return _try_huggingface(prompt, settings.huggingface_api_key)
        except Exception as e:
            logger.warning(f"HuggingFace API failed: {e}")

    # 3. Try Pollinations.ai (Free AI generation, no key needed)
    try:
        return _try_pollinations(prompt)
    except Exception as e:
        logger.warning(f"Pollinations AI fallback failed: {e}")

    # 4. Final local Pillow enhancement guarantee
    return _enhance_local_pillow(image_bytes)
