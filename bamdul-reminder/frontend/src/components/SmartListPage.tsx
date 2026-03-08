"use client";

import { useEffect, useState, useCallback } from "react";
import ReminderItem from "@/components/ReminderItem";
import type { Reminder } from "@/types/reminder";

interface SmartListPageProps {
  title: string;
  color: string;
  fetchReminders: () => Promise<Reminder[]>;
}

export default function SmartListPage({ title, color, fetchReminders }: SmartListPageProps) {
  const [reminders, setReminders] = useState<Reminder[]>([]);

  const load = useCallback(() => {
    fetchReminders().then(setReminders).catch(() => {});
  }, [fetchReminders]);

  useEffect(() => { load(); }, [load]);

  function handleUpdate(updated: Reminder) {
    setReminders((prev) => prev.map((r) => (r.id === updated.id ? updated : r)));
  }

  function handleDelete(id: number) {
    setReminders((prev) => prev.filter((r) => r.id !== id));
  }

  return (
    <div className="p-6 md:py-12 md:px-10 max-w-2xl mx-auto">
      <h1 className="font-rounded text-[34px] font-bold tracking-tight mb-6" style={{ color }}>
        {title}
      </h1>

      {reminders.length === 0 ? (
        <div className="bg-bg-secondary rounded-2xl px-5 py-10 text-center">
          <p className="text-text-tertiary text-[14px]">해당하는 미리 알림이 없어요</p>
        </div>
      ) : (
        <div className="bg-bg-secondary rounded-2xl overflow-hidden">
          {reminders.map((reminder) => (
            <ReminderItem
              key={reminder.id} reminder={reminder}
              onUpdate={handleUpdate} onDelete={handleDelete}
              listColor={color}
            />
          ))}
        </div>
      )}
    </div>
  );
}
