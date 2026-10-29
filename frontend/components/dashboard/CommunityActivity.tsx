"use client";

import Link from "next/link";
import Image from "next/image";
import { motion } from "motion/react";
import { communityActivity } from "@/lib/demo-data";

const easeOut = [0.16, 1, 0.3, 1] as const;

export default function CommunityActivity() {
  return (
    <motion.section
      className="dpanel"
      aria-labelledby="dact-h"
      initial={{ opacity: 0, y: 16 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, amount: 0.2 }}
      transition={{ duration: 0.8, ease: easeOut }}
    >
      <div className="dpanel-head">
        <h2 id="dact-h" className="dpanel-title">
          Community activity
        </h2>
        <Link href="/dashboard/communities" className="dpanel-link">
          View all
        </Link>
      </div>

      <ul className="dpanel-list">
        {communityActivity.map((item) => (
          <li key={item.id} className="dpanel-item">
            <span className="dpanel-thumb">
              <Image
                src={item.image}
                alt=""
                fill
                sizes="56px"
                className="dpanel-thumb-img"
              />
            </span>
            <div className="dpanel-body">
              <p className="dpanel-item-title">{item.description}</p>
              <p className="dpanel-item-sub">
                <span className="dpanel-cat">{item.category}</span>
                <span className="dpanel-sep" aria-hidden="true">·</span>
                <span>{item.timeAgo}</span>
              </p>
            </div>
          </li>
        ))}
      </ul>
    </motion.section>
  );
}
