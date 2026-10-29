"use client";

import Link from "next/link";
import Image from "next/image";
import { motion } from "motion/react";
import { nearbyItems } from "@/lib/demo-data";

const easeOut = [0.16, 1, 0.3, 1] as const;

export default function NearbyItems() {
  return (
    <motion.section
      className="dpanel"
      aria-labelledby="dnear-h"
      initial={{ opacity: 0, y: 16 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, amount: 0.2 }}
      transition={{ duration: 0.8, ease: easeOut }}
    >
      <div className="dpanel-head">
        <h2 id="dnear-h" className="dpanel-title">
          For you nearby
        </h2>
        <Link href="/dashboard/marketplace" className="dpanel-link">
          View all
        </Link>
      </div>

      <ul className="dpanel-list">
        {nearbyItems.map((n) => (
          <li key={n.id} className="dpanel-item">
            <span className="dpanel-thumb">
              <Image
                src={n.image}
                alt=""
                fill
                sizes="56px"
                className="dpanel-thumb-img"
              />
            </span>
            <div className="dpanel-body">
              <p className="dpanel-item-title">{n.title}</p>
              <p className="dpanel-item-sub">
                <span className="dpanel-cat">Marketplace</span>
              </p>
            </div>
            <span className="dpill dpill-price">{n.price}</span>
          </li>
        ))}
      </ul>
    </motion.section>
  );
}
