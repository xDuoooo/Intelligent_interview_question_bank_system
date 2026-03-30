// @ts-ignore
/* eslint-disable */
import request from '@/libs/request';

/** saveMyNote POST /api/user_question_note/save */
export async function saveMyNoteUsingPost(
  body: API.UserQuestionNoteSaveRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/api/user_question_note/save', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getMyNote GET /api/user_question_note/get/my */
export async function getMyNoteUsingGet(
  params: {
    questionId?: string | number;
  },
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseUserQuestionNoteVO_>('/api/user_question_note/get/my', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** deleteMyNote POST /api/user_question_note/delete/my */
export async function deleteMyNoteUsingPost(
  body: API.UserQuestionNoteSaveRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/api/user_question_note/delete/my', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** listMyNoteByPage POST /api/user_question_note/my/list/page */
export async function listMyNoteByPageUsingPost(
  body: API.UserQuestionNoteQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageUserQuestionNoteVO_>('/api/user_question_note/my/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}
