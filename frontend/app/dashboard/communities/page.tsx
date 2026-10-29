import type { Metadata } from "next";
import PlaceholderPage from "@/components/dashboard/PlaceholderPage";
import CommunityActivity from "@/components/dashboard/CommunityActivity";

export const metadata: Metadata = { title: "Communities · Samavya" };

export default function CommunitiesPage() {
  return (
    <PlaceholderPage
      eyebrow="COMMUNITIES"
      heading={<>The people <em>around you.</em></>}
      description="See what's happening in the groups you've joined and discover new ones nearby."
    >
      <CommunityActivity />
    </PlaceholderPage>
  );
}
