"use client";

import Link from "next/link";
import Image from "next/image";
import { motion } from "motion/react";
import { upcomingBookings, type Booking } from "@/lib/demo-data";

const easeOut = [0.16, 1, 0.3, 1] as const;

const STATUS_CLASS: Record<Booking["status"], string> = {
  Requested:     "dpill dpill-pending",
  Accepted:      "dpill dpill-active",
  "In progress": "dpill dpill-active",
  Completed:     "dpill dpill-done",
};

export default function BookingSummary() {
  return (
    <motion.section
      className="dpanel"
      aria-labelledby="dbook-h"
      initial={{ opacity: 0, y: 16 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, amount: 0.2 }}
      transition={{ duration: 0.8, ease: easeOut }}
    >
      <div className="dpanel-head">
        <h2 id="dbook-h" className="dpanel-title">
          Upcoming bookings
        </h2>
        <Link href="/dashboard/bookings" className="dpanel-link">
          View all
        </Link>
      </div>

      {upcomingBookings.length === 0 ? (
        <div className="dempty">
          <p className="dempty-title">No bookings yet.</p>
          <p className="dempty-text">
            When you confirm a service request, it will appear here.
          </p>
        </div>
      ) : (
        <ul className="dpanel-list">
          {upcomingBookings.map((b) => (
            <li key={b.id} className="dpanel-item">
              <span className="dpanel-thumb">
                <Image
                  src={b.image}
                  alt=""
                  fill
                  sizes="56px"
                  className="dpanel-thumb-img"
                />
              </span>
              <div className="dpanel-body">
                <p className="dpanel-item-title">{b.title}</p>
                <p className="dpanel-item-sub">{b.scheduled}</p>
              </div>
              <span className={STATUS_CLASS[b.status]}>{b.status}</span>
            </li>
          ))}
        </ul>
      )}
    </motion.section>
  );
}
