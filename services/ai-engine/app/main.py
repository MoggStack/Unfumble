from fastapi import FastAPI

from app.api.v1.routers import health

app = FastAPI(title="UnFumble AI Engine")

app.include_router(health.router, prefix="/api/v1")
