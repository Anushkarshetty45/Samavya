import type { Metadata } from "next";
import PlaceholderPage from "@/components/dashboard/PlaceholderPage";
import NearbyItems from "@/components/dashboard/NearbyItems";

export const metadata: Metadata = { title: "Marketplace · Samavya" };

export default function MarketplacePage() {
  return (
    <PlaceholderPage
      eyebrow="MARKETPLACE"
      heading={<>Buy, rent, and <em>share locally.</em></>}
      description="Browse what neighbours are selling, lending, or growing. Three ways to find value close by: local products, rentals, and group buying."
    >
      <NearbyItems />
    </PlaceholderPage>
  );
}
