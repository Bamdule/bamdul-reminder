"use client";

import { useEffect, useState, useCallback } from "react";
import { useParams } from "next/navigation";
import { getReminders, createReminder, getLists } from "@/lib/api";
import ReminderItem from "@/components/ReminderItem";
import type { Reminder, ReminderList } from "@/types/reminder";

export default function ListPage() {
  const params = useParams();
  const listId = Number(params.id);
  const [reminders, setReminders] = useState<Reminder[]>([]);
  const [list, setList] = useState<ReminderList | null>(null);
  const [newTitle, setNewTitle] = useState("");
  const [adding, setAdding] = useState(false);

  const loadReminders = useCallback(() => {
    if (listId) {
      getReminders(listId).then(setReminders).catch(() => {});
    }
  }, [listId]);

  useEffect(() => {
    loadReminders();
    getLists().then((lists) => {
      const found = lists.find((l) => l.id === listId);
      if (found) setList(found);
    }).catch(() => {});
  }, [listId, loadReminders]);

  function handleUpdate(updated: Reminder) {
    setReminders((prev) =>
      prev.map((r) => (r.id === updated.id ? updated : r))
    );
  }

  function handleDelete(id: number) {
    setReminders((prev) => prev.filter((r) => r.id !== id));
  }

  async function handleAdd(e: React.FormEvent) {
    e.preventDefault();
    if (!newTitle.trim()) return;
    try {
      const created = await createReminder(listId, {
        title: newTitle.trim(),
        sortOrder: reminders.length,
      });
      setReminders((prev) => [...prev, created]);
      setNewTitle("");
    } catch {
      // ignore
    }
  }

  return (
    <div className="p-8">
      <h1 className="text-2xl font-bold text-gray-800 mb-4">
        {list ? (
          <span className="flex items-center gap-2">
            {list.color && (
              <span className="w-4 h-4 rounded-full" style={{ backgroundColor: list.color }} />
            )}
            {list.name}
          </span>
        ) : (
          "리마인더 목록"
        )}
      </h1>

      {reminders.length > 0 && (
        <div className="border border-gray-200 rounded-xl overflow-hidden mb-4">
          {reminders.map((reminder) => (
            <ReminderItem
              key={reminder.id}
              reminder={reminder}
              onUpdate={handleUpdate}
              onDelete={handleDelete}
            />
          ))}
        </div>
      )}

      {adding ? (
        <form onSubmit={handleAdd} className="flex items-center gap-2">
          <input
            type="text"
            value={newTitle}
            onChange={(e) => setNewTitle(e.target.value)}
            placeholder="리마인더 제목"
            className="flex-1 px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            autoFocus
            onKeyDown={(e) => {
              if (e.key === "Escape") {
                setAdding(false);
                setNewTitle("");
              }
            }}
          />
          <button
            type="submit"
            className="px-4 py-2 text-sm bg-blue-500 text-white rounded-lg hover:bg-blue-600"
          >
            추가
          </button>
          <button
            type="button"
            onClick={() => { setAdding(false); setNewTitle(""); }}
            className="px-4 py-2 text-sm text-gray-600 hover:bg-gray-100 rounded-lg"
          >
            취소
          </button>
        </form>
      ) : (
        <button
          onClick={() => setAdding(true)}
          className="text-sm text-blue-500 hover:text-blue-600 font-medium"
        >
          + 리마인더 추가
        </button>
      )}
    </div>
  );
}
