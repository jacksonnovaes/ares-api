import { cookies } from "next/headers";
import { NextRequest, NextResponse } from "next/server";

const API_URL = (process.env.API_URL ?? "http://localhost:8080").replace(/\/$/, "");
const ACCESS_COOKIE = "ares_access_token";
const REFRESH_COOKIE = "ares_refresh_token";

type RouteContext = { params: Promise<{ path: string[] }> };

const allowedPaths = [
  /^auth\/(login|refresh|logout|forgot-password|reset-password|change-password|me)$/,
  /^branding$/,
  /^tenants\/register$/,
  /^users(?:\/[0-9a-f-]+\/status)?$/i,
  /^customers(?:\/[0-9a-f-]+)?$/i,
  /^assets(?:\/[0-9a-f-]+)?$/i,
  /^services(?:\/[0-9a-f-]+)?$/i,
  /^service-orders(?:\/[0-9a-f-]+(?:\/status)?)?$/i,
];

function cookieOptions(maxAge: number) {
  return {
    httpOnly: true,
    sameSite: "lax" as const,
    secure: process.env.NODE_ENV === "production",
    path: "/",
    maxAge,
  };
}

async function upstream(
  request: NextRequest,
  path: string,
  accessToken?: string,
  overrideBody?: string,
) {
  const target = new URL(`${API_URL}/api/v1/${path}`);
  request.nextUrl.searchParams.forEach((value, key) => target.searchParams.append(key, value));
  const headers = new Headers({ Accept: "application/json" });
  const contentType = request.headers.get("content-type");
  if (contentType) headers.set("Content-Type", contentType);
  if (accessToken) headers.set("Authorization", `Bearer ${accessToken}`);

  const hasBody = !["GET", "HEAD"].includes(request.method);
  const body = overrideBody ?? (hasBody ? await request.text() : undefined);
  return fetch(target, {
    method: request.method,
    headers,
    body: body || undefined,
    cache: "no-store",
  });
}

async function copyResponse(response: Response) {
  const body = response.status === 204 ? null : await response.arrayBuffer();
  const headers = new Headers();
  const contentType = response.headers.get("content-type");
  if (contentType) headers.set("Content-Type", contentType);
  return new NextResponse(body, { status: response.status, headers });
}

async function authResponse(response: Response) {
  if (!response.ok) return copyResponse(response);
  const data = await response.json();
  const result = NextResponse.json({ expiresIn: data.expiresIn, user: data.user });
  result.cookies.set(ACCESS_COOKIE, data.accessToken, cookieOptions(Number(data.expiresIn ?? 900)));
  result.cookies.set(REFRESH_COOKIE, data.refreshToken, cookieOptions(60 * 60 * 24 * 30));
  return result;
}

async function proxyHandler(request: NextRequest, context: RouteContext) {
  const { path: segments } = await context.params;
  const path = segments.join("/");

  if (!allowedPaths.some((pattern) => pattern.test(path))) {
    return NextResponse.json({ detail: "Recurso não encontrado." }, { status: 404 });
  }

  const contentLength = Number(request.headers.get("content-length") ?? 0);
  if (contentLength > 1024 * 1024) {
    return NextResponse.json({ detail: "A requisição excede o limite de 1 MB." }, { status: 413 });
  }

  const cookieStore = await cookies();
  const accessToken = cookieStore.get(ACCESS_COOKIE)?.value;
  const refreshToken = cookieStore.get(REFRESH_COOKIE)?.value;

  if (path === "auth/logout") {
    const response = refreshToken
      ? await upstream(request, path, undefined, JSON.stringify({ refreshToken }))
      : new Response(null, { status: 204 });
    const result = await copyResponse(response);
    result.cookies.delete(ACCESS_COOKIE);
    result.cookies.delete(REFRESH_COOKIE);
    return result;
  }

  if (path === "auth/refresh") {
    if (!refreshToken) return NextResponse.json({ detail: "Sessão expirada." }, { status: 401 });
    return authResponse(await upstream(request, path, undefined, JSON.stringify({ refreshToken })));
  }

  const originalBody = !["GET", "HEAD"].includes(request.method) ? await request.text() : undefined;
  let response = await upstream(request, path, accessToken, originalBody);

  if (path === "auth/login") return authResponse(response);

  if (response.status === 401 && refreshToken && !path.startsWith("auth/")) {
    const refreshResponse = await fetch(`${API_URL}/api/v1/auth/refresh`, {
      method: "POST",
      headers: { "Content-Type": "application/json", Accept: "application/json" },
      body: JSON.stringify({ refreshToken }),
      cache: "no-store",
    });

    if (refreshResponse.ok) {
      const refreshed = await refreshResponse.json();
      response = await upstream(request, path, refreshed.accessToken, originalBody);
      const result = await copyResponse(response);
      result.cookies.set(ACCESS_COOKIE, refreshed.accessToken, cookieOptions(Number(refreshed.expiresIn ?? 900)));
      result.cookies.set(REFRESH_COOKIE, refreshed.refreshToken, cookieOptions(60 * 60 * 24 * 30));
      return result;
    }
  }

  return copyResponse(response);
}

async function handler(request: NextRequest, context: RouteContext) {
  try {
    return await proxyHandler(request, context);
  } catch {
    return NextResponse.json(
      { detail: "Não foi possível conectar ao serviço. Verifique se a API está disponível." },
      { status: 503 },
    );
  }
}

export const GET = handler;
export const POST = handler;
export const PUT = handler;
export const PATCH = handler;
export const DELETE = handler;
