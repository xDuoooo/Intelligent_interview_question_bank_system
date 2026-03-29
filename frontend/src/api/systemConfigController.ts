// @ts-ignore
/* eslint-disable */
import request from "@/libs/request";

/** getSystemConfig GET /api/system_config/get */
export async function getSystemConfigUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseSystemConfigVO_>("/api/system_config/get", {
    method: "GET",
    ...(options || {}),
  });
}

/** getPublicSystemConfig GET /api/system_config/public/get */
export async function getPublicSystemConfigUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseSystemConfigVO_>("/api/system_config/public/get", {
    method: "GET",
    ...(options || {}),
  });
}

/** updateSystemConfig POST /api/system_config/update */
export async function updateSystemConfigUsingPost(
  body: API.SystemConfigUpdateRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>("/api/system_config/update", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}
