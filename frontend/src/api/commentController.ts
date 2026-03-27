/**
 * 题目评论 API 封装
 */
import request from "@/libs/request";

interface CommentAddRequest {
  questionId: number;
  parentId?: number | null;
  replyToId?: number | null;
  content: string;
}

interface CommentQueryRequest {
  questionId: number;
  current?: number;
  pageSize?: number;
  sortField?: "createTime" | "likeNum";
  sortOrder?: "ascend" | "descend";
}

interface CommentReportRequest {
  commentId: number;
  reason: string;
}

interface UserVO {
  id: number;
  userName: string;
  userAvatar: string;
  userProfile?: string;
  userRole: string;
}

export interface CommentVO {
  id: number;
  questionId: number;
  parentId?: number | null;
  replyToId?: number | null;
  content: string;
  likeNum: number;
  isPinned: number;
  isOfficial: number;
  status: number;
  createTime: string;
  deleted: boolean;
  user: UserVO | null;
  replyToUser?: UserVO | null;
  hasLiked: boolean;
  replies: CommentVO[];
}

interface Page<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

/**
 * 注意：request (myAxios) 的响应拦截器已经返回了 data (BaseResponse)
 * 我们进一步提取出 BaseResponse 中的数据部分返回给组件
 */

export async function addComment(data: CommentAddRequest): Promise<number> {
  const res = (await request.post("/api/question/comment/add", data)) as any;
  return res.data;
}

export async function deleteComment(id: number): Promise<boolean> {
  const res = (await request.post("/api/question/comment/delete", { id })) as any;
  return res.data;
}

export async function listCommentsByPage(data: CommentQueryRequest): Promise<Page<CommentVO>> {
  const res = (await request.post("/api/question/comment/list/page/vo", data)) as any;
  return res.data;
}

export async function likeComment(commentId: number): Promise<{ liked: boolean; likeNum: number }> {
  const res = (await request.post(`/api/question/comment/like?commentId=${commentId}`)) as any;
  return res.data;
}

export async function reportComment(data: CommentReportRequest): Promise<boolean> {
  const res = (await request.post("/api/question/comment/report", data)) as any;
  return res.data;
}

export async function pinComment(commentId: number, pinned: boolean): Promise<boolean> {
  const res = (await request.post(`/api/question/comment/pin?commentId=${commentId}&pinned=${pinned}`)) as any;
  return res.data;
}

export async function setOfficialAnswer(commentId: number, official: boolean): Promise<boolean> {
  const res = (await request.post(`/api/question/comment/official?commentId=${commentId}&official=${official}`)) as any;
  return res.data;
}
