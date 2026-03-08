"use client";

import { useEffect, useState } from "react";
import { getLists, getSmartListCounts } from "@/lib/api";
import Link from "next/link";
import type { ReminderList, SmartListCounts } from "@/types/reminder";

const SMART_CARDS = [
  { key: "today" as const, label: "오늘", color: "#007AFF", href: "/today", icon: (
    <svg viewBox="0 0 24 24" fill="currentColor" className="w-7 h-7"><path d="M6 2a1 1 0 012 0v1h8V2a1 1 0 112 0v1h1a3 3 0 013 3v12a3 3 0 01-3 3H5a3 3 0 01-3-3V6a3 3 0 013-3h1V2zm-1 7v9a1 1 0 001 1h12a1 1 0 001-1V9H5zm3 3a1 1 0 110 2 1 1 0 010-2z"/></svg>
  )},
  { key: "scheduled" as const, label: "예정", color: "#FF3B30", href: "/scheduled", icon: (
    <svg viewBox="0 0 24 24" fill="currentColor" className="w-7 h-7"><path d="M12 2a10 10 0 110 20 10 10 0 010-20zm0 4a1 1 0 00-1 1v5a1 1 0 00.293.707l3 3a1 1 0 001.414-1.414L13 11.586V7a1 1 0 00-1-1z"/></svg>
  )},
  { key: "all" as const, label: "전체", color: "#5856D6", href: "/all", icon: (
    <svg viewBox="0 0 24 24" fill="currentColor" className="w-7 h-7"><path d="M5 3h14a3 3 0 013 3v12a3 3 0 01-3 3H5a3 3 0 01-3-3V6a3 3 0 013-3zm1 5a1 1 0 100 2h12a1 1 0 100-2H6zm0 4a1 1 0 100 2h12a1 1 0 100-2H6zm0 4a1 1 0 100 2h8a1 1 0 100-2H6z"/></svg>
  )},
  { key: "flagged" as const, label: "깃발 표시", color: "#FF9500", href: "/flagged", icon: (
    <svg viewBox="0 0 24 24" fill="currentColor" className="w-7 h-7"><path d="M5 2a1 1 0 011 1v1h11.5a1.5 1.5 0 011.2 2.4L15.5 11l3.2 4.6a1.5 1.5 0 01-1.2 2.4H6v3a1 1 0 11-2 0V3a1 1 0 011-1z"/></svg>
  )},
  { key: "completed" as const, label: "완료됨", color: "#8E8E93", href: "/completed", icon: (
    <svg viewBox="0 0 24 24" fill="currentColor" className="w-7 h-7"><path d="M12 2a10 10 0 110 20 10 10 0 010-20zm4.707 7.293a1 1 0 00-1.414 0L11 13.586l-2.293-2.293a1 1 0 00-1.414 1.414l3 3a1 1 0 001.414 0l5-5a1 1 0 000-1.414z"/></svg>
  )},
];

export default function HomePage() {
  const [lists, setLists] = useState<ReminderList[]>([]);
  const [counts, setCounts] = useState<SmartListCounts | null>(null);

  useEffect(() => {
    getLists().then(setLists).catch(() => {});
    getSmartListCounts().then(setCounts).catch(() => {});
  }, []);

  return (
    <div className="p-6 md:py-12 md:px-10 max-w-2xl mx-auto">
      {/* Smart cards */}
      <div className="grid grid-cols-2 gap-3 mb-10">
        {SMART_CARDS.map(({ key, label, color, href, icon }) => (
          <Link key={key} href={href}
            className="bg-bg-secondary rounded-2xl p-4 hover:scale-[1.02] active:scale-[0.98] transition-apple">
            <div className="flex items-start justify-between mb-4">
              <span className="w-11 h-11 rounded-full flex items-center justify-center" style={{ backgroundColor: color }}>
                <span className="text-white">{icon}</span>
              </span>
              <span className="font-rounded text-[28px] font-bold text-text-primary tabular-nums">
                {counts ? counts[key] : "–"}
              </span>
            </div>
            <p className="text-[14px] font-semibold text-text-secondary">{label}</p>
          </Link>
        ))}
      </div>

      {/* My lists */}
      <p className="text-[12px] font-semibold text-text-secondary uppercase tracking-wider mb-2 px-1">나의 목록</p>
      {lists.length === 0 ? (
        <div className="bg-bg-secondary rounded-2xl px-5 py-10 text-center">
          <p className="text-text-tertiary text-[14px]">아직 목록이 없어요</p>
          <p className="text-text-tertiary text-[12px] mt-1">사이드바에서 새 목록을 만들어 보세요</p>
        </div>
      ) : (
        <div className="bg-bg-secondary rounded-2xl overflow-hidden">
          {lists.map((list, i) => (
            <Link key={list.id} href={`/lists/${list.id}`}
              className="flex items-center gap-3 px-4 py-3 hover:bg-text-primary/[0.03] active:bg-text-primary/[0.06] transition-apple">
              <span className="w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0" style={{ backgroundColor: list.color || "#007AFF" }}>
                <svg className="w-4 h-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M4 6h16M4 12h16M4 18h8" />
                </svg>
              </span>
              <span className="flex-1 text-[15px] text-text-primary">{list.name}</span>
              <svg className="w-[14px] h-[14px] text-text-tertiary" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M8.25 4.5l7.5 7.5-7.5 7.5" />
              </svg>
              {i < lists.length - 1 && <div className="absolute bottom-0 right-0 left-[60px] h-px bg-separator" />}
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
