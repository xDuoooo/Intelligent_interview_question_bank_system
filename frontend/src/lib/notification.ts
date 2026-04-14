export function getNotificationTargetUrl(item?: {
  targetUrl?: string;
  type?: string;
  title?: string;
  content?: string;
  targetId?: string | number;
}) {
  if (!item) {
    return "/user/notifications";
  }
  if (item.targetUrl) {
    return item.targetUrl;
  }
  const type = item.type || "";
  const title = item.title || "";
  const content = item.content || "";
  const targetId = String(item.targetId ?? "").trim();
  const hasTargetId = /^[1-9]\d*$/.test(targetId);

  switch (type) {
    case "question_bank_review":
      if (title.includes("未通过") || content.includes("未通过")) {
        return "/user/center?tab=banks";
      }
      return hasTargetId ? `/bank/${targetId}` : "/user/center?tab=banks";
    case "post_review":
      if (title.includes("未通过") || content.includes("未通过")) {
        return "/user/center?tab=posts";
      }
      return hasTargetId ? `/post/${targetId}` : "/user/center?tab=posts";
    case "post_reply":
    case "post_comment_review":
      return hasTargetId ? `/post/${targetId}#post-comment-section` : "/user/notifications";
    case "question_review":
      if (title.includes("未通过") || content.includes("未通过")) {
        return "/user/center?tab=submission";
      }
      return hasTargetId ? `/question/${targetId}` : "/user/center?tab=submission";
    case "reply":
    case "like":
    case "comment_review":
      return hasTargetId ? `/question/${targetId}#comment-section` : "/user/notifications";
    case "user_follow":
      return hasTargetId ? `/user/${targetId}` : "/user/notifications";
    case "learning_goal_reminder":
      return "/user/center?tab=record";
    default:
      if (hasTargetId) {
        if (type.startsWith("post")) {
          return `/post/${targetId}`;
        }
        if (type.startsWith("question")) {
          return `/question/${targetId}`;
        }
      }
      return "/user/notifications";
  }
}
