"use client";

import { useEffect, useState, useCallback, useRef } from "react";
import { useParams } from "next/navigation";
import { getReminders, createReminder, getLists, reorderReminders } from "@/lib/api";
import ReminderItem from "@/components/ReminderItem";
import type { Reminder, ReminderList } from "@/types/reminder";
import {
  DndContext, closestCenter, KeyboardSensor, PointerSensor,
  useSensor, useSensors, type DragEndEvent,
} from "@dnd-kit/core";
import {
  SortableContext, sortableKeyboardCoordinates,
  verticalListSortingStrategy, useSortable,
} from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";

function SortableItem({ reminder, listColor, onUpdate, onDelete }: {
  reminder: Reminder; listColor: string;
  onUpdate: (updated: Reminder) => void; onDelete: (id: number) => void;
}) {
  const { attributes, listeners, setNodeRef, transform, transition } = useSortable({ id: reminder.id });
  const style = { transform: CSS.Transform.toString(transform), transition };
  return (
    <div ref={setNodeRef} style={style} {...attributes} {...listeners}>
      <ReminderItem reminder={reminder} onUpdate={onUpdate} onDelete={onDelete} listColor={listColor} />
    </div>
  );
}

export default function ListPage() {
  const params = useParams();
  const listId = Number(params.id);
  const [reminders, setReminders] = useState<Reminder[]>([]);
  const [list, setList] = useState<ReminderList | null>(null);
  const [newTitle, setNewTitle] = useState("");
  const inputRef = useRef<HTMLInputElement>(null);

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 8 } }),
    useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates }),
  );

  const loadReminders = useCallback(() => {
    if (listId) getReminders(listId).then(setReminders).catch(() => {});
  }, [listId]);

  useEffect(() => {
    loadReminders();
    getLists().then((lists) => {
      const found = lists.find((l) => l.id === listId);
      if (found) setList(found);
    }).catch(() => {});
  }, [listId, loadReminders]);

  function handleUpdate(updated: Reminder) {
    setReminders((prev) => prev.map((r) => (r.id === updated.id ? updated : r)));
  }

  function handleDelete(id: number) {
    setReminders((prev) => prev.filter((r) => r.id !== id));
  }

  async function handleAdd(e: React.FormEvent) {
    e.preventDefault();
    if (!newTitle.trim()) return;
    try {
      const created = await createReminder(listId, { title: newTitle.trim(), sortOrder: reminders.length });
      setReminders((prev) => [...prev, { ...created, children: created.children || [] }]);
      setNewTitle("");
      inputRef.current?.focus();
    } catch { /* ignore */ }
  }

  async function handleDragEnd(event: DragEndEvent) {
    const { active, over } = event;
    if (!over || active.id === over.id) return;
    const oldIdx = reminders.findIndex((r) => r.id === active.id);
    const newIdx = reminders.findIndex((r) => r.id === over.id);
    if (oldIdx === -1 || newIdx === -1) return;
    const reordered = [...reminders];
    const [moved] = reordered.splice(oldIdx, 1);
    reordered.splice(newIdx, 0, moved);
    setReminders(reordered);
    try { await reorderReminders(reordered.map((r) => r.id)); }
    catch { loadReminders(); }
  }

  const listColor = list?.color || "#007AFF";

  return (
    <div className="p-6 md:py-12 md:px-10 max-w-2xl mx-auto">
      {/* Header */}
      <h1 className="font-rounded text-[34px] font-bold tracking-tight mb-6" style={{ color: listColor }}>
        {list?.name || "미리 알림"}
      </h1>

      {/* Reminder list card */}
      <div className="bg-bg-secondary rounded-2xl overflow-hidden">
        {reminders.length === 0 && (
          <div className="px-5 py-10 text-center">
            <p className="text-text-tertiary text-[14px]">아직 미리 알림이 없어요</p>
          </div>
        )}

        {reminders.length > 0 && (
          <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={handleDragEnd}>
            <SortableContext items={reminders.map((r) => r.id)} strategy={verticalListSortingStrategy}>
              {reminders.map((reminder) => (
                <SortableItem
                  key={reminder.id} reminder={reminder}
                  listColor={listColor} onUpdate={handleUpdate} onDelete={handleDelete}
                />
              ))}
            </SortableContext>
          </DndContext>
        )}

        {/* Always-visible inline input */}
        <form onSubmit={handleAdd} className="flex items-center gap-3 px-5 py-[10px] border-t border-separator">
          <div
            className="w-[20px] h-[20px] rounded-full border-[1.5px] flex-shrink-0 transition-apple"
            style={{ borderColor: newTitle.trim() ? listColor : "var(--text-tertiary)" }}
          />
          <input
            ref={inputRef}
            type="text" value={newTitle}
            onChange={(e) => setNewTitle(e.target.value)}
            placeholder="새로운 미리 알림"
            className="flex-1 bg-transparent text-[15px] text-text-primary placeholder:text-text-tertiary focus:outline-none"
          />
        </form>
      </div>
    </div>
  );
}
