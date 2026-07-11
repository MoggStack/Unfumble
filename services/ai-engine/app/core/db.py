from collections.abc import AsyncGenerator

from sqlalchemy.ext.asyncio import AsyncSession, async_sessionmaker, create_async_engine
from sqlalchemy.orm import DeclarativeBase

from app.core.config import get_settings

engine = create_async_engine(get_settings().database_url)
async_session = async_sessionmaker(engine, expire_on_commit=False)


class Base(DeclarativeBase):
    pass


async def get_db() -> AsyncGenerator[AsyncSession, None]:
    async with async_session() as session:
        yield session

async def update_job(job_id: str, status: str, result_key: str | None = None) -> None:
    from app.models.orm import JobRecord
    async with async_session() as session:
        record = await session.get(JobRecord, job_id)
        if record:
            record.status = status
            if result_key:
                record.result_key = result_key
            await session.commit()
