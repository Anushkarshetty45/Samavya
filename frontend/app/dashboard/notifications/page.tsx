import type { Metadata } from "next";
import PlaceholderPage from "@/components/dashboard/PlaceholderPage";

export const metadata: Metadata = { title: "Notifications · Samavya" };

const NOTIFS = [
  { when: "2 hours ago", text: "A provider accepted your kitchen tap booking." },
  { when: "Yesterday",   text: "The rice & pulses group buy needs 2 more members." },
  { when: "Yesterday",   text: "Weekend Clean-up Crew posted an update." },
  { when: "3 days ago",  text: "A neighbour added fresh seasonal vegetables." },
  { when: "4 days ago",  text: "Monsoon Repair Day announced in your neighbourhood." },
];

export default function NotificationsPage() {
  return (
    <PlaceholderPage
      eyebrow="NOTIFICATIONS"
      heading={<>What&apos;s been <em>happening.</em></>}
      description="Only the things that need your attention. Nothing routed outward, nothing hidden."
    >
      <ul className="dash-list">
        {NOTIFS.map((n, i) => (
          <li key={i} className="dash-list-item">
            <p className="dash-list-title">{n.text}</p>
            <span className="dash-list-meta">{n.when}</span>
          </li>
        ))}
      </ul>
    </PlaceholderPage>
  );
}
