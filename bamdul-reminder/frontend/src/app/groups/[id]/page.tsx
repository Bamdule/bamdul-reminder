"use client";

import { useEffect, useState, useCallback } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import {
  getGroups,
  getGroupMembers,
  getGroupLists,
  inviteGroupMember,
  removeGroupMember,
  updateGroupMemberPermission,
  updateGroup,
  deleteGroup,
} from "@/lib/api";
import { useRouter } from "next/navigation";
import type { Group, GroupMember, GroupPermission } from "@/types/group";
import type { ReminderList } from "@/types/reminder";

export default function GroupPage() {
  const params = useParams();
  const router = useRouter();
  const groupId = Number(params.id);
  const [group, setGroup] = useState<Group | null>(null);
  const [members, setMembers] = useState<GroupMember[]>([]);
  const [lists, setLists] = useState<ReminderList[]>([]);
  const [inviteEmail, setInviteEmail] = useState("");
  const [invitePermission, setInvitePermission] = useState<GroupPermission>("READ");
  const [inviting, setInviting] = useState(false);
  const [editing, setEditing] = useState(false);
  const [editName, setEditName] = useState("");
  const [error, setError] = useState("");

  const load = useCallback(() => {
    getGroups().then((groups) => {
      const found = groups.find((g) => g.id === groupId);
      if (found) setGroup(found);
    }).catch(() => {});
    getGroupMembers(groupId).then(setMembers).catch(() => {});
    getGroupLists(groupId).then(setLists).catch(() => {});
  }, [groupId]);

  useEffect(() => { load(); }, [load]);

  async function handleInvite(e: React.FormEvent) {
    e.preventDefault();
    if (!inviteEmail.trim()) return;
    setError("");
    try {
      const member = await inviteGroupMember(groupId, inviteEmail.trim(), invitePermission);
      setMembers((prev) => [...prev, member]);
      setInviteEmail("");
      setInviting(false);
    } catch (err) {
      setError(err instanceof Error ? err.message : "초대 실패");
    }
  }

  async function handleRemove(memberId: number) {
    if (!confirm("이 그룹원을 강퇴하시겠습니까?")) return;
    try {
      await removeGroupMember(groupId, memberId);
      setMembers((prev) => prev.filter((m) => m.memberId !== memberId));
    } catch { /* ignore */ }
  }

  async function handlePermissionChange(memberId: number, permission: GroupPermission) {
    try {
      const updated = await updateGroupMemberPermission(groupId, memberId, permission);
      setMembers((prev) => prev.map((m) => (m.memberId === memberId ? updated : m)));
    } catch { /* ignore */ }
  }

  async function handleUpdateGroup() {
    if (!editName.trim()) return;
    try {
      const updated = await updateGroup(groupId, editName.trim());
      setGroup(updated);
      setEditing(false);
    } catch { /* ignore */ }
  }

  async function handleDeleteGroup() {
    if (!confirm("이 그룹을 삭제하시겠습니까?")) return;
    try {
      await deleteGroup(groupId);
      router.push("/");
    } catch { /* ignore */ }
  }

  if (!group) {
    return (
      <div className="p-6 md:p-10 max-w-3xl mx-auto">
        <div className="bg-bg-secondary rounded-2xl p-10 text-center">
          <p className="text-text-tertiary text-[15px]">로딩 중...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6 md:p-10 max-w-3xl mx-auto">
      {/* Header */}
      <div className="mb-6">
        {editing ? (
          <div className="flex items-center gap-3">
            <input
              type="text"
              value={editName}
              onChange={(e) => setEditName(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter") handleUpdateGroup();
                if (e.key === "Escape") setEditing(false);
              }}
              className="flex-1 px-4 py-2 text-[22px] font-bold bg-bg-tertiary text-text-primary border-none rounded-xl focus:outline-none focus:ring-2 focus:ring-system-blue/40"
              autoFocus
            />
            <button onClick={handleUpdateGroup} className="px-4 py-2 text-[13px] font-medium bg-system-blue text-white rounded-full hover:opacity-90 transition-apple">저장</button>
            <button onClick={() => setEditing(false)} className="px-4 py-2 text-[13px] font-medium text-text-secondary hover:bg-bg-tertiary rounded-full transition-apple">취소</button>
          </div>
        ) : (
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-system-purple flex items-center justify-center flex-shrink-0">
              <svg className="w-5 h-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
            </div>
            <h1 className="font-rounded text-[34px] font-bold text-text-primary tracking-tight flex-1">{group.name}</h1>
            <button
              onClick={() => { setEditing(true); setEditName(group.name); }}
              className="p-2 rounded-full text-text-tertiary hover:text-system-blue hover:bg-system-blue/10 transition-apple"
            >
              <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
              </svg>
            </button>
            <button
              onClick={handleDeleteGroup}
              className="p-2 rounded-full text-text-tertiary hover:text-system-red hover:bg-system-red/10 transition-apple"
            >
              <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          </div>
        )}
        <p className="text-[13px] text-text-secondary mt-2 px-1">대표: {group.ownerName}</p>
      </div>

      {/* Group Lists */}
      <section className="mb-8">
        <p className="text-[11px] font-semibold text-text-secondary uppercase tracking-wider mb-2 px-1">그룹 목록</p>
        {lists.length === 0 ? (
          <div className="bg-bg-secondary rounded-2xl p-8 text-center">
            <p className="text-text-tertiary text-[13px]">목록이 없습니다</p>
          </div>
        ) : (
          <div className="bg-bg-secondary rounded-2xl overflow-hidden">
            {lists.map((list, i) => (
              <Link
                key={list.id}
                href={`/lists/${list.id}`}
                className={`flex items-center gap-3 px-4 py-3 hover:bg-text-primary/[0.03] transition-apple ${
                  i < lists.length - 1 ? "border-b border-separator" : ""
                }`}
              >
                <span className="w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0" style={{ backgroundColor: list.color || "#007AFF" }}>
                  <svg className="w-4 h-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M4 6h16M4 12h16M4 18h8" />
                  </svg>
                </span>
                <span className="flex-1 text-[15px] text-text-primary">{list.name}</span>
                <svg className="w-4 h-4 text-text-tertiary" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M8.25 4.5l7.5 7.5-7.5 7.5" />
                </svg>
              </Link>
            ))}
          </div>
        )}
      </section>

      {/* Group Members */}
      <section>
        <p className="text-[11px] font-semibold text-text-secondary uppercase tracking-wider mb-2 px-1">그룹원</p>
        <div className="bg-bg-secondary rounded-2xl overflow-hidden mb-4">
          {members.map((m, i) => (
            <div
              key={m.id}
              className={`flex items-center gap-3 px-4 py-3 ${
                i < members.length - 1 ? "border-b border-separator" : ""
              }`}
            >
              {/* Avatar */}
              <div className="w-8 h-8 rounded-full bg-system-gray/20 flex items-center justify-center flex-shrink-0">
                <svg className="w-4 h-4 text-system-gray" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-[15px] text-text-primary">{m.memberName}</p>
                <p className="text-[12px] text-text-secondary truncate">{m.memberEmail}</p>
              </div>
              <select
                value={m.permission}
                onChange={(e) => handlePermissionChange(m.memberId, e.target.value as GroupPermission)}
                className="text-[13px] px-3 py-1.5 bg-bg-tertiary text-text-primary border-none rounded-xl focus:outline-none focus:ring-2 focus:ring-system-blue/40"
              >
                <option value="READ">읽기</option>
                <option value="READ_WRITE">읽기/쓰기</option>
              </select>
              <button
                onClick={() => handleRemove(m.memberId)}
                className="p-1.5 rounded-lg text-text-tertiary hover:text-system-red hover:bg-system-red/10 transition-apple"
              >
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
          ))}
        </div>

        {/* Invite */}
        {inviting ? (
          <div className="bg-bg-secondary rounded-2xl p-4">
            {error && <p className="text-[13px] text-system-red mb-3 px-1">{error}</p>}
            <form onSubmit={handleInvite} className="space-y-3">
              <div className="flex gap-2">
                <input
                  type="email"
                  value={inviteEmail}
                  onChange={(e) => setInviteEmail(e.target.value)}
                  placeholder="이메일 주소"
                  className="flex-1 px-4 py-2.5 text-[15px] bg-bg-tertiary text-text-primary border-none rounded-xl focus:outline-none focus:ring-2 focus:ring-system-blue/40 placeholder:text-text-tertiary"
                  autoFocus
                  onKeyDown={(e) => { if (e.key === "Escape") setInviting(false); }}
                />
                <select
                  value={invitePermission}
                  onChange={(e) => setInvitePermission(e.target.value as GroupPermission)}
                  className="px-3 py-2.5 text-[13px] bg-bg-tertiary text-text-primary border-none rounded-xl focus:outline-none focus:ring-2 focus:ring-system-blue/40"
                >
                  <option value="READ">읽기</option>
                  <option value="READ_WRITE">읽기/쓰기</option>
                </select>
              </div>
              <div className="flex gap-2">
                <button type="submit" className="px-4 py-1.5 text-[13px] font-medium bg-system-blue text-white rounded-full hover:opacity-90 transition-apple">초대</button>
                <button type="button" onClick={() => { setInviting(false); setError(""); }} className="px-4 py-1.5 text-[13px] font-medium text-text-secondary hover:bg-bg-tertiary rounded-full transition-apple">취소</button>
              </div>
            </form>
          </div>
        ) : (
          <button
            onClick={() => setInviting(true)}
            className="flex items-center gap-2 text-[15px] font-medium text-system-blue px-1 transition-apple"
          >
            <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 2a10 10 0 110 20 10 10 0 010-20zm1 5a1 1 0 10-2 0v4H7a1 1 0 100 2h4v4a1 1 0 102 0v-4h4a1 1 0 100-2h-4V7z"/>
            </svg>
            그룹원 초대
          </button>
        )}
      </section>
    </div>
  );
}
