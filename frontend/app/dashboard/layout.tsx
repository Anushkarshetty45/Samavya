import type { ReactNode } from "react";
import DashboardNavigation from "@/components/dashboard/DashboardNavigation";

export default function DashboardLayout({ children }: { children: ReactNode }) {
  return (
    <div className="dash-root">
      <DashboardNavigation />
      <main className="dash-main">{children}</main>
    </div>
  );
}
