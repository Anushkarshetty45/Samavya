import type { Metadata } from "next";
import DashboardGreeting from "@/components/dashboard/DashboardGreeting";
import ConversationalAssistant from "@/components/dashboard/ConversationalAssistant";
import QuickActions from "@/components/dashboard/QuickActions";
import BookingSummary from "@/components/dashboard/BookingSummary";
import CommunityActivity from "@/components/dashboard/CommunityActivity";
import NearbyItems from "@/components/dashboard/NearbyItems";

export const metadata: Metadata = {
  title: "Overview · Samavya",
  description:
    "Your community at a glance — find what you need, see what's happening nearby, and start a conversation.",
};

export default function DashboardOverview() {
  return (
    <div className="dpage">
      <DashboardGreeting />
      <ConversationalAssistant
        heroStyle
        title="What do you need help with today?"
        placeholder="Find a plumber, rent a drill, or organise a group purchase…"
      />
      <QuickActions />
      <section className="dgrid-3" aria-label="Dashboard activity">
        <BookingSummary />
        <CommunityActivity />
        <NearbyItems />
      </section>
    </div>
  );
}
