from functools import lru_cache

import boto3
from botocore.exceptions import ClientError

from app.core.config import get_settings


@lru_cache
def _client():
    settings = get_settings()
    return boto3.client(
        "s3",
        endpoint_url=settings.s3_endpoint_url,
        aws_access_key_id=settings.s3_access_key,
        aws_secret_access_key=settings.s3_secret_key,
    )


def upload_bytes(key: str, data: bytes, content_type: str = "image/jpeg") -> None:
    _client().put_object(
        Bucket=get_settings().s3_bucket,
        Key=key,
        Body=data,
        ContentType=content_type,
    )


def presigned_url(key: str, expires_in: int = 3600) -> str:
    return _client().generate_presigned_url(
        "get_object",
        Params={"Bucket": get_settings().s3_bucket, "Key": key},
        ExpiresIn=expires_in,
    )


def ensure_bucket() -> None:
    bucket = get_settings().s3_bucket
    client = _client()
    try:
        client.head_bucket(Bucket=bucket)
    except (client.exceptions.NoSuchBucket, ClientError):
        try:
            client.create_bucket(Bucket=bucket)
        except ClientError:
            pass
