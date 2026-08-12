import io
import uuid

from fastapi import APIRouter, File, Form, HTTPException, UploadFile
from pydantic import BaseModel

from app.models.job import UseCase
from app.services.image_editor import edit_image
from app.services.prompt import build_prompt
from app.services.storage import presigned_url, upload_bytes

router = APIRouter(prefix="/process", tags=["process"])


class ProcessResponse(BaseModel):
    job_id: str
    result_url: str
    use_case: str


@router.post("", response_model=ProcessResponse)
async def process_image(
    file: UploadFile = File(..., description="Photo to transform"),
    use_case: UseCase = Form(..., description="Target use-case: passport | linkedin | instagram | job_application"),
) -> ProcessResponse:
    """
    Direct endpoint: upload a photo + pick a use-case → get an AI-edited image back.
    This does the full flow synchronously (no Celery needed for quick testing).
    """
    # 1. Read uploaded file
    content_type = file.content_type or "image/jpeg"
    image_bytes = await file.read()
    if not image_bytes:
        raise HTTPException(status_code=400, detail="Uploaded file is empty")

    # 2. Store original in MinIO so we have a URL to pass around
    job_id = str(uuid.uuid4())
    source_key = f"source/{job_id}{_ext(content_type)}"
    upload_bytes(source_key, image_bytes, content_type)
    source_url = presigned_url(source_key)

    # 3. Build prompt and call Gemini
    prompt = build_prompt(use_case)
    try:
        result_bytes = edit_image(source_url, prompt)
    except Exception as exc:
        raise HTTPException(status_code=502, detail=f"AI engine error: {exc}") from exc

    # 4. Store result in MinIO and return a pre-signed URL
    result_key = f"result/{job_id}.jpg"
    upload_bytes(result_key, result_bytes, "image/jpeg")
    result_url = presigned_url(result_key)

    return ProcessResponse(job_id=job_id, result_url=result_url, use_case=use_case.value)


def _ext(content_type: str) -> str:
    return {
        "image/jpeg": ".jpg",
        "image/png": ".png",
        "image/webp": ".webp",
        "image/gif": ".gif",
    }.get(content_type, ".jpg")
