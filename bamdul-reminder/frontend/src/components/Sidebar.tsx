"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { getLists, createList, updateList, deleteList, getSmartListCounts, reorderLists, searchReminders, getGroups, createGroup } from "@/lib/api";
import { removeToken } from "@/lib/auth";
import type { Group } from "@/types/group";
import type { Reminder, ReminderList, SmartListCounts } from "@/types/reminder";
import {
  DndContext, closestCenter, KeyboardSensor, PointerSensor, useSensor, useSensors, type DragEndEvent,
} from "@dnd-kit/core";
import {
  SortableContext, sortableKeyboardCoordinates, verticalListSortingStrategy, useSortable,
} from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";

const COLORS = ["#FF3B30", "#FF9500", "#FFCC00", "#34C759", "#007AFF", "#5856D6", "#AF52DE", "#FF2D55", "#8E8E93", "#A2845E"];

const SMART_LISTS = [
  { key: "today" as const, label: "오늘", href: "/today", color: "#007AFF", icon: (
    <svg viewBox="0 0 24 24" fill="currentColor" className="w-[18px] h-[18px]"><path d="M6 2a1 1 0 012 0v1h8V2a1 1 0 112 0v1h1a3 3 0 013 3v12a3 3 0 01-3 3H5a3 3 0 01-3-3V6a3 3 0 013-3h1V2zm-1 7v9a1 1 0 001 1h12a1 1 0 001-1V9H5zm3 3a1 1 0 110 2 1 1 0 010-2z"/></svg>
  )},
  { key: "scheduled" as const, label: "예정", href: "/scheduled", color: "#FF3B30", icon: (
    <svg viewBox="0 0 24 24" fill="currentColor" className="w-[18px] h-[18px]"><path d="M12 2a10 10 0 110 20 10 10 0 010-20zm0 4a1 1 0 00-1 1v5a1 1 0 00.293.707l3 3a1 1 0 001.414-1.414L13 11.586V7a1 1 0 00-1-1z"/></svg>
  )},
  { key: "all" as const, label: "전체", href: "/all", color: "#5856D6", icon: (
    <svg viewBox="0 0 24 24" fill="currentColor" className="w-[18px] h-[18px]"><path d="M5 3h14a3 3 0 013 3v12a3 3 0 01-3 3H5a3 3 0 01-3-3V6a3 3 0 013-3zm1 5a1 1 0 100 2h12a1 1 0 100-2H6zm0 4a1 1 0 100 2h12a1 1 0 100-2H6zm0 4a1 1 0 100 2h8a1 1 0 100-2H6z"/></svg>
  )},
  { key: "flagged" as const, label: "깃발 표시", href: "/flagged", color: "#FF9500", icon: (
    <svg viewBox="0 0 24 24" fill="currentColor" className="w-[18px] h-[18px]"><path d="M5 2a1 1 0 011 1v1h11.5a1.5 1.5 0 011.2 2.4L15.5 11l3.2 4.6a1.5 1.5 0 01-1.2 2.4H6v3a1 1 0 11-2 0V3a1 1 0 011-1z"/></svg>
  )},
  { key: "completed" as const, label: "완료됨", href: "/completed", color: "#8E8E93", icon: (
    <svg viewBox="0 0 24 24" fill="currentColor" className="w-[18px] h-[18px]"><path d="M12 2a10 10 0 110 20 10 10 0 010-20zm4.707 7.293a1 1 0 00-1.414 0L11 13.586l-2.293-2.293a1 1 0 00-1.414 1.414l3 3a1 1 0 001.414 0l5-5a1 1 0 000-1.414z"/></svg>
  )},
];

