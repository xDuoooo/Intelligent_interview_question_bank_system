// @ts-ignore
/* eslint-disable */
import request from '@/libs/request';

/** addPost POST /api/post/add */
export async function addPostUsingPost(body: API.PostAddRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseLong_>('/api/post/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** deletePost POST /api/post/delete */
export async function deletePostUsingPost(
  body: API.DeleteRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/api/post/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** editPost POST /api/post/edit */
export async function editPostUsingPost(
  body: API.PostEditRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/api/post/edit', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getPostVOById GET /api/post/get/vo */
export async function getPostVoByIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getPostVOByIdUsingGETParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePostVO_>('/api/post/get/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** listHotPost GET /api/post/hot/list */
export async function listHotPostUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseListPostVO_>('/api/post/hot/list', {
    method: 'GET',
    ...(options || {}),
  });
}

/** listFeaturedPost GET /api/post/featured/list */
export async function listFeaturedPostUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseListPostVO_>('/api/post/featured/list', {
    method: 'GET',
    ...(options || {}),
  });
}

/** listRelatedPost GET /api/post/related/list */
export async function listRelatedPostUsingGet(
  params: {
    postId?: number;
    size?: number;
  },
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseListPostVO_>('/api/post/related/list', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** operatePost POST /api/post/operate */
export async function operatePostUsingPost(
  body: API.PostOperateRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/api/post/operate', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** listPostByPage POST /api/post/list/page */
export async function listPostByPageUsingPost(
  body: API.PostQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePagePost_>('/api/post/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** listPostVOByPage POST /api/post/list/page/vo */
export async function listPostVoByPageUsingPost(
  body: API.PostQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePagePostVO_>('/api/post/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** listMyPostVOByPage POST /api/post/my/list/page/vo */
export async function listMyPostVoByPageUsingPost(
  body: API.PostQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePagePostVO_>('/api/post/my/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** searchPostVOByPage POST /api/post/search/page/vo */
export async function searchPostVoByPageUsingPost(
  body: API.PostQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePagePostVO_>('/api/post/search/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** reviewPost POST /api/post/review */
export async function reviewPostUsingPost(
  body: API.PostReviewRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/api/post/review', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** reportPost POST /api/post/report */
export async function reportPostUsingPost(
  body: API.PostReportRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/api/post/report', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** listPostReportVOByPage POST /api/post/report/list/page */
export async function listPostReportVoByPageUsingPost(
  body: API.PostReportQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePagePostReportVO_>('/api/post/report/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** processPostReport POST /api/post/report/process */
export async function processPostReportUsingPost(
  body: API.PostReportProcessRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/api/post/report/process', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** updatePost POST /api/post/update */
export async function updatePostUsingPost(
  body: API.PostUpdateRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/api/post/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}
