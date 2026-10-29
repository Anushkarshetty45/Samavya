import type { Metadata } from "next";
import PlaceholderPage from "@/components/dashboard/PlaceholderPage";

export const metadata: Metadata = { title: "Group Buying · Samavya" };

const GROUPS = [
  { title: "Rice & pulses · bulk order",  members: "6 joined · 2 needed", deadline: "Closes Sun" },
  { title: "Mango crate · six-way split", members: "3 joined · 3 needed", deadline: "Closes Fri" },
  { title: "Monsoon tarpaulin batch",     members: "9 joined · open",      deadline: "Open until full" },
];

export default function GroupBuyingPage() {
  return (
    <PlaceholderPage
      eyebrow="GROUP BUYING"
      heading={<>Better prices, <em>bought together.</em></>}
      description="Join an existing group buy or propose a new one. Everyone splits cleanly and nobody has to do the arithmetic."
    >
      <ul className="dash-list">
        {GROUPS.map((g) => (
          <li key={g.title} className="dash-list-item">
            <div>
              <p className="dash-list-title">{g.title}</p>
              <p className="dash-list-sub">{g.members}</p>
            </div>
            <span className="dash-list-meta">{g.deadline}</span>
          </li>
        ))}
      </ul>
    </PlaceholderPage>
  );
}
