// @ts-ignore
/* eslint-disable */
import request from '@/libs/request';

/** getGlobalLeaderboard GET /api/leaderboard/global */
export async function getGlobalLeaderboardUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseGlobalLeaderboardVO_>('/api/leaderboard/global', {
    method: 'GET',
    ...(options || {}),
  });
}

/** getQuestionBankLeaderboard GET /api/leaderboard/bank */
export async function getQuestionBankLeaderboardUsingGet(
  params: {
    questionBankId?: number;
  },
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseQuestionBankLeaderboardVO_>('/api/leaderboard/bank', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}
