export function getNotificationTargetUrl(item?: {
  targetUrl?: string;
  type?: string;
  title?: string;
  content?: string;
  targetId?: number;
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
  const targetId = Number(item.targetId || 0);

  switch (type) {
    case "post_review":
      if (title.includes("未通过") || content.includes("未通过")) {
        return "/user/center?tab=posts";
      }
      return targetId > 0 ? `/post/${targetId}` : "/user/center?tab=posts";
    case "question_review":
      if (title.includes("未通过") || content.includes("未通过")) {
        return "/user/center?tab=submission";
      }
      return targetId > 0 ? `/question/${targetId}` : "/user/center?tab=submission";
    case "reply":
    case "like":
    case "comment_review":
      return targetId > 0 ? `/question/${targetId}#comment-section` : "/user/notifications";
    case "user_follow":
      return targetId > 0 ? `/user/${targetId}` : "/user/notifications";
    case "learning_goal_reminder":
      return "/user/center?tab=record";
    default:
      if (targetId > 0) {
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
