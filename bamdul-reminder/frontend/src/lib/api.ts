import { getToken, removeToken } from "./auth";
import type { AuthResult } from "@/types/auth";
import type { Group, GroupMember, GroupPermission } from "@/types/group";
import type { Reminder, ReminderList, SmartListCounts } from "@/types/reminder";

const BASE_URL = "http://localhost:8081/api";

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = getToken();
  const headers: HeadersInit = {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };

  const res = await fetch(`${BASE_URL}${path}`, { ...options, headers });

  if (res.status === 401) {
    removeToken();
    window.location.href = "/login";
    throw new Error("Unauthorized");
  }

  if (!res.ok) {
    const body = await res.json().catch(() => null);
    throw new Error(body?.detail || `Request failed: ${res.status}`);
  }

  if (res.status === 204) return undefined as T;
  return res.json();
}

// Auth
export function signup(email: string, password: string, name: string): Promise<AuthResult> {
  return request("/auth/signup", {
    method: "POST",
    body: JSON.stringify({ email, password, name }),
  });
}

export function login(email: string, password: string): Promise<AuthResult> {
  return request("/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
}

// Lists
export function getLists(): Promise<ReminderList[]> {
  return request("/lists");
}

export function createList(data: { name: string; color?: string; icon?: string; sortOrder?: number }): Promise<ReminderList> {
  return request("/lists", { method: "POST", body: JSON.stringify(data) });
}

export function updateList(id: number, data: { name: string; color?: string; icon?: string }): Promise<ReminderList> {
  return request(`/lists/${id}`, { method: "PUT", body: JSON.stringify(data) });
}

export function deleteList(id: number): Promise<void> {
  return request(`/lists/${id}`, { method: "DELETE" });
}

// Reminders
export function getReminders(listId: number): Promise<Reminder[]> {
  return request(`/lists/${listId}/reminders`);
}

export function createReminder(listId: number, data: {
  title: string; notes?: string; dueDate?: string; priority?: string; flagged?: boolean; sortOrder?: number; parentId?: number;
}): Promise<Reminder> {
  return request(`/lists/${listId}/reminders`, { method: "POST", body: JSON.stringify(data) });
}

export function updateReminder(id: number, data: {
  title: string; notes?: string; dueDate?: string; priority?: string; flagged?: boolean;
}): Promise<Reminder> {
  return request(`/reminders/${id}`, { method: "PUT", body: JSON.stringify(data) });
}

export function deleteReminder(id: number): Promise<void> {
  return request(`/reminders/${id}`, { method: "DELETE" });
}

export function toggleReminder(id: number): Promise<Reminder> {
  return request(`/reminders/${id}/toggle`, { method: "PATCH" });
}

// Smart Lists
export function getTodayReminders(): Promise<Reminder[]> {
  return request("/reminders/today");
}

export function getScheduledReminders(): Promise<Reminder[]> {
  return request("/reminders/scheduled");
}

export function getAllReminders(): Promise<Reminder[]> {
  return request("/reminders/all");
}

export function getFlaggedReminders(): Promise<Reminder[]> {
  return request("/reminders/flagged");
}

export function getCompletedReminders(): Promise<Reminder[]> {
  return request("/reminders/completed");
}

export function getSmartListCounts(): Promise<SmartListCounts> {
  return request("/reminders/counts");
}

export function reorderReminders(ids: number[]): Promise<void> {
  return request("/reminders/reorder", { method: "PATCH", body: JSON.stringify({ ids }) });
}

export function reorderLists(ids: number[]): Promise<void> {
  return request("/lists/reorder", { method: "PATCH", body: JSON.stringify({ ids }) });
}

export function searchReminders(q: string): Promise<Reminder[]> {
  return request(`/reminders/search?q=${encodeURIComponent(q)}`);
}

// Groups
export function getGroups(): Promise<Group[]> {
  return request("/groups");
}

export function createGroup(name: string): Promise<Group> {
  return request("/groups", { method: "POST", body: JSON.stringify({ name }) });
}

export function updateGroup(id: number, name: string): Promise<Group> {
  return request(`/groups/${id}`, { method: "PUT", body: JSON.stringify({ name }) });
}

export function deleteGroup(id: number): Promise<void> {
  return request(`/groups/${id}`, { method: "DELETE" });
}

export function getGroupMembers(groupId: number): Promise<GroupMember[]> {
  return request(`/groups/${groupId}/members`);
}

export function inviteGroupMember(groupId: number, email: string, permission: GroupPermission): Promise<GroupMember> {
  return request(`/groups/${groupId}/members`, { method: "POST", body: JSON.stringify({ email, permission }) });
}

export function removeGroupMember(groupId: number, memberId: number): Promise<void> {
  return request(`/groups/${groupId}/members/${memberId}`, { method: "DELETE" });
}

export function updateGroupMemberPermission(groupId: number, memberId: number, permission: GroupPermission): Promise<GroupMember> {
  return request(`/groups/${groupId}/members/${memberId}/permission`, { method: "PATCH", body: JSON.stringify({ permission }) });
}

export function getGroupLists(groupId: number): Promise<ReminderList[]> {
  return request(`/groups/${groupId}/lists`);
}
