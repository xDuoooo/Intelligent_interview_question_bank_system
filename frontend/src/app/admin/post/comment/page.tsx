import { redirect } from "next/navigation";

export default function AdminPostCommentRedirectPage() {
  redirect("/admin/post?view=replies");
}
