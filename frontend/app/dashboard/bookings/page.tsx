import type { Metadata } from "next";
import PlaceholderPage from "@/components/dashboard/PlaceholderPage";
import BookingSummary from "@/components/dashboard/BookingSummary";

export const metadata: Metadata = { title: "Bookings · Samavya" };

export default function BookingsPage() {
  return (
    <PlaceholderPage
      eyebrow="BOOKINGS"
      heading={<>Everything you&apos;ve <em>scheduled.</em></>}
      description="A compact view of requested, accepted, in-progress, and completed bookings. Nothing moves without your confirmation."
    >
      <BookingSummary />
    </PlaceholderPage>
  );
}
