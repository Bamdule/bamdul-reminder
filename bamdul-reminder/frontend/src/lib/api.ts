import { getToken, removeToken } from "./auth";
import type { AuthResult } from "@/types/auth";
import type { Reminder, ReminderList } from "@/types/reminder";

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
