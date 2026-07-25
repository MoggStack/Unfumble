import asyncio
import logging

from app.core.celery_app import celery_app
from app.core.db import update_job
from app.models.job import JobStatus, UseCase, CreateJobRequest
from app.services.image_editor import edit_image
from app.services.prompt import build_prompt
from app.services.storage import upload_bytes

logger = logging.getLogger(__name__)


@celery_app.task(name="process_image_job")
def process_image_job(job_id: str, photo_url: str, use_case_value: str, metadata: dict | None = None) -> None:
    try:
        use_case = UseCase(use_case_value)
        request = CreateJobRequest(photo_url=photo_url, use_case=use_case, metadata=metadata)
        prompt_payload = build_prompt(request)
        
        # We pass the positive_prompt string to the current stub edit_image 
        # (you'll later update edit_image to accept the full payload)
        image_bytes = edit_image(photo_url, prompt_payload.positive_prompt)
        
        result_key = f"{job_id}.jpg"
        upload_bytes(result_key, image_bytes)
        
        asyncio.run(update_job(job_id, JobStatus.done.value, result_key))
    except Exception as e:
        logger.error(f"Failed to process job {job_id}: {e}")
        asyncio.run(update_job(job_id, JobStatus.failed.value))
