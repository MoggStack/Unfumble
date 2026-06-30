from enum import Enum

from pydantic import BaseModel


class UseCase(str, Enum):
    passport = "passport"
    linkedin = "linkedin"
    instagram = "instagram"
    job_application = "job_application"


class JobStatus(str, Enum):
    pending = "PENDING"
    processing = "PROCESSING"
    done = "DONE"
    failed = "FAILED"


class CreateJobRequest(BaseModel):
    photo_url: str
    use_case: UseCase


class JobResponse(BaseModel):
    job_id: str
    use_case: UseCase
    status: JobStatus
    result_url: str | None = None
