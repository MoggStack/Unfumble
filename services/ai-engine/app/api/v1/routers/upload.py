import uuid

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.db import get_db
from app.models.job import CreateJobRequest, JobResponse, JobStatus
from app.models.orm import JobRecord
from app.services.storage import presigned_url

router = APIRouter(prefix="/jobs", tags=["jobs"])


def _to_response(record: JobRecord) -> JobResponse:
    result_url = presigned_url(record.result_key) if record.result_key else None
    return JobResponse(
        job_id=record.job_id,
        use_case=record.use_case,
        status=record.status,
        result_url=result_url,
    )


@router.post("", response_model=JobResponse, status_code=201)
async def create_job(payload: CreateJobRequest, db: AsyncSession = Depends(get_db)) -> JobResponse:
    job_id = str(uuid.uuid4())

    record = JobRecord(
        job_id=job_id,
        use_case=payload.use_case.value,
        status=JobStatus.pending.value,
    )
    db.add(record)
    await db.commit()

    from app.workers.tasks import process_image_job
    process_image_job.delay(job_id, payload.photo_url, payload.use_case.value)

    return _to_response(record)


@router.get("/{job_id}", response_model=JobResponse)
async def get_job(job_id: str, db: AsyncSession = Depends(get_db)) -> JobResponse:
    record = await db.get(JobRecord, job_id)
    if record is None:
        raise HTTPException(status_code=404, detail="job not found")
    return _to_response(record)
