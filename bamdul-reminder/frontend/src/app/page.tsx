"use client";

import { useEffect, useState } from "react";
import { getLists } from "@/lib/api";
import Link from "next/link";
import type { ReminderList } from "@/types/reminder";

export default function HomePage() {
  const [lists, setLists] = useState<ReminderList[]>([]);

  useEffect(() => {
    getLists().then(setLists).catch(() => {});
  }, []);

  return (
    <div className="p-8">
      <h1 className="text-2xl font-bold text-gray-800 mb-6">리마인더</h1>
      {lists.length === 0 ? (
        <p className="text-gray-500">목록이 없습니다. 사이드바에서 목록을 추가해보세요.</p>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {lists.map((list) => (
            <Link
              key={list.id}
              href={`/lists/${list.id}`}
              className="block p-4 border border-gray-200 rounded-xl hover:shadow-md transition-shadow"
            >
              <div className="flex items-center gap-3">
                {list.color && (
                  <span
                    className="w-4 h-4 rounded-full"
                    style={{ backgroundColor: list.color }}
                  />
                )}
                <span className="font-medium text-gray-800">{list.name}</span>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
