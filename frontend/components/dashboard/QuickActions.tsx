"use client";

import Link from "next/link";
import Image from "next/image";
import { motion } from "motion/react";
import { quickActions } from "@/lib/demo-data";

const easeOut = [0.16, 1, 0.3, 1] as const;

/**
 * Row of 4 photo tiles. Each tile = small square cropped photo on the left,
 * label + helper text on the right. Fits the editorial reference style —
 * no icons, no oversized cards.
 */
export default function QuickActions() {
  return (
    <motion.section
      className="dquick"
      aria-labelledby="dquick-h"
      initial="hidden"
      whileInView="shown"
      viewport={{ once: true, amount: 0.2 }}
      variants={{
        hidden: {},
        shown: { transition: { staggerChildren: 0.08 } },
      }}
    >
      <h2 id="dquick-h" className="dsection-title">
        Quick actions
      </h2>
      <ul className="dquick-list">
        {quickActions.map((a) => (
          <motion.li
            key={a.href}
            variants={{
              hidden: { opacity: 0, y: 8 },
              shown: { opacity: 1, y: 0, transition: { duration: 0.6, ease: easeOut } },
            }}
          >
            <Link href={a.href} className="dquick-link">
              <span className="dquick-img">
                <Image
                  src={a.image}
                  alt={a.alt}
                  fill
                  sizes="80px"
                  className="dquick-img-el"
                />
              </span>
              <span className="dquick-text">
                <span className="dquick-label">{a.label}</span>
                <span className="dquick-helper">{a.helper}</span>
              </span>
            </Link>
          </motion.li>
        ))}
      </ul>
    </motion.section>
  );
}
