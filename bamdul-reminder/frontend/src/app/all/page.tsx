"use client";

import SmartListPage from "@/components/SmartListPage";
import { getAllReminders } from "@/lib/api";

export default function AllPage() {
  return <SmartListPage title="전체" color="#5856D6" fetchReminders={getAllReminders} />;
}
