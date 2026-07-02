from contextlib import asynccontextmanager

from fastapi import FastAPI

import asyncio

from app.api.v1.routers import health, upload
from app.core.db import Base, engine
from app.models import orm  # noqa: F401 — registers JobRecord with Base.metadata
from app.services.storage import ensure_bucket


@asynccontextmanager
async def lifespan(app: FastAPI):
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    await asyncio.to_thread(ensure_bucket)
    yield


app = FastAPI(title="UnFumble AI Engine", lifespan=lifespan)

app.include_router(health.router, prefix="/api/v1")
app.include_router(upload.router, prefix="/api/v1")
