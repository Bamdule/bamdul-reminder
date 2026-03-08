"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { signup } from "@/lib/api";
import { setToken } from "@/lib/auth";

export default function SignupPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const result = await signup(email, password, name);
      setToken(result.token);
      router.push("/");
    } catch (err) {
      setError(err instanceof Error ? err.message : "회원가입에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-bg-primary px-6">
      <div className="w-full max-w-[340px]">
        <div className="text-center mb-10">
          <h1 className="font-rounded text-[32px] font-bold text-text-primary">새 계정 만들기</h1>
          <p className="text-[14px] text-text-secondary mt-1">미리 알림을 시작해 보세요</p>
        </div>

        <div className="bg-bg-secondary rounded-2xl overflow-hidden">
          <form onSubmit={handleSubmit}>
            <div className="px-4 pt-4 pb-0">
              <input
                type="text" value={name}
                onChange={(e) => setName(e.target.value)}
                required
                className="w-full px-0 py-3 text-[16px] bg-transparent text-text-primary border-b border-separator focus:outline-none placeholder:text-text-tertiary"
                placeholder="이름"
              />
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
                required minLength={8}
                className="w-full px-0 py-3 text-[16px] bg-transparent text-text-primary border-b border-separator focus:outline-none placeholder:text-text-tertiary"
                placeholder="비밀번호 (8자 이상)"
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
                {loading ? "가입 중..." : "회원가입"}
              </button>
            </div>
          </form>
        </div>

        <p className="mt-6 text-center text-[14px] text-text-secondary">
          이미 계정이 있으신가요?{" "}
          <Link href="/login" className="text-system-blue font-medium">
            로그인
          </Link>
        </p>
      </div>
    </div>
  );
}