function SortableListItem({ list, pathname, onEdit, onNavigate }: {
  list: ReminderList; pathname: string; onEdit: (list: ReminderList) => void; onNavigate?: () => void;
}) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({ id: list.id });
  const style = { transform: CSS.Transform.toString(transform), transition, opacity: isDragging ? 0.5 : 1 };
  const active = pathname === `/lists/${list.id}`;

  return (
    <div ref={setNodeRef} style={style} {...attributes} {...listeners}>
      <div className="group flex items-center">
        <Link
          href={`/lists/${list.id}`} onClick={onNavigate}
          className={`flex-1 flex items-center gap-3 px-3 py-[7px] rounded-[10px] text-[14px] transition-apple ${
            active ? "bg-system-blue/12 font-medium" : "hover:bg-text-primary/[0.04] active:bg-text-primary/[0.08]"
          }`}
        >
          <span className="w-[26px] h-[26px] rounded-full flex items-center justify-center flex-shrink-0" style={{ backgroundColor: list.color || "#007AFF" }}>
            <svg className="w-[13px] h-[13px] text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={3}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M4 6h16M4 12h16M4 18h8" />
            </svg>
          </span>
          <span className={`flex-1 truncate ${active ? "text-system-blue" : "text-text-primary"}`}>{list.name}</span>
        </Link>
        <button
          onClick={(e) => { e.stopPropagation(); onEdit(list); }}
          className="p-1.5 rounded-lg text-text-tertiary hover:text-text-secondary opacity-0 group-hover:opacity-100 transition-all"
        >
          <svg className="w-[14px] h-[14px]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M6.75 12a.75.75 0 11-1.5 0 .75.75 0 011.5 0zM12.75 12a.75.75 0 11-1.5 0 .75.75 0 011.5 0zM18.75 12a.75.75 0 11-1.5 0 .75.75 0 011.5 0z" />
          </svg>
        </button>
      </div>
    </div>
  );
}

