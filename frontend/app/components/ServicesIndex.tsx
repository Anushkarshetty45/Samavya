"use client";

import Image from "next/image";
import { useRef } from "react";
import { motion, useScroll, useTransform } from "motion/react";



const easeOut = [0.16, 1, 0.3, 1] as const;

const SERVICES = [
  { num: "01", name: "Plumber",         blurb: "Pipes, taps, leaks and plumbing",      image: "/images/plumber.jpeg" },
  { num: "02", name: "Electrician",     blurb: "Electrical repairs and installation",  image: "/images/electrician.jpg" },
  { num: "03", name: "House Cleaner",   blurb: "Home cleaning",                        image: "/images/house-cleaner.webp" },
  { num: "04", name: "Cook",            blurb: "Home cooking services",                image: "/images/cook.jpg" },
  { num: "05", name: "Tutor",           blurb: "Academic tutoring",                    image: "/images/tutor.jpeg" },
  { num: "06", name: "Vehicle Mechanic", blurb: "Vehicle servicing and repairs",       image: "/images/vehicle-mechanic.png" },
];

function ServiceCard({ s, isFirst }: { s: (typeof SERVICES)[number]; isFirst: boolean }) {
  const cardRef = useRef<HTMLElement>(null);

  // scroll-linked image parallax inside the card
  const { scrollYProgress } = useScroll({
    target: cardRef,
    offset: ["start end", "end start"],
  });
  const imgY = useTransform(scrollYProgress, [0, 1], ["-8%", "8%"]);
  const imgScale = useTransform(scrollYProgress, [0, 0.5, 1], [1.1, 1.02, 1.08]);

  return (
    <motion.article
      ref={cardRef}
      className="svc-block"
      initial="hidden"
      whileInView="shown"
      viewport={{ once: false, amount: 0.2 }}
      variants={{
        hidden: {},
        shown: { transition: { staggerChildren: 0.12, delayChildren: 0.05 } },
      }}
    >
      <div className="svc-block-image">
        <motion.div
          className="svc-block-image-inner"
          style={{ y: imgY, scale: imgScale }}
        >
          <Image
            src={s.image}
            alt={s.name}
            fill
            sizes="(max-width: 1100px) 100vw, 46vw"
            className="svc-block-img"
            priority={isFirst}
          />
        </motion.div>
      </div>

      <motion.div
        className="svc-block-caption"
        variants={{
          hidden: {},
          shown: { transition: { staggerChildren: 0.08 } },
        }}
      >
        <motion.h3
          className="svc-block-title"
          variants={{
            hidden: { opacity: 0, y: 18 },
            shown: { opacity: 1, y: 0, transition: { duration: 0.75, ease: easeOut } },
          }}
        >
          {s.name}
        </motion.h3>

        <motion.span
          className="svc-block-rule"
          variants={{
            hidden: { scaleX: 0 },
            shown: { scaleX: 1, transition: { duration: 0.9, ease: easeOut } },
          }}
          aria-hidden="true"
        />

        <motion.div
          className="svc-block-meta"
          variants={{
            hidden: { opacity: 0, y: 14 },
            shown: { opacity: 1, y: 0, transition: { duration: 0.8, ease: easeOut } },
          }}
        >
          <span className="svc-block-num">{s.num}</span>
          <span className="svc-block-divider" aria-hidden="true" />
          <p className="svc-block-blurb">{s.blurb}</p>
        </motion.div>
      </motion.div>
    </motion.article>
  );
}

export default function ServicesIndex() {
  return (
    <div className="svc-stream">
      {SERVICES.map((s, i) => (
        <ServiceCard key={s.num} s={s} isFirst={i === 0} />
      ))}
    </div>
  );
}
