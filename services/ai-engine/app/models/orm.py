from sqlalchemy.orm import Mapped, mapped_column

from app.core.db import Base


class JobRecord(Base):
    __tablename__ = "jobs"

    job_id: Mapped[str] = mapped_column(primary_key=True)
    use_case: Mapped[str]
    status: Mapped[str]
    result_key: Mapped[str | None] = mapped_column(default=None)