export default function Sidebar({ onNavigate }: { onNavigate?: () => void }) {
  const pathname = usePathname();
  const router = useRouter();
  const [lists, setLists] = useState<ReminderList[]>([]);
  const [counts, setCounts] = useState<SmartListCounts | null>(null);
  const [adding, setAdding] = useState(false);
  const [newName, setNewName] = useState("");
  const [newColor, setNewColor] = useState(COLORS[4]);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editName, setEditName] = useState("");
  const [editColor, setEditColor] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [searchResults, setSearchResults] = useState<Reminder[]>([]);
  const [searching, setSearching] = useState(false);
  const [groups, setGroups] = useState<Group[]>([]);
  const [addingGroup, setAddingGroup] = useState(false);
  const [newGroupName, setNewGroupName] = useState("");

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 5 } }),
    useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates }),
  );

  const [isDark, setIsDark] = useState(false);
  useEffect(() => { setIsDark(document.documentElement.classList.contains("dark")); }, []);

  function toggleDarkMode() {
    const dark = !isDark;
    document.documentElement.classList[dark ? "add" : "remove"]("dark");
    localStorage.setItem("theme", dark ? "dark" : "light");
    setIsDark(dark);
  }

  useEffect(() => {
    getLists().then(setLists).catch(() => {});
    getSmartListCounts().then(setCounts).catch(() => {});
    getGroups().then(setGroups).catch(() => {});
  }, []);

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    if (!newName.trim()) return;
    try {
      const created = await createList({ name: newName.trim(), color: newColor, sortOrder: lists.length });
      setLists((prev) => [...prev, created]);
      setNewName(""); setAdding(false);
      router.push(`/lists/${created.id}`); onNavigate?.();
    } catch { /* ignore */ }
  }

  async function handleUpdate(id: number) {
    if (!editName.trim()) return;
    try {
      const updated = await updateList(id, { name: editName.trim(), color: editColor });
      setLists((prev) => prev.map((l) => (l.id === updated.id ? updated : l)));
      setEditingId(null);
    } catch { /* ignore */ }
  }

  async function handleDelete(id: number) {
    try {
      await deleteList(id);
      setLists((prev) => prev.filter((l) => l.id !== id));
      if (pathname === `/lists/${id}`) router.push("/");
    } catch { /* ignore */ }
  }

  function startEdit(list: ReminderList) {
    setEditingId(list.id); setEditName(list.name); setEditColor(list.color || COLORS[4]);
  }

  async function handleCreateGroup(e: React.FormEvent) {
    e.preventDefault();
    if (!newGroupName.trim()) return;
    try {
      const created = await createGroup(newGroupName.trim());
      setGroups((prev) => [...prev, created]);
      setNewGroupName(""); setAddingGroup(false);
      router.push(`/groups/${created.id}`); onNavigate?.();
    } catch { /* ignore */ }
  }

  async function handleSearch(q: string) {
    setSearchQuery(q);
    if (q.trim().length === 0) { setSearchResults([]); setSearching(false); return; }
    setSearching(true);
    try { setSearchResults(await searchReminders(q.trim())); } catch { setSearchResults([]); }
  }

  async function handleListDragEnd(event: DragEndEvent) {
    const { active, over } = event;
    if (!over || active.id === over.id) return;
    const oldIdx = lists.findIndex((l) => l.id === active.id);
    const newIdx = lists.findIndex((l) => l.id === over.id);
    if (oldIdx === -1 || newIdx === -1) return;
    const reordered = [...lists];
    const [moved] = reordered.splice(oldIdx, 1);
    reordered.splice(newIdx, 0, moved);
    setLists(reordered);
    try { await reorderLists(reordered.map((l) => l.id)); } catch { getLists().then(setLists).catch(() => {}); }
  }

  return (
    <aside className="w-[280px] bg-bg-secondary/80 backdrop-blur-xl border-r border-separator h-screen flex flex-col">
      {/* Top bar */}
      <div className="px-4 pt-4 pb-2 flex items-center justify-between">
        <button onClick={toggleDarkMode} className="p-1.5 -ml-1.5 rounded-lg text-text-secondary hover:bg-text-primary/[0.05] transition-apple">
          {isDark ? (
            <svg className="w-[18px] h-[18px]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 3v2.25m6.364.386l-1.591 1.591M21 12h-2.25m-.386 6.364l-1.591-1.591M12 18.75V21m-4.773-4.227l-1.591 1.591M5.25 12H3m4.227-4.773L5.636 5.636M15.75 12a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0z" />
            </svg>
          ) : (
            <svg className="w-[18px] h-[18px]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M21.752 15.002A9.718 9.718 0 0118 15.75c-5.385 0-9.75-4.365-9.75-9.75 0-1.33.266-2.597.748-3.752A9.753 9.753 0 003 11.25C3 16.635 7.365 21 12.75 21a9.753 9.753 0 009.002-5.998z" />
            </svg>
          )}
        </button>
        <button onClick={() => { removeToken(); window.location.href = "/login"; }} className="p-1.5 -mr-1.5 rounded-lg text-text-secondary hover:bg-text-primary/[0.05] transition-apple">
          <svg className="w-[18px] h-[18px]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 9V5.25A2.25 2.25 0 0013.5 3h-6a2.25 2.25 0 00-2.25 2.25v13.5A2.25 2.25 0 007.5 21h6a2.25 2.25 0 002.25-2.25V15m3 0l3-3m0 0l-3-3m3 3H9" />
          </svg>
        </button>
      </div>

      {/* Search */}
      <div className="px-3 pb-2">
        <div className="relative">
          <svg className="absolute left-2.5 top-1/2 -translate-y-1/2 w-[14px] h-[14px] text-text-tertiary" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z" />
          </svg>
          <input
            type="text" value={searchQuery}
            onChange={(e) => handleSearch(e.target.value)}
            placeholder="검색"
            className="w-full pl-8 pr-3 py-[6px] text-[13px] bg-text-primary/[0.04] rounded-lg text-text-primary placeholder:text-text-tertiary focus:outline-none focus:bg-text-primary/[0.06] transition-apple"
          />
        </div>
      </div>

      <nav className="flex-1 overflow-y-auto px-3 pb-4">
        {searching ? (
          <div className="pt-1">
            <p className="px-3 py-1.5 text-[11px] font-semibold text-text-secondary uppercase tracking-wider">검색 결과</p>
            {searchResults.length === 0 ? (
              <p className="px-3 py-6 text-[13px] text-text-tertiary text-center">결과 없음</p>
            ) : (
              <div className="space-y-px">
                {searchResults.map((r) => (
                  <Link key={r.id} href={`/lists/${r.listId}`} onClick={onNavigate}
                    className="block px-3 py-2 rounded-[10px] text-[13px] text-text-primary hover:bg-text-primary/[0.04] transition-apple">
                    <p className={r.completed ? "line-through text-text-tertiary" : ""}>{r.title}</p>
                    {r.notes && <p className="text-[11px] text-text-tertiary truncate mt-0.5">{r.notes}</p>}
                  </Link>
                ))}
              </div>
            )}
          </div>
        ) : (
        <>
          {/* Smart lists */}
          <div className="space-y-px mb-5">
            {SMART_LISTS.map(({ key, label, href, color, icon }) => {
              const active = pathname === href;
              const count = counts?.[key];
              return (
                <Link key={key} href={href} onClick={onNavigate}
                  className={`flex items-center gap-3 px-3 py-[7px] rounded-[10px] text-[14px] transition-apple ${
                    active ? "bg-system-blue/12 font-medium" : "hover:bg-text-primary/[0.04] active:bg-text-primary/[0.08]"
                  }`}>
                  <span className="w-[26px] h-[26px] rounded-full flex items-center justify-center flex-shrink-0" style={{ backgroundColor: color }}>
                    <span className="text-white">{icon}</span>
                  </span>
                  <span className={`flex-1 ${active ? "text-system-blue" : "text-text-primary"}`}>{label}</span>
                  {count !== undefined && count > 0 && (
                    <span className="font-rounded text-[13px] font-semibold text-text-secondary tabular-nums">{count}</span>
                  )}
                </Link>
              );
            })}
          </div>

          {/* My lists */}
          <div className="mb-1">
            <p className="px-3 py-1.5 text-[11px] font-semibold text-text-secondary uppercase tracking-wider">나의 목록</p>
          </div>

          <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={handleListDragEnd}>
            <SortableContext items={lists.map((l) => l.id)} strategy={verticalListSortingStrategy}>
              <div className="space-y-px">
                {lists.map((list) =>
                  editingId === list.id ? (
                    <div key={list.id} className="p-3 bg-bg-tertiary rounded-[10px] space-y-2.5">
                      <input type="text" value={editName} onChange={(e) => setEditName(e.target.value)}
                        onKeyDown={(e) => { if (e.key === "Enter") handleUpdate(list.id); if (e.key === "Escape") setEditingId(null); }}
                        className="w-full px-3 py-1.5 text-[13px] bg-bg-secondary rounded-lg text-text-primary focus:outline-none focus:ring-1 focus:ring-system-blue/30"
                        autoFocus />
                      <div className="flex gap-[6px] flex-wrap">
                        {COLORS.map((c) => (
                          <button key={c} onClick={() => setEditColor(c)}
                            className={`w-[22px] h-[22px] rounded-full transition-apple ${editColor === c ? "ring-2 ring-offset-1 ring-system-blue ring-offset-bg-tertiary scale-110" : ""}`}
                            style={{ backgroundColor: c }} />
                        ))}
                      </div>
                      <div className="flex gap-2">
                        <button onClick={() => handleUpdate(list.id)} className="px-3 py-1 text-[12px] font-medium bg-system-blue text-white rounded-lg transition-apple">저장</button>
                        <button onClick={() => setEditingId(null)} className="px-3 py-1 text-[12px] text-text-secondary rounded-lg transition-apple">취소</button>
                        <button onClick={() => { if (confirm(`"${list.name}" 목록을 삭제할까요?`)) handleDelete(list.id); }}
                          className="px-3 py-1 text-[12px] text-system-red rounded-lg transition-apple ml-auto">삭제</button>
                      </div>
                    </div>
                  ) : (
                    <SortableListItem key={list.id} list={list} pathname={pathname} onEdit={startEdit} onNavigate={onNavigate} />
                  )
                )}
              </div>
            </SortableContext>
          </DndContext>

          {adding ? (
            <form onSubmit={handleCreate} className="mt-1 p-3 bg-bg-tertiary rounded-[10px] space-y-2.5">
              <input type="text" value={newName} onChange={(e) => setNewName(e.target.value)}
                onKeyDown={(e) => { if (e.key === "Escape") { setAdding(false); setNewName(""); } }}
                placeholder="목록 이름" autoFocus
                className="w-full px-3 py-1.5 text-[13px] bg-bg-secondary rounded-lg text-text-primary placeholder:text-text-tertiary focus:outline-none focus:ring-1 focus:ring-system-blue/30" />
              <div className="flex gap-[6px] flex-wrap">
                {COLORS.map((c) => (
                  <button key={c} type="button" onClick={() => setNewColor(c)}
                    className={`w-[22px] h-[22px] rounded-full transition-apple ${newColor === c ? "ring-2 ring-offset-1 ring-system-blue ring-offset-bg-tertiary scale-110" : ""}`}
                    style={{ backgroundColor: c }} />
                ))}
              </div>
              <div className="flex gap-2">
                <button type="submit" className="px-3 py-1 text-[12px] font-medium bg-system-blue text-white rounded-lg transition-apple">추가</button>
                <button type="button" onClick={() => { setAdding(false); setNewName(""); }}
                  className="px-3 py-1 text-[12px] text-text-secondary rounded-lg transition-apple">취소</button>
              </div>
            </form>
          ) : (
            <button onClick={() => setAdding(true)}
              className="flex items-center gap-2 px-3 py-[7px] mt-1 text-[14px] text-system-blue hover:bg-text-primary/[0.04] rounded-[10px] w-full transition-apple font-medium">
              <svg className="w-[16px] h-[16px]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
              </svg>
              목록 추가
            </button>
          )}

          {/* Groups */}
          {(groups.length > 0 || addingGroup) && (
            <div className="mt-5">
              <p className="px-3 py-1.5 text-[11px] font-semibold text-text-secondary uppercase tracking-wider">그룹</p>
            </div>
          )}

          {groups.map((group) => (
            <Link key={group.id} href={`/groups/${group.id}`} onClick={onNavigate}
              className={`flex items-center gap-3 px-3 py-[7px] rounded-[10px] text-[14px] transition-apple ${
                pathname === `/groups/${group.id}` ? "bg-system-blue/12 font-medium text-system-blue" : "text-text-primary hover:bg-text-primary/[0.04]"
              }`}>
              <span className="w-[26px] h-[26px] rounded-full bg-system-purple flex items-center justify-center flex-shrink-0">
                <svg className="w-[14px] h-[14px] text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M15 19.128a9.38 9.38 0 002.625.372 9.337 9.337 0 004.121-.952 4.125 4.125 0 00-7.533-2.493M15 19.128v-.003c0-1.113-.285-2.16-.786-3.07M15 19.128v.106A12.318 12.318 0 018.624 21c-2.331 0-4.512-.645-6.374-1.766l-.001-.109a6.375 6.375 0 0111.964-3.07M12 6.375a3.375 3.375 0 11-6.75 0 3.375 3.375 0 016.75 0zm8.25 2.25a2.625 2.625 0 11-5.25 0 2.625 2.625 0 015.25 0z" />
                </svg>
              </span>
              <span className="truncate">{group.name}</span>
            </Link>
          ))}

          {addingGroup ? (
            <form onSubmit={handleCreateGroup} className="mt-1 p-3 bg-bg-tertiary rounded-[10px] space-y-2">
              <input type="text" value={newGroupName} onChange={(e) => setNewGroupName(e.target.value)}
                onKeyDown={(e) => { if (e.key === "Escape") { setAddingGroup(false); setNewGroupName(""); } }}
                placeholder="그룹 이름" autoFocus
                className="w-full px-3 py-1.5 text-[13px] bg-bg-secondary rounded-lg text-text-primary placeholder:text-text-tertiary focus:outline-none focus:ring-1 focus:ring-system-blue/30" />
              <div className="flex gap-2">
                <button type="submit" className="px-3 py-1 text-[12px] font-medium bg-system-blue text-white rounded-lg">추가</button>
                <button type="button" onClick={() => { setAddingGroup(false); setNewGroupName(""); }} className="px-3 py-1 text-[12px] text-text-secondary rounded-lg">취소</button>
              </div>
            </form>
          ) : (
            <button onClick={() => setAddingGroup(true)}
              className="flex items-center gap-2 px-3 py-[7px] mt-1 text-[14px] text-system-blue hover:bg-text-primary/[0.04] rounded-[10px] w-full transition-apple font-medium">
              <svg className="w-[16px] h-[16px]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
              </svg>
              그룹 추가
            </button>
          )}
        </>
        )}
      </nav>
    </aside>
  );
}
