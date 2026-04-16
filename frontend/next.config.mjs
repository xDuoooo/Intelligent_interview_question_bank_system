const rawApiBaseUrl =
  process.env.NEXT_PUBLIC_API_BASE_URL ||
  process.env.API_BASE_URL ||
  "http://localhost:8101";

const apiBaseUrl = rawApiBaseUrl.replace(/\/$/, "");

/** @type {import('next').NextConfig} */
const nextConfig = {
    output: "standalone",
    images: {
        // Production container does not bundle sharp by default in standalone mode.
        // Disable Next.js runtime image optimization to avoid _next/image 500 errors.
        unoptimized: true,
        remotePatterns: [
            {
                protocol: 'https',
                hostname: 'gw.alipayobjects.com',
            },
            {
                protocol: 'https',
                hostname: 'avatars.githubusercontent.com',
            },
            {
                protocol: 'https',
                hostname: 'portrait.gitee.com',
            },
            {
                protocol: 'https',
                hostname: 'gitee.com',
            },
            {
                protocol: 'https',
                hostname: 'lh3.googleusercontent.com',
            }
        ],
    },
    async rewrites() {
        return [
            {
                source: "/api/:path*",
                destination: `${apiBaseUrl}/api/:path*`,
            },
        ];
    },
};

export default nextConfig;
