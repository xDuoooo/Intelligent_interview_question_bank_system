import { buildApiUrl } from "@/libs/request";

export const SOCIAL_AUTH_PROVIDERS = [
  {
    key: "gitee",
    label: "Gitee",
    iconSrc: "/assets/gitee-logo.png",
  },
  {
    key: "github",
    label: "GitHub",
    iconSrc: "/assets/github-logo.png",
  },
  {
    key: "google",
    label: "Google",
    iconSrc: "/assets/google-logo.png",
  },
] as const;

export type SocialAuthProviderKey = (typeof SOCIAL_AUTH_PROVIDERS)[number]["key"];

export function getSocialAuthUrl(provider: SocialAuthProviderKey, action?: "bind") {
  const search = action ? `?action=${action}` : "";
  return buildApiUrl(`/api/user/login/${provider}${search}`);
}

export function getSocialAuthProviderLabel(provider: SocialAuthProviderKey) {
  return SOCIAL_AUTH_PROVIDERS.find((item) => item.key === provider)?.label ?? provider;
}
