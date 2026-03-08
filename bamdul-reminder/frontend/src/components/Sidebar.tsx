"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { getLists } from "@/lib/api";
import { removeToken } from "@/lib/auth";
import type { ReminderList } from "@/types/reminder";

export default function Sidebar() {
  const pathname = usePathname();
  const [lists, setLists] = useState<ReminderList[]>([]);

  useEffect(() => {
    getLists().then(setLists).catch(() => {});
  }, []);

  function handleLogout() {
    removeToken();
    window.location.href = "/login";
  }

  return (
    <aside className="w-64 bg-gray-50 border-r border-gray-200 h-screen flex flex-col">
      <div className="p-4 border-b border-gray-200">
        <h1 className="text-lg font-bold text-gray-800">Bamdul Reminder</h1>
      </div>

      <nav className="flex-1 overflow-y-auto p-3 space-y-1">
        <SidebarLink href="/" label="홈" active={pathname === "/"} />

        <div className="pt-4 pb-1">
          <span className="text-xs font-semibold text-gray-500 uppercase tracking-wider px-3">
            내 목록
          </span>
        </div>
        {lists.map((list) => (
          <SidebarLink
            key={list.id}
            href={`/lists/${list.id}`}
            label={list.name}
            color={list.color}
            active={pathname === `/lists/${list.id}`}
          />
        ))}
      </nav>

      <div className="p-3 border-t border-gray-200">
        <button
          onClick={handleLogout}
          className="w-full text-left px-3 py-2 text-sm text-gray-600 hover:bg-gray-200 rounded-lg"
        >
          로그아웃
        </button>
      </div>
    </aside>
  );
}

function SidebarLink({
  href,
  label,
  color,
  active,
}: {
  href: string;
  label: string;
  color?: string | null;
  active: boolean;
}) {
  return (
    <Link
      href={href}
      className={`flex items-center gap-2 px-3 py-2 rounded-lg text-sm ${
        active ? "bg-blue-100 text-blue-700 font-medium" : "text-gray-700 hover:bg-gray-200"
      }`}
    >
      {color && (
        <span
          className="w-3 h-3 rounded-full flex-shrink-0"
          style={{ backgroundColor: color }}
        />
      )}
      <span className="truncate">{label}</span>
    </Link>
  );
}
