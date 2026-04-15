// @ts-ignore
/* eslint-disable */
import request from '@/libs/request';

export type WxMpLoginTicketVO = {
  ticket?: string;
  keyword?: string;
  accountName?: string;
  qrImageUrl?: string;
  expireAt?: number;
};

export type WxMpLoginStatusVO = {
  status?: string;
  codeSent?: boolean;
  message?: string;
  expireAt?: number;
};

export type WxMpCodeLoginRequest = {
  code?: string;
};

export type BaseResponseWxMpLoginTicketVO = {
  code?: number;
  data?: WxMpLoginTicketVO;
  message?: string;
};

export type BaseResponseWxMpLoginStatusVO = {
  code?: number;
  data?: WxMpLoginStatusVO;
  message?: string;
};

/** check GET /api/ */
export async function checkUsingGet(
  params: API.checkUsingGETParams,
  options?: { [key: string]: any },
) {
  return request<string>('/api/', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** receiveMessage POST /api/ */
export async function receiveMessageUsingPost(options?: { [key: string]: any }) {
  return request<any>('/api/', {
    method: 'POST',
    ...(options || {}),
  });
}

/** setMenu GET /api/setMenu */
export async function setMenuUsingGet(options?: { [key: string]: any }) {
  return request<string>('/api/setMenu', {
    method: 'GET',
    ...(options || {}),
  });
}

/** createWxMpLoginTicket POST /api/wx/mp/login/ticket */
export async function createWxMpLoginTicketUsingPost(options?: { [key: string]: any }) {
  return request<BaseResponseWxMpLoginTicketVO>('/api/wx/mp/login/ticket', {
    method: 'POST',
    ...(options || {}),
  });
}

/** getWxMpLoginStatus GET /api/wx/mp/login/status */
export async function getWxMpLoginStatusUsingGet(options?: { [key: string]: any }) {
  return request<BaseResponseWxMpLoginStatusVO>('/api/wx/mp/login/status', {
    method: 'GET',
    ...(options || {}),
  });
}

/** createWxMpBindTicket POST /api/wx/mp/bind/ticket */
export async function createWxMpBindTicketUsingPost(options?: { [key: string]: any }) {
  return request<BaseResponseWxMpLoginTicketVO>('/api/wx/mp/bind/ticket', {
    method: 'POST',
    ...(options || {}),
  });
}

/** getWxMpBindStatus GET /api/wx/mp/bind/status */
export async function getWxMpBindStatusUsingGet(options?: { [key: string]: any }) {
  return request<BaseResponseWxMpLoginStatusVO>('/api/wx/mp/bind/status', {
    method: 'GET',
    ...(options || {}),
  });
}

/** loginByWxMpCode POST /api/wx/mp/login/code */
export async function loginByWxMpCodeUsingPost(
  body: WxMpCodeLoginRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLoginUserVO_>('/api/wx/mp/login/code', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** bindByWxMpCode POST /api/wx/mp/bind/code */
export async function bindByWxMpCodeUsingPost(
  body: WxMpCodeLoginRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLoginUserVO_>('/api/wx/mp/bind/code', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}
