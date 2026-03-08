"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { getReminders } from "@/lib/api";
import ReminderItem from "@/components/ReminderItem";
import type { Reminder } from "@/types/reminder";

export default function ListPage() {
  const params = useParams();
  const listId = Number(params.id);
  const [reminders, setReminders] = useState<Reminder[]>([]);

  useEffect(() => {
    if (listId) {
      getReminders(listId).then(setReminders).catch(() => {});
    }
  }, [listId]);

  function handleToggle(updated: Reminder) {
    setReminders((prev) =>
      prev.map((r) => (r.id === updated.id ? updated : r))
    );
  }

  return (
    <div className="p-8">
      <h1 className="text-2xl font-bold text-gray-800 mb-4">리마인더 목록</h1>
      {reminders.length === 0 ? (
        <p className="text-gray-500">리마인더가 없습니다.</p>
      ) : (
        <div className="border border-gray-200 rounded-xl overflow-hidden">
          {reminders.map((reminder) => (
            <ReminderItem
              key={reminder.id}
              reminder={reminder}
              onToggle={handleToggle}
            />
          ))}
        </div>
      )}
    </div>
  );
}
