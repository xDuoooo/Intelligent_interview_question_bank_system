// @ts-ignore
/* eslint-disable */
import request from '@/libs/request';

/** listAlertByPage POST /api/security_alert/admin/list/page */
export async function listAlertByPageUsingPost(
  body: API.SecurityAlertQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageSecurityAlert_>('/api/security_alert/admin/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** banUserByAlert POST /api/security_alert/admin/ban */
export async function banUserByAlertUsingPost(
  body: API.SecurityAlertHandleRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/api/security_alert/admin/ban', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** ignoreAlert POST /api/security_alert/admin/ignore */
export async function ignoreAlertUsingPost(
  body: API.SecurityAlertHandleRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/api/security_alert/admin/ignore', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}
