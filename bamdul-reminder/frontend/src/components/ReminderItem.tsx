"use client";

import { useState, useEffect } from "react";
import type { Reminder, Priority } from "@/types/reminder";
import { toggleReminder, updateReminder, deleteReminder, createReminder } from "@/lib/api";

interface Props {
  reminder: Reminder;
  onUpdate: (updated: Reminder) => void;
  onDelete: (id: number) => void;
  depth?: number;
  listColor?: string;
}

const PRIORITY_MARKS: Record<string, { label: string; color: string }> = {
  HIGH: { label: "!!!", color: "var(--system-red)" },
  MEDIUM: { label: "!!", color: "var(--system-orange)" },
  LOW: { label: "!", color: "var(--system-blue)" },
};

export default function ReminderItem({ reminder, onUpdate, onDelete, depth = 0, listColor = "#007AFF" }: Props) {
  const [expanded, setExpanded] = useState(false);
  const [title, setTitle] = useState(reminder.title);
  const [notes, setNotes] = useState(reminder.notes || "");
  const [dueDate, setDueDate] = useState(reminder.dueDate?.slice(0, 16) || "");
  const [priority, setPriority] = useState<Priority>(reminder.priority);
  const [flagged, setFlagged] = useState(reminder.flagged);
  const [addingChild, setAddingChild] = useState(false);
  const [childTitle, setChildTitle] = useState("");
  const [confirmDelete, setConfirmDelete] = useState(false);

  useEffect(() => {
    setTitle(reminder.title);
    setNotes(reminder.notes || "");
    setDueDate(reminder.dueDate?.slice(0, 16) || "");
    setPriority(reminder.priority);
    setFlagged(reminder.flagged);
  }, [reminder]);

  async function handleToggle() {
    try {
      const updated = await toggleReminder(reminder.id);
      onUpdate({ ...updated, children: reminder.children });
    } catch { /* ignore */ }
  }

  async function saveField(field: string, value: string | boolean) {
    try {
      const payload = {
        title: field === "title" ? (value as string) : title,
        notes: field === "notes" ? (value as string) || undefined : notes || undefined,
        dueDate: field === "dueDate" ? (value as string) || undefined : dueDate || undefined,
        priority: field === "priority" ? (value as string) : priority,
        flagged: field === "flagged" ? (value as boolean) : flagged,
      };
      const updated = await updateReminder(reminder.id, payload);
      onUpdate({ ...updated, children: reminder.children });
    } catch { /* ignore */ }
  }

  function handleTitleBlur() {
    if (title.trim() && title !== reminder.title) saveField("title", title);
    else setTitle(reminder.title);
  }

  function handleNotesBlur() {
    if (notes !== (reminder.notes || "")) saveField("notes", notes);
  }

  async function handleDelete() {
    if (!confirmDelete) { setConfirmDelete(true); return; }
    try { await deleteReminder(reminder.id); onDelete(reminder.id); }
    catch { setConfirmDelete(false); }
  }

  function handleChildUpdate(updated: Reminder) {
    onUpdate({ ...reminder, children: reminder.children.map((c) => (c.id === updated.id ? updated : c)) });
  }

  function handleChildDelete(id: number) {
    onUpdate({ ...reminder, children: reminder.children.filter((c) => c.id !== id) });
  }

  async function handleAddChild(e: React.FormEvent) {
    e.preventDefault();
    if (!childTitle.trim()) return;
    try {
      const created = await createReminder(reminder.listId, {
        title: childTitle.trim(), parentId: reminder.id, sortOrder: reminder.children.length,
      });
      onUpdate({ ...reminder, children: [...reminder.children, { ...created, children: [] }] });
      setChildTitle("");
    } catch { /* ignore */ }
  }

  const indentPx = depth * 24;

  return (
    <>
      <div
        className="group transition-apple"
        style={{ paddingLeft: `${indentPx}px` }}
      >
        {/* Main row */}
        <div className="flex items-start gap-3 px-5 py-[10px]">
          {/* Checkbox */}
          <button
            onClick={handleToggle}
            className="mt-[3px] w-[20px] h-[20px] rounded-full border-[1.5px] flex-shrink-0 flex items-center justify-center transition-apple"
            style={{
              borderColor: reminder.completed ? listColor : "var(--text-tertiary)",
              backgroundColor: reminder.completed ? listColor : "transparent",
            }}
          >
            {reminder.completed && (
              <svg className="w-[10px] h-[10px] text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={4}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
              </svg>
            )}
          </button>

          {/* Content */}
          <div className="flex-1 min-w-0 cursor-pointer" onClick={() => setExpanded(!expanded)}>
            <p className={`text-[15px] leading-[1.35] ${
              reminder.completed ? "line-through text-text-tertiary" : "text-text-primary"
            }`}>
              {reminder.priority !== "NONE" && (
                <span className="font-semibold mr-0.5" style={{ color: PRIORITY_MARKS[reminder.priority]?.color }}>
                  {PRIORITY_MARKS[reminder.priority]?.label}
                </span>
              )}
              {reminder.title}
            </p>
            {/* Inline metadata */}
            {(reminder.notes || reminder.dueDate || reminder.flagged || reminder.children.length > 0) && (
              <div className="flex items-center gap-1.5 mt-[2px]">
                {reminder.notes && (
                  <span className="text-[13px] text-text-secondary truncate max-w-[200px]">{reminder.notes}</span>
                )}
                {reminder.notes && (reminder.dueDate || reminder.flagged) && (
                  <span className="text-text-tertiary text-[10px]">·</span>
                )}
                {reminder.dueDate && (
                  <span className="text-[12px] text-text-secondary whitespace-nowrap">
                    {new Date(reminder.dueDate).toLocaleDateString("ko-KR", { month: "short", day: "numeric" })}
                  </span>
                )}
                {reminder.flagged && (
                  <svg className="w-3 h-3 text-system-orange flex-shrink-0" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M5 2a1 1 0 011 1v1h11.5a1.5 1.5 0 011.2 2.4L15.5 11l3.2 4.6a1.5 1.5 0 01-1.2 2.4H6v3a1 1 0 11-2 0V3a1 1 0 011-1z"/>
                  </svg>
                )}
                {reminder.children.length > 0 && (
                  <span className="text-[12px] text-text-tertiary whitespace-nowrap">
                    {reminder.children.filter(c => c.completed).length}/{reminder.children.length}
                  </span>
                )}
              </div>
            )}
          </div>

          {/* Subtle info button */}
          <button
            onClick={() => setExpanded(!expanded)}
            className={`mt-[3px] w-[20px] h-[20px] rounded-full flex items-center justify-center flex-shrink-0 transition-apple ${
              expanded
                ? "bg-system-blue text-white"
                : "text-text-tertiary opacity-0 group-hover:opacity-100"
            }`}
          >
            <svg className="w-[12px] h-[12px]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
              {expanded
                ? <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                : <path strokeLinecap="round" strokeLinejoin="round" d="M12 6.75a.75.75 0 110-1.5.75.75 0 010 1.5zM12 12.75a.75.75 0 110-1.5.75.75 0 010 1.5zM12 18.75a.75.75 0 110-1.5.75.75 0 010 1.5z" />
              }
            </svg>
          </button>
        </div>

        {/* Detail panel */}
        {expanded && (
          <div className="detail-enter mx-5 mb-3 ml-[52px] bg-bg-tertiary rounded-xl overflow-hidden" style={{ marginLeft: `${52 + indentPx}px` }}>
            <div className="p-3 space-y-2">
              {/* Title */}
              <input
                type="text" value={title}
                onChange={(e) => setTitle(e.target.value)}
                onBlur={handleTitleBlur}
                onKeyDown={(e) => { if (e.key === "Enter" || e.key === "Escape") (e.target as HTMLInputElement).blur(); }}
                className="w-full px-3 py-2 text-[15px] bg-bg-secondary text-text-primary rounded-lg focus:outline-none focus:ring-1 focus:ring-system-blue/30 placeholder:text-text-tertiary"
                placeholder="제목"
              />
              {/* Notes */}
              <input
                type="text" value={notes}
                onChange={(e) => setNotes(e.target.value)}
                onBlur={handleNotesBlur}
                onKeyDown={(e) => { if (e.key === "Enter") (e.target as HTMLInputElement).blur(); }}
                className="w-full px-3 py-2 text-[13px] bg-bg-secondary text-text-primary rounded-lg focus:outline-none focus:ring-1 focus:ring-system-blue/30 placeholder:text-text-tertiary"
                placeholder="메모"
              />
            </div>

            {/* Options */}
            <div className="border-t border-separator">
              {/* Date row */}
              <label className="flex items-center gap-3 px-4 py-2.5 cursor-pointer hover:bg-text-primary/[0.03] transition-apple">
                <svg className="w-[18px] h-[18px] text-system-blue flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M6.75 3v2.25M17.25 3v2.25M3 18.75V7.5a2.25 2.25 0 012.25-2.25h13.5A2.25 2.25 0 0121 7.5v11.25m-18 0A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75m-18 0v-7.5A2.25 2.25 0 015.25 9h13.5A2.25 2.25 0 0121 11.25v7.5" />
                </svg>
                <span className="flex-1 text-[14px] text-text-primary">날짜</span>
                <span className="text-[13px] text-system-blue">
                  {dueDate ? new Date(dueDate).toLocaleDateString("ko-KR", { month: "long", day: "numeric" }) : "없음"}
                </span>
                <input
                  type="datetime-local" value={dueDate}
                  onChange={(e) => { setDueDate(e.target.value); saveField("dueDate", e.target.value); }}
                  className="absolute w-0 h-0 opacity-0"
                />
              </label>

              {/* Priority row */}
              <div className="flex items-center gap-3 px-4 py-2.5">
                <svg className="w-[18px] h-[18px] text-system-red flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z" />
                </svg>
                <span className="flex-1 text-[14px] text-text-primary">우선순위</span>
                <div className="flex bg-bg-secondary rounded-lg overflow-hidden">
                  {(["NONE", "LOW", "MEDIUM", "HIGH"] as Priority[]).map((p) => (
                    <button
                      key={p}
                      onClick={() => { setPriority(p); saveField("priority", p); }}
                      className={`px-2.5 py-1 text-[12px] font-medium transition-apple ${
                        priority === p
                          ? "bg-system-blue text-white"
                          : "text-text-secondary hover:bg-text-primary/[0.05]"
                      }`}
                    >
                      {p === "NONE" ? "없음" : p === "LOW" ? "낮음" : p === "MEDIUM" ? "보통" : "높음"}
                    </button>
                  ))}
                </div>
              </div>

              {/* Flag row */}
              <button
                onClick={() => { const next = !flagged; setFlagged(next); saveField("flagged", next); }}
                className="flex items-center gap-3 px-4 py-2.5 w-full hover:bg-text-primary/[0.03] transition-apple"
              >
                <svg className="w-[18px] h-[18px] text-system-orange flex-shrink-0" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M5 2a1 1 0 011 1v1h11.5a1.5 1.5 0 011.2 2.4L15.5 11l3.2 4.6a1.5 1.5 0 01-1.2 2.4H6v3a1 1 0 11-2 0V3a1 1 0 011-1z"/>
                </svg>
                <span className="flex-1 text-[14px] text-text-primary text-left">깃발</span>
                {flagged && (
                  <svg className="w-4 h-4 text-system-blue" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M4.5 12.75l6 6 9-13.5" />
                  </svg>
                )}
              </button>

              {/* Sub-reminder */}
              {depth === 0 && (
                <button
                  onClick={() => { setAddingChild(true); setExpanded(true); }}
                  className="flex items-center gap-3 px-4 py-2.5 w-full hover:bg-text-primary/[0.03] transition-apple"
                >
                  <svg className="w-[18px] h-[18px] text-system-indigo flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M8.25 6.75h12M8.25 12h12m-12 5.25h12M3.75 6.75h.007v.008H3.75V6.75zm.375 0a.375.375 0 11-.75 0 .375.375 0 01.75 0zM3.75 12h.007v.008H3.75V12zm.375 0a.375.375 0 11-.75 0 .375.375 0 01.75 0zm-.375 5.25h.007v.008H3.75v-.008zm.375 0a.375.375 0 11-.75 0 .375.375 0 01.75 0z" />
                  </svg>
                  <span className="flex-1 text-[14px] text-text-primary text-left">하위 미리 알림</span>
                  {reminder.children.length > 0 && (
                    <span className="text-[13px] text-text-secondary">{reminder.children.length}</span>
                  )}
                </button>
              )}
            </div>

            {/* Delete */}
            <div className="border-t border-separator">
              <button
                onClick={handleDelete}
                onBlur={() => setConfirmDelete(false)}
                className={`w-full px-4 py-2.5 text-[14px] text-left transition-apple ${
                  confirmDelete ? "bg-system-red text-white font-medium" : "text-system-red hover:bg-text-primary/[0.03]"
                }`}
              >
                {confirmDelete ? "정말 삭제할까요?" : "삭제"}
              </button>
            </div>
          </div>
        )}

        {/* Separator line (indented like Apple) */}
        <div className="border-b border-separator" style={{ marginLeft: `${52 + indentPx}px` }} />
      </div>

      {/* Children */}
      {reminder.children.map((child) => (
        <ReminderItem
          key={child.id} reminder={child}
          onUpdate={handleChildUpdate} onDelete={handleChildDelete}
          depth={depth + 1} listColor={listColor}
        />
      ))}

      {/* Add child inline */}
      {addingChild && (
        <form
          onSubmit={handleAddChild}
          className="flex items-center gap-3 py-[10px]"
          style={{ paddingLeft: `${52 + (depth + 1) * 24}px`, paddingRight: 20 }}
        >
          <div className="w-[16px] h-[16px] rounded-full border-[1.5px] border-text-tertiary flex-shrink-0" />
          <input
            type="text" value={childTitle}
            onChange={(e) => setChildTitle(e.target.value)}
            placeholder="새로운 하위 미리 알림"
            className="flex-1 bg-transparent text-[14px] text-text-primary placeholder:text-text-tertiary focus:outline-none"
            autoFocus
            onKeyDown={(e) => { if (e.key === "Escape") { setAddingChild(false); setChildTitle(""); } }}
          />
          <button type="button" onClick={() => { setAddingChild(false); setChildTitle(""); }}
            className="text-[13px] text-text-tertiary hover:text-text-primary transition-apple">닫기</button>
        </form>
      )}
    </>
  );
}
