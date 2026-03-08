"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { login } from "@/lib/api";
import { setToken } from "@/lib/auth";

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const result = await login(email, password);
      setToken(result.token);
      router.push("/");
    } catch (err) {
      setError(err instanceof Error ? err.message : "로그인에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-bg-primary px-6">
      <div className="w-full max-w-[340px]">
        <div className="text-center mb-10">
          <h1 className="font-rounded text-[32px] font-bold text-text-primary">미리 알림</h1>
          <p className="text-[14px] text-text-secondary mt-1">로그인하여 시작하세요</p>
        </div>

        <div className="bg-bg-secondary rounded-2xl overflow-hidden">
          <form onSubmit={handleSubmit}>
            <div className="px-4 pt-4 pb-0">
              <input
                type="email" value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="w-full px-0 py-3 text-[16px] bg-transparent text-text-primary border-b border-separator focus:outline-none placeholder:text-text-tertiary"
                placeholder="이메일"
              />
              <input
                type="password" value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="w-full px-0 py-3 text-[16px] bg-transparent text-text-primary border-b border-separator focus:outline-none placeholder:text-text-tertiary"
                placeholder="비밀번호"
              />
            </div>
            {error && (
              <p className="text-[13px] text-system-red px-4 pt-3">{error}</p>
            )}
            <div className="p-4">
              <button
                type="submit" disabled={loading}
                className="w-full py-3 bg-system-blue text-white text-[16px] font-semibold rounded-xl hover:opacity-90 disabled:opacity-50 transition-apple"
              >
                {loading ? "로그인 중..." : "로그인"}
              </button>
            </div>
          </form>
        </div>

        <p className="mt-6 text-center text-[14px] text-text-secondary">
          계정이 없으신가요?{" "}
          <Link href="/signup" className="text-system-blue font-medium">
            회원가입
          </Link>
        </p>
      </div>
    </div>
  );
}
