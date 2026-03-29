// @ts-ignore
/* eslint-disable */
import request from "@/libs/request";

/** followUser POST /api/user_follow/follow */
export async function followUserUsingPost(
  body: API.UserFollowRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>("/api/user_follow/follow", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** unfollowUser POST /api/user_follow/unfollow */
export async function unfollowUserUsingPost(
  body: API.UserFollowRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>("/api/user_follow/unfollow", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** listFollowerUserVOByPage POST /api/user_follow/follower/list/page/vo */
export async function listFollowerUserVoByPageUsingPost(
  body: API.UserFollowQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageUserVO_>("/api/user_follow/follower/list/page/vo", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** listFollowingUserVOByPage POST /api/user_follow/following/list/page/vo */
export async function listFollowingUserVoByPageUsingPost(
  body: API.UserFollowQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageUserVO_>("/api/user_follow/following/list/page/vo", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}
