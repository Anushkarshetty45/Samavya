"use client";

import { useRef } from "react";
import Link from "next/link";
import { motion, useScroll, useTransform } from "motion/react";

const easeOut = [0.16, 1, 0.3, 1] as const;

const PROBLEMS = [
  {
    label: "Scattered services",
    text: "Finding reliable local service providers often requires searching across different platforms.",
  },
  {
    label: "Missed opportunities",
    text: "Neighbours may have similar needs but lack a simple way to coordinate.",
  },
  {
    label: "Fragmented interactions",
    text: "Requests, bookings, purchases, and complaints can become difficult to manage across different channels.",
  },
];

const APPROACH = [
  {
    num: "01",
    title: "Discover locally",
    text: "Find services, products, and rental opportunities relevant to your community.",
  },
  {
    num: "02",
    title: "Coordinate naturally",
    text: "Describe what you need in a conversation instead of navigating complicated workflows.",
  },
  {
    num: "03",
    title: "Bring people together",
    text: "Identify opportunities for shared purchases, coordinated requests, and community initiatives.",
  },
  {
    num: "04",
    title: "Stay informed",
    text: "Keep track of bookings, fulfilment, and complaints through connected workflows.",
  },
];

const DIFFERENT = [
  {
    label: "Local services",
    text: "Connect people with service providers for everyday needs.",
  },
  {
    label: "Community marketplace",
    text: "Bring together local products, rentals, and shared purchasing opportunities.",
  },
  {
    label: "Conversational coordination",
    text: "Use AI to understand requests, organise relevant information, and suggest suitable next steps.",
  },
];

