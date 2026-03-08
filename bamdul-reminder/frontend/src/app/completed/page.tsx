"use client";

import SmartListPage from "@/components/SmartListPage";
import { getCompletedReminders } from "@/lib/api";

export default function CompletedPage() {
  return <SmartListPage title="완료됨" color="#34C759" fetchReminders={getCompletedReminders} />;
}
