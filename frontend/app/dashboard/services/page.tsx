import type { Metadata } from "next";
import ServicesPanel from "@/components/dashboard/ServicesPanel";

export const metadata: Metadata = { title: "Services · Samavya" };

export default function ServicesPage() {
  return <ServicesPanel />;
}
