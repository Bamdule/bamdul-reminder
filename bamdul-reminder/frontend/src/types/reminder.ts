export type Priority = "NONE" | "LOW" | "MEDIUM" | "HIGH";

export interface ReminderList {
  id: number;
  name: string;
  color: string | null;
  icon: string | null;
  sortOrder: number;
  createdAt: string;
  updatedAt: string;
}

export interface Reminder {
  id: number;
  listId: number;
  parentId: number | null;
  title: string;
  notes: string | null;
  dueDate: string | null;
  completed: boolean;
  completedAt: string | null;
  priority: Priority;
  flagged: boolean;
  sortOrder: number;
  createdAt: string;
  updatedAt: string;
  children: Reminder[];
}

export interface SmartListCounts {
  today: number;
  scheduled: number;
  all: number;
  flagged: number;
  completed: number;
}
