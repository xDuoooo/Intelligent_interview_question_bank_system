// @ts-ignore
/* eslint-disable */
import { buildApiUrl } from '@/libs/request';
import request from '@/libs/request';

type StreamEventCallback = (event: string, payload: any) => void | Promise<void>;

function parseSseBlock(block: string) {
  let event = 'message';
  const dataLines: string[] = [];
  block
    .split('\n')
    .map((line) => line.trimEnd())
    .forEach((line) => {
      if (!line || line.startsWith(':')) {
        return;
      }
      if (line.startsWith('event:')) {
        event = line.slice(6).trim();
        return;
      }
      if (line.startsWith('data:')) {
        dataLines.push(line.slice(5).trim());
      }
    });
  const dataText = dataLines.join('\n');
  let payload: any = dataText;
  if (dataText) {
    try {
      payload = JSON.parse(dataText);
    } catch {
      payload = dataText;
    }
  }
  return { event, payload };
}

/** addMockInterview POST /api/mockInterview/add */
export async function addMockInterviewUsingPost(
  body: API.MockInterviewAddRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong_>('/api/mockInterview/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** deleteMockInterview POST /api/mockInterview/delete */
export async function deleteMockInterviewUsingPost(
  body: API.DeleteRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/api/mockInterview/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getMockInterviewById GET /api/mockInterview/get */
export async function getMockInterviewByIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getMockInterviewByIdUsingGETParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseMockInterview_>('/api/mockInterview/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** handleMockInterviewEvent POST /api/mockInterview/handleEvent */
export async function handleMockInterviewEventUsingPost(
  body: API.MockInterviewEventRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseString_>('/api/mockInterview/handleEvent', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

export async function streamMockInterviewEventUsingPost(
  body: API.MockInterviewEventRequest,
  onEvent: StreamEventCallback,
) {
  const response = await fetch(buildApiUrl('/api/mockInterview/stream/handleEvent'), {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Accept': 'text/event-stream',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(body),
  });
  const contentType = response.headers.get('content-type') || '';
  if (!response.ok || !response.body) {
    throw new Error('流式面试连接失败');
  }
  if (contentType.includes('application/json')) {
    const json = await response.json();
    throw new Error(json?.message || '流式面试处理失败');
  }
  const reader = response.body.getReader();
  const decoder = new TextDecoder('utf-8');
  let buffer = '';

  while (true) {
    const { value, done } = await reader.read();
    buffer += decoder.decode(value || new Uint8Array(), { stream: !done });
    let delimiterIndex = buffer.indexOf('\n\n');
    while (delimiterIndex >= 0) {
      const block = buffer.slice(0, delimiterIndex).replace(/\r/g, '');
      buffer = buffer.slice(delimiterIndex + 2);
      if (block.trim()) {
        const parsed = parseSseBlock(block);
        await onEvent(parsed.event, parsed.payload);
      }
      delimiterIndex = buffer.indexOf('\n\n');
    }
    if (done) {
      break;
    }
  }
  if (buffer.trim()) {
    const parsed = parseSseBlock(buffer.replace(/\r/g, ''));
    await onEvent(parsed.event, parsed.payload);
  }
}

export async function downloadMockInterviewReviewUsingGet(id: number) {
  const response = await fetch(buildApiUrl(`/api/mockInterview/export?id=${id}`), {
    method: 'GET',
    credentials: 'include',
  });
  const contentType = response.headers.get('content-type') || '';
  if (!response.ok) {
    throw new Error('导出复盘失败');
  }
  if (contentType.includes('application/json')) {
    const json = await response.json();
    throw new Error(json?.message || '导出复盘失败');
  }
  const blob = await response.blob();
  const disposition = response.headers.get('content-disposition') || '';
  const matched = disposition.match(/filename\*=UTF-8''([^;]+)/);
  const fileName = matched?.[1] ? decodeURIComponent(matched[1]) : `mock-interview-${id}-review.md`;
  return { blob, fileName };
}

/** listMockInterviewByPage POST /api/mockInterview/list/page */
export async function listMockInterviewByPageUsingPost(
  body: API.MockInterviewQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageMockInterview_>('/api/mockInterview/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** listMockInterviewVOByPage POST /api/mockInterview/my/list/page/vo */
export async function listMockInterviewVoByPageUsingPost(
  body: API.MockInterviewQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageMockInterview_>('/api/mockInterview/my/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}
