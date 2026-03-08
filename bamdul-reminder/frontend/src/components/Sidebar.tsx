"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { getLists, createList, updateList, deleteList } from "@/lib/api";
import { removeToken } from "@/lib/auth";
import type { ReminderList } from "@/types/reminder";

const COLORS = ["#FF3B30", "#FF9500", "#FFCC00", "#34C759", "#007AFF", "#5856D6", "#AF52DE", "#FF2D55"];

export default function Sidebar() {
  const pathname = usePathname();
  const router = useRouter();
  const [lists, setLists] = useState<ReminderList[]>([]);
  const [adding, setAdding] = useState(false);
  const [newName, setNewName] = useState("");
  const [newColor, setNewColor] = useState(COLORS[4]);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editName, setEditName] = useState("");
  const [editColor, setEditColor] = useState("");

  useEffect(() => {
    getLists().then(setLists).catch(() => {});
  }, []);

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    if (!newName.trim()) return;
    try {
      const created = await createList({ name: newName.trim(), color: newColor, sortOrder: lists.length });
      setLists((prev) => [...prev, created]);
      setNewName("");
      setAdding(false);
      router.push(`/lists/${created.id}`);
    } catch {
      // ignore
    }
  }

  async function handleUpdate(id: number) {
    if (!editName.trim()) return;
    try {
      const updated = await updateList(id, { name: editName.trim(), color: editColor });
      setLists((prev) => prev.map((l) => (l.id === updated.id ? updated : l)));
      setEditingId(null);
    } catch {
      // ignore
    }
  }

  async function handleDelete(id: number) {
    try {
      await deleteList(id);
      setLists((prev) => prev.filter((l) => l.id !== id));
      if (pathname === `/lists/${id}`) router.push("/");
    } catch {
      // ignore
    }
  }

  function startEdit(list: ReminderList) {
    setEditingId(list.id);
    setEditName(list.name);
    setEditColor(list.color || COLORS[4]);
  }

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

        {lists.map((list) =>
          editingId === list.id ? (
            <div key={list.id} className="px-2 py-1 space-y-1">
              <input
                type="text"
                value={editName}
                onChange={(e) => setEditName(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === "Enter") handleUpdate(list.id);
                  if (e.key === "Escape") setEditingId(null);
                }}
                className="w-full px-2 py-1 text-sm border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                autoFocus
              />
              <div className="flex gap-1">
                {COLORS.map((c) => (
                  <button
                    key={c}
                    onClick={() => setEditColor(c)}
                    className={`w-5 h-5 rounded-full border-2 ${editColor === c ? "border-gray-800" : "border-transparent"}`}
                    style={{ backgroundColor: c }}
                  />
                ))}
              </div>
              <div className="flex gap-1">
                <button onClick={() => handleUpdate(list.id)} className="px-2 py-0.5 text-xs bg-blue-500 text-white rounded">저장</button>
                <button onClick={() => setEditingId(null)} className="px-2 py-0.5 text-xs text-gray-500 hover:bg-gray-200 rounded">취소</button>
                <button onClick={() => { if (confirm(`"${list.name}" 목록을 삭제하시겠습니까?`)) handleDelete(list.id); }} className="px-2 py-0.5 text-xs text-red-500 hover:bg-red-50 rounded ml-auto">삭제</button>
              </div>
            </div>
          ) : (
            <div key={list.id} className="group flex items-center">
              <Link
                href={`/lists/${list.id}`}
                className={`flex-1 flex items-center gap-2 px-3 py-2 rounded-lg text-sm ${
                  pathname === `/lists/${list.id}` ? "bg-blue-100 text-blue-700 font-medium" : "text-gray-700 hover:bg-gray-200"
                }`}
              >
                {list.color && (
                  <span className="w-3 h-3 rounded-full flex-shrink-0" style={{ backgroundColor: list.color }} />
                )}
                <span className="truncate">{list.name}</span>
              </Link>
              <button
                onClick={() => startEdit(list)}
                className="p-1 text-gray-400 hover:text-gray-600 opacity-0 group-hover:opacity-100 transition-opacity"
                title="편집"
              >
                <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
                </svg>
              </button>
            </div>
          )
        )}

        {adding ? (
          <form onSubmit={handleCreate} className="px-2 py-1 space-y-1">
            <input
              type="text"
              value={newName}
              onChange={(e) => setNewName(e.target.value)}
              onKeyDown={(e) => { if (e.key === "Escape") { setAdding(false); setNewName(""); } }}
              placeholder="목록 이름"
              className="w-full px-2 py-1 text-sm border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
              autoFocus
            />
            <div className="flex gap-1">
              {COLORS.map((c) => (
                <button
                  key={c}
                  type="button"
                  onClick={() => setNewColor(c)}
                  className={`w-5 h-5 rounded-full border-2 ${newColor === c ? "border-gray-800" : "border-transparent"}`}
                  style={{ backgroundColor: c }}
                />
              ))}
            </div>
            <div className="flex gap-1">
              <button type="submit" className="px-2 py-0.5 text-xs bg-blue-500 text-white rounded">추가</button>
              <button type="button" onClick={() => { setAdding(false); setNewName(""); }} className="px-2 py-0.5 text-xs text-gray-500 hover:bg-gray-200 rounded">취소</button>
            </div>
          </form>
        ) : (
          <button
            onClick={() => setAdding(true)}
            className="flex items-center gap-2 px-3 py-2 text-sm text-blue-500 hover:bg-gray-200 rounded-lg w-full"
          >
            + 목록 추가
          </button>
        )}
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

function SidebarLink({ href, label, active }: { href: string; label: string; active: boolean }) {
  return (
    <Link
      href={href}
      className={`flex items-center gap-2 px-3 py-2 rounded-lg text-sm ${
        active ? "bg-blue-100 text-blue-700 font-medium" : "text-gray-700 hover:bg-gray-200"
      }`}
    >
      <span className="truncate">{label}</span>
    </Link>
  );
}
