"use client";

import { useState } from "react";
import type { Reminder, Priority } from "@/types/reminder";
import { toggleReminder, updateReminder, deleteReminder } from "@/lib/api";

interface Props {
  reminder: Reminder;
  onUpdate: (updated: Reminder) => void;
  onDelete: (id: number) => void;
}

export default function ReminderItem({ reminder, onUpdate, onDelete }: Props) {
  const [editing, setEditing] = useState(false);
  const [title, setTitle] = useState(reminder.title);
  const [notes, setNotes] = useState(reminder.notes || "");
  const [dueDate, setDueDate] = useState(reminder.dueDate?.slice(0, 16) || "");
  const [priority, setPriority] = useState<Priority>(reminder.priority);
  const [flagged, setFlagged] = useState(reminder.flagged);
  const [deleting, setDeleting] = useState(false);

  async function handleToggle() {
    try {
      const updated = await toggleReminder(reminder.id);
      onUpdate(updated);
    } catch {
      // ignore
    }
  }

  async function handleSave() {
    try {
      const updated = await updateReminder(reminder.id, {
        title,
        notes: notes || undefined,
        dueDate: dueDate || undefined,
        priority,
        flagged,
      });
      onUpdate(updated);
      setEditing(false);
    } catch {
      // ignore
    }
  }

  async function handleDelete() {
    if (!deleting) {
      setDeleting(true);
      return;
    }
    try {
      await deleteReminder(reminder.id);
      onDelete(reminder.id);
    } catch {
      setDeleting(false);
    }
  }

  function handleCancel() {
    setTitle(reminder.title);
    setNotes(reminder.notes || "");
    setDueDate(reminder.dueDate?.slice(0, 16) || "");
    setPriority(reminder.priority);
    setFlagged(reminder.flagged);
    setEditing(false);
    setDeleting(false);
  }

  if (editing) {
    return (
      <div className="px-4 py-3 border-b border-gray-100 bg-blue-50 space-y-2">
        <input
          type="text"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && handleSave()}
          className="w-full px-2 py-1 text-sm border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
          autoFocus
        />
        <input
          type="text"
          value={notes}
          onChange={(e) => setNotes(e.target.value)}
          placeholder="메모"
          className="w-full px-2 py-1 text-sm border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <div className="flex items-center gap-2">
          <input
            type="datetime-local"
            value={dueDate}
            onChange={(e) => setDueDate(e.target.value)}
            className="px-2 py-1 text-sm border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <select
            value={priority}
            onChange={(e) => setPriority(e.target.value as Priority)}
            className="px-2 py-1 text-sm border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="NONE">없음</option>
            <option value="LOW">낮음</option>
            <option value="MEDIUM">보통</option>
            <option value="HIGH">높음</option>
          </select>
          <label className="flex items-center gap-1 text-sm text-gray-600">
            <input
              type="checkbox"
              checked={flagged}
              onChange={(e) => setFlagged(e.target.checked)}
              className="rounded"
            />
            플래그
          </label>
        </div>
        <div className="flex gap-2">
          <button
            onClick={handleSave}
            className="px-3 py-1 text-sm bg-blue-500 text-white rounded hover:bg-blue-600"
          >
            저장
          </button>
          <button
            onClick={handleCancel}
            className="px-3 py-1 text-sm text-gray-600 hover:bg-gray-200 rounded"
          >
            취소
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="group flex items-start gap-3 px-4 py-3 border-b border-gray-100 hover:bg-gray-50">
      <button
        onClick={handleToggle}
        className={`mt-0.5 w-5 h-5 rounded-full border-2 flex-shrink-0 flex items-center justify-center transition-colors ${
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
      <div
        className="flex-1 min-w-0 cursor-pointer"
        onClick={() => setEditing(true)}
      >
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
      <button
        onClick={handleDelete}
        onBlur={() => setDeleting(false)}
        className={`self-center text-xs px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity ${
          deleting
            ? "bg-red-500 text-white"
            : "text-gray-400 hover:text-red-500 hover:bg-red-50"
        }`}
      >
        {deleting ? "확인" : "삭제"}
      </button>
    </div>
  );
}
