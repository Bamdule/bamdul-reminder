"use client";

import SmartListPage from "@/components/SmartListPage";
import { getScheduledReminders } from "@/lib/api";

export default function ScheduledPage() {
  return <SmartListPage title="예정" color="#FF3B30" fetchReminders={getScheduledReminders} />;
}
