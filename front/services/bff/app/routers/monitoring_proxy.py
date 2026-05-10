from __future__ import annotations

import json
import logging
from typing import Annotated, Any

import httpx
from fastapi import APIRouter, Body, Depends, Request, Response
from fastapi.responses import JSONResponse

from app.config import Settings, get_settings
from app.errors import bff_error_response

_log = logging.getLogger("fins.bff.monitoring")

router = APIRouter(prefix="/monitoring", tags=["monitoring"])


def _monitoring_base(settings: Settings) -> str | None:
    value = (settings.monitoring_service_base_url or "").strip().rstrip("/")
    return value or None


def _forward_headers(request: Request) -> dict[str, str]:
    headers: dict[str, str] = {}
    for name in (
        "user-agent",
        "x-correlation-id",
        "x-span-id",
        "x-forwarded-for",
        "x-forwarded-host",
        "x-forwarded-proto",
    ):
        value = request.headers.get(name)
        if value:
            headers[name] = value
    return headers


def _upstream_error(body: bytes) -> str | dict[str, object]:
    if not body:
        return "Monitoring service error"
    try:
        parsed = json.loads(body.decode("utf-8"))
        if isinstance(parsed, dict):
            return parsed
    except (json.JSONDecodeError, UnicodeDecodeError):
        pass
    return body.decode("utf-8", errors="replace")


async def _post_monitoring(
    settings: Settings,
    request: Request,
    path: str,
    body: Any,
) -> Response | JSONResponse:
    base = _monitoring_base(settings)
    if base is None:
        return bff_error_response(
            503,
            message="Monitoring service is not configured",
            code="MONITORING_UNAVAILABLE",
        )

    url = f"{base}{path}"
    timeout = httpx.Timeout(settings.upstream_timeout_seconds)
    try:
        async with httpx.AsyncClient(timeout=timeout) as client:
            response = await client.post(
                url,
                headers=_forward_headers(request),
                json=body,
            )
    except httpx.RequestError as exc:
        _log.warning("monitoring upstream request failed: %s", exc)
        return bff_error_response(
            503,
            message="Monitoring service is unavailable",
            code="MONITORING_UNAVAILABLE",
        )

    content_type = response.headers.get("content-type", "application/json")
    if response.status_code >= 400:
        return bff_error_response(
            response.status_code,
            message=_upstream_error(response.content),
            code="MONITORING_UPSTREAM_ERROR",
        )
    return Response(
        content=response.content,
        status_code=response.status_code,
        media_type=content_type,
    )


@router.post("/metrics", response_model=None)
async def get_web_metrics(
    request: Request,
    settings: Annotated[Settings, Depends(get_settings)],
    body: Annotated[dict[str, Any], Body(...)],
) -> Response | JSONResponse:
    return await _post_monitoring(settings, request, "/metrics/web", body)


@router.post("/metrics/batch", response_model=None)
async def get_web_metrics_batch(
    request: Request,
    settings: Annotated[Settings, Depends(get_settings)],
    body: Annotated[list[dict[str, Any]], Body(...)],
) -> Response | JSONResponse:
    return await _post_monitoring(settings, request, "/metrics/web/batch", body)
