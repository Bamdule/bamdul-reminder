"use client";

import type { Reminder } from "@/types/reminder";
import { toggleReminder } from "@/lib/api";

interface Props {
  reminder: Reminder;
  onToggle: (updated: Reminder) => void;
}

export default function ReminderItem({ reminder, onToggle }: Props) {
  async function handleToggle() {
    try {
      const updated = await toggleReminder(reminder.id);
      onToggle(updated);
    } catch {
      // ignore
    }
  }

  return (
    <div className="flex items-start gap-3 px-4 py-3 border-b border-gray-100 hover:bg-gray-50">
      <button
        onClick={handleToggle}
        className={`mt-0.5 w-5 h-5 rounded-full border-2 flex-shrink-0 flex items-center justify-center ${
          reminder.completed
            ? "bg-blue-500 border-blue-500 text-white"
            : "border-gray-300 hover:border-blue-400"
        }`}
      >
        {reminder.completed && (
          <svg className="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={3}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
          </svg>
        )}
      </button>
      <div className="flex-1 min-w-0">
        <p className={`text-sm ${reminder.completed ? "line-through text-gray-400" : "text-gray-800"}`}>
          {reminder.title}
        </p>
        {reminder.notes && (
          <p className="text-xs text-gray-500 mt-0.5 truncate">{reminder.notes}</p>
        )}
        <div className="flex items-center gap-2 mt-1">
          {reminder.dueDate && (
            <span className="text-xs text-gray-400">
              {new Date(reminder.dueDate).toLocaleDateString("ko-KR")}
            </span>
          )}
          {reminder.priority !== "NONE" && (
            <span className={`text-xs font-medium ${
              reminder.priority === "HIGH" ? "text-red-500" :
              reminder.priority === "MEDIUM" ? "text-orange-500" : "text-blue-500"
            }`}>
              {reminder.priority === "HIGH" ? "!!!" : reminder.priority === "MEDIUM" ? "!!" : "!"}
            </span>
          )}
          {reminder.flagged && <span className="text-xs text-orange-500">&#9873;</span>}
        </div>
      </div>
    </div>
  );
}
