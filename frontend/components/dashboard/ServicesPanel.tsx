"use client";

import Link from "next/link";
import { motion } from "motion/react";
import PlaceholderPage from "./PlaceholderPage";
import ConversationalAssistant from "./ConversationalAssistant";

const easeOut = [0.16, 1, 0.3, 1] as const;

const CATEGORIES = [
  { label: "Home repairs",     desc: "Plumbing, electrical, carpentry" },
  { label: "Cleaning",         desc: "Home and deep cleaning" },
  { label: "Appliance repair", desc: "Fridges, washing machines, more" },
  { label: "Vehicle",          desc: "Service, pick-up, repair" },
  { label: "Cooking",          desc: "Home cooks and tiffin services" },
  { label: "Tutoring",         desc: "Academic help and lessons" },
];

/**
 * Full service catalogue (grouped) — used only by the assistant's reply
 * engine so users can ask things like "what services do you offer?" and
 * get a complete, grouped answer back without listing them all on screen.
 */
const SERVICE_INDEX: Record<string, string[]> = {
  "Home repairs":        ["Plumbing", "Electrical", "Carpentry", "Painting", "Masonry", "Welding", "Roof & waterproofing"],
  "Cleaning & upkeep":   ["Home cleaning", "Deep cleaning", "Sofa & carpet", "Water tank cleaning", "Pest control", "Laundry & ironing"],
  "Appliance & tech":    ["AC service", "Fridge", "Washing machine", "Geyser", "Microwave & oven", "TV & electronics", "Computer & Wi-Fi help"],
  "Daily living":        ["Home cooking", "Tiffin", "Grocery pickup", "Milk & essentials", "Gardening", "Pet care", "Tailoring"],
  "Vehicle":             ["Bike service", "Car service", "Puncture & roadside", "Pick-up & drop", "Driver on call"],
  "Learning":            ["Academic tutoring", "Music lessons", "Yoga & fitness", "Language classes", "Art & craft", "Exam coaching"],
  "Care & wellness":     ["Elder care", "Child care", "Physiotherapy", "Nursing visits", "Beauty at home", "Haircuts & grooming"],
  "Events & occasions":  ["Event help", "Catering", "Photography", "Decoration", "Priest & rituals", "Rentals (chairs, lights, tents)"],
  "Moving & logistics":  ["Packers & movers", "Local pick-up", "Furniture assembly", "Heavy lifting", "Storage help"],
};

const PROMPTS = [
  "What services do you offer?",
  "Do you do tailoring?",
  "I need an AC repair",
  "Find a cook near me",
] as const;

/** Reply engine: searches the full catalogue by keyword and returns a scoped answer. */
function servicesReply(prompt: string): string {
  const q = prompt.toLowerCase().trim();

  // "list / show / what / all services" → full catalogue summary
  if (/\b(what|which|all|list|show|every|offer|provide|catalog(ue)?|options?)\b/.test(q)) {
    const groups = Object.entries(SERVICE_INDEX)
      .map(([group, items]) => `${group}: ${items.slice(0, 3).join(", ")}${items.length > 3 ? ", …" : ""}`)
      .join(" · ");
    return `Samavya covers nine groups of services. ${groups}. Tell me what you need and I'll shortlist nearby providers for your review.`;
  }

  // Keyword search across the catalogue
  const matches: { group: string; item: string }[] = [];
  for (const [group, items] of Object.entries(SERVICE_INDEX)) {
    for (const item of items) {
      if (item.toLowerCase().includes(q) || q.includes(item.toLowerCase().split(" ")[0])) {
        matches.push({ group, item });
      }
    }
  }

  if (matches.length === 1) {
    const { group, item } = matches[0];
    return `Yes — ${item} is listed under ${group}. I can shortlist nearby providers; nothing is booked until you confirm.`;
  }
  if (matches.length > 1) {
    const top = matches.slice(0, 4).map((m) => m.item).join(", ");
    return `A few things in the catalogue match that: ${top}. Which one did you have in mind?`;
  }

  // Common intents without a direct keyword match
  if (/\b(book|schedule|appoint|visit)\b/.test(q)) {
    return "I can set up a request and ask nearby providers for availability. Share a day or window that works and I'll draft the request for you to confirm.";
  }
  if (/\b(cost|price|charge|rate|fees?)\b/.test(q)) {
    return "Prices vary by provider and the specifics of your request. I'll gather quotes from nearby providers for you to compare before anything is booked.";
  }

  return "Describe what you need in a sentence or two and I'll find matching services from your neighbourhood. Nothing is sent outward until you confirm.";
}

export default function ServicesPanel() {
  return (
    <PlaceholderPage
      eyebrow="SERVICES"
      heading={<>Verified help, <em>right down the street.</em></>}
      description="Browse the main categories, or just ask Samavya below what you need — the assistant knows the full catalogue and can point you to the right place."
    >
      <motion.ul
        className="dserv-tiles"
        initial="hidden"
        whileInView="shown"
        viewport={{ once: true, amount: 0.2 }}
        variants={{
          hidden: {},
          shown: { transition: { staggerChildren: 0.06 } },
        }}
      >
        {CATEGORIES.map((c) => (
          <motion.li
            key={c.label}
            variants={{
              hidden: { opacity: 0, y: 10 },
              shown: { opacity: 1, y: 0, transition: { duration: 0.6, ease: easeOut } },
            }}
          >
            <Link href="/dashboard/conversations" className="dserv-tile">
              <span className="dserv-tile-label">{c.label}</span>
              <span className="dserv-tile-desc">{c.desc}</span>
            </Link>
          </motion.li>
        ))}
      </motion.ul>

      <ConversationalAssistant
        title="Ask about services"
        placeholder="Ask about any service — e.g. 'do you do tailoring?'"
        prompts={PROMPTS}
        replyFn={servicesReply}
      />
    </PlaceholderPage>
  );
}
