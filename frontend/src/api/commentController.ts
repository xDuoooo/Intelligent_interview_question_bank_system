/**
 * 题目评论 API 封装
 * 手写封装，调用后端 /question/comment/** 接口
 */
import axios from "axios";

const BASE_URL = typeof window !== "undefined" ? "/api" : process.env.NEXT_PUBLIC_API_URL || "http://localhost:8101/api";

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
  hasLiked: boolean;
  replies: CommentVO[];
}

interface Page<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

const request = axios.create({ baseURL: BASE_URL, withCredentials: true });

export async function addComment(data: CommentAddRequest): Promise<number> {
  const res = await request.post("/question/comment/add", data);
  return res.data.data;
}

export async function deleteComment(id: number): Promise<boolean> {
  const res = await request.post("/question/comment/delete", { id });
  return res.data.data;
}

export async function listCommentsByPage(data: CommentQueryRequest): Promise<Page<CommentVO>> {
  const res = await request.post("/question/comment/list/page/vo", data);
  return res.data.data;
}

export async function likeComment(commentId: number): Promise<{ liked: boolean; likeNum: number }> {
  const res = await request.post(`/question/comment/like?commentId=${commentId}`);
  return res.data.data;
}

export async function reportComment(data: CommentReportRequest): Promise<boolean> {
  const res = await request.post("/question/comment/report", data);
  return res.data.data;
}

export async function pinComment(commentId: number, pinned: boolean): Promise<boolean> {
  const res = await request.post(`/question/comment/pin?commentId=${commentId}&pinned=${pinned}`);
  return res.data.data;
}

export async function setOfficialAnswer(commentId: number, official: boolean): Promise<boolean> {
  const res = await request.post(`/question/comment/official?commentId=${commentId}&official=${official}`);
  return res.data.data;
}
