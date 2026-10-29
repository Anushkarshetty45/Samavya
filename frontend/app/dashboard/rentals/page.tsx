import type { Metadata } from "next";
import PlaceholderPage from "@/components/dashboard/PlaceholderPage";

export const metadata: Metadata = { title: "Rentals · Samavya" };

export default function RentalsPage() {
  return (
    <PlaceholderPage
      eyebrow="RENTALS"
      heading={<>Borrow before <em>you buy.</em></>}
      description="Tools, appliances and small equipment available from people on your street. Rentals are coordinated by neighbours — nothing is listed until the owner says so."
    >
      <div className="dash-empty">
        <p className="dash-empty-title">No active rentals yet.</p>
        <p className="dash-empty-text">
          When you rent something or list an item of your own, it will appear here.
          This page is a frontend placeholder — rental logic is not implemented.
        </p>
      </div>
    </PlaceholderPage>
  );
}
