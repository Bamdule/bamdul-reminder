"use client";

import SmartListPage from "@/components/SmartListPage";
import { getTodayReminders } from "@/lib/api";

export default function TodayPage() {
  return <SmartListPage title="오늘" color="#007AFF" fetchReminders={getTodayReminders} />;
}
