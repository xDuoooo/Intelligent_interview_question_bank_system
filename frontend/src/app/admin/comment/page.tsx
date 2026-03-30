import { redirect } from "next/navigation";

export default function AdminCommentRedirectPage() {
  redirect("/admin/question?view=comments");
}