export default function AboutScene() {
  const ref = useRef<HTMLElement>(null);

  const { scrollYProgress } = useScroll({
    target: ref,
    offset: ["start end", "end start"],
  });

  // very subtle vertical motion on the texture overlay
  const grainY = useTransform(scrollYProgress, [0, 1], ["-2%", "2%"]);

  return (
    <section
      id="about"
      ref={ref}
      className="about-scene"
      aria-label="About Samavya"
      data-nav-tone="light"
    >
      <div className="about-bg" aria-hidden="true">
        <motion.div className="about-bg-grain" style={{ y: grainY }} />
      </div>

      <div className="about-stage">
        {/* ======================= 1 · OUR STORY ======================= */}
        <section className="about-section-block about-story">
          <motion.div
            className="about-section-head"
            initial="hidden"
            whileInView="shown"
            viewport={{ once: false, amount: 0.3 }}
            variants={{
              hidden: {},
              shown: { transition: { staggerChildren: 0.14 } },
            }}
          >
            <motion.span
              className="about-section-no"
              variants={{
                hidden: { opacity: 0, y: 10 },
                shown: { opacity: 1, y: 0, transition: { duration: 0.7, ease: easeOut } },
              }}
            >
              01 · OUR STORY
            </motion.span>
            <motion.h2
              className="about-h"
              variants={{
                hidden: { opacity: 0, y: 24 },
                shown: { opacity: 1, y: 0, transition: { duration: 0.9, ease: easeOut } },
              }}
            >
              A better way to <em>bring communities</em> together.
            </motion.h2>
          </motion.div>

          <motion.div
            className="about-prose"
            initial="hidden"
            whileInView="shown"
            viewport={{ once: false, amount: 0.2 }}
            variants={{
              hidden: {},
              shown: { transition: { staggerChildren: 0.12, delayChildren: 0.2 } },
            }}
          >
            <motion.p
              variants={{
                hidden: { opacity: 0, y: 20 },
                shown: { opacity: 1, y: 0, transition: { duration: 0.8, ease: easeOut } },
              }}
            >
              Samavya began with a simple idea: everyday needs are easier to
              solve when communities come together.
            </motion.p>
            <motion.p
              variants={{
                hidden: { opacity: 0, y: 20 },
                shown: { opacity: 1, y: 0, transition: { duration: 0.8, ease: easeOut } },
              }}
            >
              From finding a reliable local service to sharing purchases and
              accessing rental items, people often have to search across
              multiple platforms to get things done.
            </motion.p>
            <motion.p
              variants={{
                hidden: { opacity: 0, y: 20 },
                shown: { opacity: 1, y: 0, transition: { duration: 0.8, ease: easeOut } },
              }}
            >
              Samavya brings these everyday interactions into one connected
              space, helping people discover local opportunities, coordinate
              with their neighbours, and get things done through a single
              conversational assistant.
            </motion.p>
          </motion.div>
        </section>

        {/* ======================= 2 · THE PROBLEM ======================= */}
        <section className="about-section-block about-problem">
          <motion.div
            className="about-section-head"
            initial="hidden"
            whileInView="shown"
            viewport={{ once: false, amount: 0.3 }}
            variants={{
              hidden: {},
              shown: { transition: { staggerChildren: 0.14 } },
            }}
          >
            <motion.span
              className="about-section-no"
              variants={{
                hidden: { opacity: 0, y: 10 },
                shown: { opacity: 1, y: 0, transition: { duration: 0.7, ease: easeOut } },
              }}
            >
              02 · THE PROBLEM
            </motion.span>
            <motion.h2
              className="about-h"
              variants={{
                hidden: { opacity: 0, y: 24 },
                shown: { opacity: 1, y: 0, transition: { duration: 0.9, ease: easeOut } },
              }}
            >
              Everyday needs <em>shouldn&apos;t</em> be complicated.
            </motion.h2>
          </motion.div>

          <motion.div
            className="about-cols3"
            initial="hidden"
            whileInView="shown"
            viewport={{ once: false, amount: 0.2 }}
            variants={{
              hidden: {},
              shown: { transition: { staggerChildren: 0.14, delayChildren: 0.2 } },
            }}
          >
            {PROBLEMS.map((p) => (
              <motion.div
                key={p.label}
                className="about-col"
                variants={{
                  hidden: { opacity: 0, y: 20 },
                  shown: { opacity: 1, y: 0, transition: { duration: 0.8, ease: easeOut } },
                }}
              >
                <h3 className="about-col-label">{p.label}</h3>
                <p className="about-col-text">{p.text}</p>
              </motion.div>
            ))}
          </motion.div>
        </section>

        {/* ======================= 3 · OUR APPROACH ======================= */}
        <section className="about-section-block about-approach">
          <motion.div
            className="about-section-head"
            initial="hidden"
            whileInView="shown"
            viewport={{ once: false, amount: 0.3 }}
            variants={{
              hidden: {},
              shown: { transition: { staggerChildren: 0.14 } },
            }}
          >
            <motion.span
              className="about-section-no"
              variants={{
                hidden: { opacity: 0, y: 10 },
                shown: { opacity: 1, y: 0, transition: { duration: 0.7, ease: easeOut } },
              }}
            >
              03 · OUR APPROACH
            </motion.span>
            <motion.h2
              className="about-h"
              variants={{
                hidden: { opacity: 0, y: 24 },
                shown: { opacity: 1, y: 0, transition: { duration: 0.9, ease: easeOut } },
              }}
            >
              One place. <em>Many possibilities.</em>
            </motion.h2>
          </motion.div>

          <motion.ol
            className="about-approach-list"
            initial="hidden"
            whileInView="shown"
            viewport={{ once: false, amount: 0.1 }}
            variants={{
              hidden: {},
              shown: { transition: { staggerChildren: 0.12, delayChildren: 0.2 } },
            }}
          >
            {APPROACH.map((a) => (
              <motion.li
                key={a.num}
                className="about-approach-item"
                variants={{
                  hidden: { opacity: 0, y: 24 },
                  shown: { opacity: 1, y: 0, transition: { duration: 0.9, ease: easeOut } },
                }}
              >
                <span className="about-approach-num">{a.num}</span>
                <div className="about-approach-body">
                  <h3 className="about-approach-title">{a.title}</h3>
                  <p className="about-approach-text">{a.text}</p>
                </div>
              </motion.li>
            ))}
          </motion.ol>
        </section>

        {/* ======================= 4 · WHAT MAKES US DIFFERENT ======================= */}
        <section className="about-section-block about-different">
          <motion.div
            className="about-section-head"
            initial="hidden"
            whileInView="shown"
            viewport={{ once: false, amount: 0.3 }}
            variants={{
              hidden: {},
              shown: { transition: { staggerChildren: 0.14 } },
            }}
          >
            <motion.span
              className="about-section-no"
              variants={{
                hidden: { opacity: 0, y: 10 },
                shown: { opacity: 1, y: 0, transition: { duration: 0.7, ease: easeOut } },
              }}
            >
              04 · WHAT MAKES US DIFFERENT
            </motion.span>
            <motion.h2
              className="about-h"
              variants={{
                hidden: { opacity: 0, y: 24 },
                shown: { opacity: 1, y: 0, transition: { duration: 0.9, ease: easeOut } },
              }}
            >
              More than a <em>marketplace.</em>
            </motion.h2>
          </motion.div>

          <motion.p
            className="about-lede"
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: false, amount: 0.3 }}
            transition={{ duration: 0.9, ease: easeOut, delay: 0.3 }}
          >
            Samavya combines three connected ideas. The important distinction
            is that Samavya doesn&apos;t just help people find something — it
            also helps them coordinate what happens next.
          </motion.p>

          <motion.div
            className="about-triad"
            initial="hidden"
            whileInView="shown"
            viewport={{ once: false, amount: 0.2 }}
            variants={{
              hidden: {},
              shown: { transition: { staggerChildren: 0.18, delayChildren: 0.4 } },
            }}
          >
            {DIFFERENT.map((d, i) => (
              <motion.div
                key={d.label}
                className="about-triad-item"
                variants={{
                  hidden: { opacity: 0, y: 24 },
                  shown: { opacity: 1, y: 0, transition: { duration: 0.9, ease: easeOut } },
                }}
              >
                <span className="about-triad-label">{d.label}</span>
                <p className="about-triad-text">{d.text}</p>
                {i < DIFFERENT.length - 1 && (
                  <span className="about-triad-arrow" aria-hidden="true">
                    →
                  </span>
                )}
              </motion.div>
            ))}
          </motion.div>
        </section>

        {/* ======================= 5 · OUR VISION (closing, contrast panel) ======================= */}
        <motion.section
          className="about-section-block about-vision"
          initial={{ opacity: 0, y: 40 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: false, amount: 0.2 }}
          transition={{ duration: 1.0, ease: easeOut }}
        >
          <motion.span
            className="about-section-no"
            initial={{ opacity: 0, y: 10 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: false, amount: 0.3 }}
            transition={{ duration: 0.7, ease: easeOut, delay: 0.2 }}
          >
            05 · OUR VISION
          </motion.span>

          <motion.h2
            className="about-vision-h"
            initial={{ opacity: 0, y: 32 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: false, amount: 0.3 }}
            transition={{ duration: 1.0, ease: easeOut, delay: 0.3 }}
          >
            Stronger communities start with
            <em> everyday connections.</em>
          </motion.h2>

          <motion.p
            className="about-vision-text"
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: false, amount: 0.3 }}
            transition={{ duration: 0.9, ease: easeOut, delay: 0.5 }}
          >
            By bringing everyday needs and community coordination into one
            connected platform, Samavya aims to make local interactions
            simpler and communities more connected.
          </motion.p>

          <motion.div
            className="about-sign"
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            viewport={{ once: false, amount: 0.3 }}
            transition={{ duration: 0.9, ease: easeOut, delay: 0.7 }}
          >
            <motion.span
              className="about-sign-rule"
              initial={{ scaleX: 0 }}
              whileInView={{ scaleX: 1 }}
              viewport={{ once: false, amount: 0.3 }}
              transition={{ duration: 1.2, ease: easeOut, delay: 0.8 }}
              aria-hidden="true"
            />
            <span className="about-sign-text">SAMAVYA · 2026</span>
          </motion.div>

          <motion.nav
            className="about-legal"
            aria-label="Legal"
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            viewport={{ once: false, amount: 0.3 }}
            transition={{ duration: 0.9, ease: easeOut, delay: 0.9 }}
          >
            <Link href="/privacy" className="about-legal-link">
              Privacy policy
            </Link>
            <span className="about-legal-sep" aria-hidden="true">·</span>
            <Link href="/terms" className="about-legal-link">
              Terms &amp; conditions
            </Link>
          </motion.nav>
        </motion.section>
      </div>
    </section>
  );
}
