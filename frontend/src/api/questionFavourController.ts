// @ts-ignore
/* eslint-disable */
import request from '@/libs/request';

/** doQuestionFavour POST /api/question_favour/ */
export async function doQuestionFavourUsingPost(
  body: API.QuestionFavourAddRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseInt_>('/api/question_favour/', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}
