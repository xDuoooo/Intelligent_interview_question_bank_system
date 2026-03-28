// @ts-ignore
/* eslint-disable */
import request from '@/libs/request';

/** getDashboardOverview GET /api/admin/dashboard/overview */
export async function getDashboardOverviewUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseMapStringObject_>('/api/admin/dashboard/overview', {
    method: 'GET',
    ...(options || {}),
  });
}
