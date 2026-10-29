"use client";

import Image from "next/image";
import { useSyncExternalStore } from "react";
import { motion } from "motion/react";
import { demoUser, greetingFromHour } from "@/lib/demo-data";

// useSyncExternalStore subscription that never changes — the only purpose
// is to return a stable server snapshot ("Good day") during prerender and
// the real time-of-day on the client. This satisfies both:
//   • Next 16's prerender guard (no `new Date()` in the client render body)
//   • React's lint rule (no setState inside useEffect)
const subscribe = () => () => {};
const getClientSnapshot = () => greetingFromHour(new Date().getHours());
const getServerSnapshot = () => "Good day";

const easeOut = [0.16, 1, 0.3, 1] as const;

/**
 * Dashboard greeting hero.
 * Left: eyebrow + two-line serif heading + lead copy.
 * Right: a wide decorative photo that bleeds to the right edge of the page
 * with a short handwritten script positioned inside the arch area.
 */
export default function DashboardGreeting() {
  const greeting = useSyncExternalStore(subscribe, getClientSnapshot, getServerSnapshot);

  return (
    <section className="dgreet" aria-labelledby="dgreet-h">
      <div className="dgreet-copy">
        <motion.span
          className="dgreet-eyebrow"
          initial={{ opacity: 0, y: 6 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, ease: easeOut }}
        >
          {demoUser.neighbourhood.toUpperCase()} COMMUNITY
          <span className="dgreet-eyebrow-rule" aria-hidden="true" />
        </motion.span>

        <motion.h1
          id="dgreet-h"
          className="dgreet-greet"
          initial={{ opacity: 0, y: 14 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, ease: easeOut, delay: 0.06 }}
        >
          <span className="dgreet-line">{greeting},</span>
          <span className="dgreet-line dgreet-name">{demoUser.firstName}.</span>
        </motion.h1>

        <motion.p
          className="dgreet-lead"
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, ease: easeOut, delay: 0.16 }}
        >
          Your community, your connections, all in one place.
        </motion.p>
      </div>

      <motion.div
        className="dgreet-photo"
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ duration: 0.9, ease: easeOut, delay: 0.1 }}
        aria-hidden="true"
      >
        <Image
          src="/images/dashboard/crops/hero-right-arch.jpg"
          alt=""
          fill
          priority
          sizes="(max-width: 1024px) 100vw, 55vw"
          className="dgreet-photo-img"
        />
        <div className="dgreet-script" aria-hidden="true">
          <span>Stronger</span>
          <span>neighbourhoods</span>
          <em>Brighter days</em>
          <svg
            className="dgreet-script-rule"
            viewBox="0 0 120 14"
            fill="none"
          >
            <path
              d="M2 8 Q 40 14, 80 6 T 118 4"
              stroke="currentColor"
              strokeWidth="1.4"
              strokeLinecap="round"
              fill="none"
            />
          </svg>
        </div>
      </motion.div>
    </section>
  );
}
