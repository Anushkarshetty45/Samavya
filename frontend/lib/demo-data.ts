// Central source of frontend-only demo content for the Samavya dashboard.
// No backend wiring, no real users. All items clearly placeholder.

export type DemoUser = {
  firstName: string;
  neighbourhood: string;
};

export const demoUser: DemoUser = {
  firstName: "Anushka",
  neighbourhood: "Golden Courtyard",
};

export type NavItem = {
  href: string;
  label: string;
};

/** Top horizontal navigation — 4 items: Home / Services / Marketplace / Communities.
 *  Overview is the dashboard home (/dashboard). Bookings + settings are reached
 *  through the profile dropdown or inline links on the overview page. */
export const topNav: NavItem[] = [
  { href: "/dashboard",             label: "Home" },
  { href: "/dashboard/services",    label: "Services" },
  { href: "/dashboard/marketplace", label: "Marketplace" },
  { href: "/dashboard/communities", label: "Communities" },
];

export const examplePrompts = [
  "Find a plumber near me",
  "I need to rent a drill",
  "Find local vegetables",
  "Can we organise a group purchase?",
] as const;

export type QuickAction = {
  href: string;
  label: string;
  helper: string;
  image: string;
  alt: string;
};

/** Four quick-action photo tiles — label + helper line + small cropped photo. */
export const quickActions: QuickAction[] = [
  {
    href: "/dashboard/services",
    label: "Find a service",
    helper: "Verified local providers",
    image: "/images/dashboard/crops/drill-stool.jpg",
    alt: "A drill resting on a wooden stool",
  },
  {
    href: "/dashboard/marketplace",
    label: "Browse marketplace",
    helper: "What neighbours are sharing",
    image: "/images/dashboard/crops/veg-crate.jpg",
    alt: "A crate of fresh vegetables",
  },
  {
    href: "/dashboard/communities",
    label: "Explore communities",
    helper: "Groups near your street",
    image: "/images/dashboard/crops/arched-courtyard.jpg",
    alt: "An arched courtyard with flowers",
  },
  {
    href: "/dashboard/bookings",
    label: "View my bookings",
    helper: "Upcoming and active",
    image: "/images/dashboard/crops/arched-hall.jpg",
    alt: "An arched Mediterranean corridor",
  },
];

export type Booking = {
  id: string;
  title: string;
  provider: string;
  scheduled: string;
  status: "Requested" | "Accepted" | "In progress" | "Completed";
  image: string;
};

export const upcomingBookings: Booking[] = [
  {
    id: "b1",
    title: "Plumbing service",
    provider: "Verified provider",
    scheduled: "Thu, 30 Oct · 10:00",
    status: "Accepted",
    image: "/images/dashboard/crops/mb1-r1-faucet.jpg",
  },
  {
    id: "b2",
    title: "Deep cleaning",
    provider: "Verified provider",
    scheduled: "Sun, 02 Nov · 09:00",
    status: "Requested",
    image: "/images/dashboard/crops/stone-stairs.jpg",
  },
];

export type ActivityItem = {
  id: string;
  description: string;
  category: "Services" | "Marketplace" | "Group buying" | "Community";
  timeAgo: string;
  image: string;
};

export const communityActivity: ActivityItem[] = [
  {
    id: "a1",
    description: "A neighbour is looking for an electrician this week.",
    category: "Services",
    timeAgo: "2 hours ago",
    image: "/images/dashboard/crops/mb1-r1-arch-plant.jpg",
  },
  {
    id: "a2",
    description: "Local producer added fresh seasonal vegetables.",
    category: "Marketplace",
    timeAgo: "Yesterday",
    image: "/images/dashboard/crops/veg-crate.jpg",
  },
  {
    id: "a3",
    description: "Community event announced: Monsoon Repair Day.",
    category: "Community",
    timeAgo: "4 days ago",
    image: "/images/dashboard/crops/stucco-closeup.jpg",
  },
];

export type NearbyItem = {
  id: string;
  title: string;
  price: string;
  image: string;
};

export const nearbyItems: NearbyItem[] = [
  { id: "n1", title: "Lemons from a nearby grove",       price: "₹80 / kg",   image: "/images/dashboard/crops/lemon-detail.jpg" },
  { id: "n2", title: "Fresh grains and pulses",          price: "₹150 / kg",  image: "/images/dashboard/crops/grain-basket.jpg" },
  { id: "n3", title: "Woven lantern (rent)",             price: "₹120 / day", image: "/images/dashboard/crops/lantern.jpg" },
];

export function greetingFromHour(hour: number): string {
  if (hour < 12) return "Good morning";
  if (hour < 17) return "Good afternoon";
  return "Good evening";
}
