"use client";

import SmartListPage from "@/components/SmartListPage";
import { getFlaggedReminders } from "@/lib/api";

export default function FlaggedPage() {
  return <SmartListPage title="깃발 표시" color="#FF9500" fetchReminders={getFlaggedReminders} />;
}
