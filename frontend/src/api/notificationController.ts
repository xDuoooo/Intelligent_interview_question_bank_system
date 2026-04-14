// @ts-ignore
/* eslint-disable */
import request from '@/libs/request';

/** addNotification POST /api/notification/add */
export async function addNotificationUsingPost(
  body: API.NotificationAddRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong_>('/api/notification/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** adminSendNotification POST /api/notification/admin/send */
export async function adminSendNotificationUsingPost(
  body: Record<string, any>,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong_>('/api/notification/admin/send', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** deleteNotification POST /api/notification/delete */
export async function deleteNotificationUsingPost(
  body: API.DeleteRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/api/notification/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** updateNotification POST /api/notification/update */
export async function updateNotificationUsingPost(
  body: API.NotificationVO,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/api/notification/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getNotificationVOById GET /api/notification/get/vo */
export async function getNotificationVOByIdUsingGet(
  params: {
    // query
    /** id */
    id?: string | number;
  },
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseNotificationVO_>('/api/notification/get/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** listNotificationByPage POST /api/notification/list/page */
export async function listNotificationByPageUsingPost(
  body: API.NotificationQueryRequest,
  options?: { [key: string]: any },
) {
  return request<any>('/api/notification/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** listMyNotificationVOByPage POST /api/notification/my/list/page/vo */
export async function listMyNotificationVOByPageUsingPost(
  body: API.NotificationQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageNotificationVO_>('/api/notification/my/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** readAllNotification POST /api/notification/read/all */
export async function readAllNotificationUsingPost(options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean_>('/api/notification/read/all', {
    method: 'POST',
    ...(options || {}),
  });
}

/** readNotification POST /api/notification/read */
export async function readNotificationUsingPost(
  params: {
    // query
    /** id */
    id?: string | number;
  },
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/api/notification/read', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}
