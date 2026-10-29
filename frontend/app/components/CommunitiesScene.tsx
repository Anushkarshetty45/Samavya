"use client";

import { useRef } from "react";
import { motion, useScroll, useTransform } from "motion/react";

const easeOut = [0.16, 1, 0.3, 1] as const;

const ITEMS = [
  {
    num: "01",
    title: "Connect with neighbours",
    text: "Meet people in your neighbourhood and stay updated on what's happening around you.",
  },
  {
    num: "02",
    title: "Share needs and get help",
    text: "Post your everyday needs; ask for recommendations and find reliable services within your community.",
  },
  {
    num: "03",
    title: "Join local events",
    text: "Discover and participate in neighbourhood meetups, clean-up drives, workshops and cultural events.",
  },
  {
    num: "04",
    title: "Collaborate on initiatives",
    text: "Organise group purchases, start local initiatives, and work together on ideas that improve your neighbourhood.",
  },
];

export default function CommunitiesScene() {
  const ref = useRef<HTMLElement>(null);

  const { scrollYProgress } = useScroll({
    target: ref,
    offset: ["start end", "end start"],
  });

  const bgY = useTransform(scrollYProgress, [0, 1], ["-2%", "2%"]);
  const bgScale = useTransform(scrollYProgress, [0, 0.5, 1], [1.02, 1.04, 1.02]);

  return (
    <section
      id="communities"
      ref={ref}
      className="comm-section"
      aria-label="Communities"
      data-nav-tone="light"
    >
      <div className="comm-bg-wrap" aria-hidden="true">
        <motion.div className="comm-bg" style={{ y: bgY, scale: bgScale }} />
      </div>

      <div className="comm-stage">
        {/* LEFT */}
        <div className="comm-content">
          <motion.div
            className="comm-eyebrow-row"
            initial={{ opacity: 0, y: 16 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: false, amount: 0.3 }}
            transition={{ duration: 0.8, ease: easeOut }}
          >
            <span className="comm-eyebrow">COMMUNITIES</span>
            <motion.span
              className="comm-eyebrow-rule"
              initial={{ scaleX: 0 }}
              whileInView={{ scaleX: 1 }}
              viewport={{ once: false, amount: 0.3 }}
              transition={{ duration: 1.1, ease: easeOut, delay: 0.2 }}
              aria-hidden="true"
            />
          </motion.div>

          <h2 className="comm-heading" aria-label="People near you, a stronger neighbourhood.">
            <motion.span
              className="comm-heading-line"
              initial={{ opacity: 0, y: 28 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: false, amount: 0.3 }}
              transition={{ duration: 0.9, ease: easeOut, delay: 0.1 }}
            >
              People near you,
            </motion.span>
            <motion.span
              className="comm-heading-line"
              initial={{ opacity: 0, y: 28 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: false, amount: 0.3 }}
              transition={{ duration: 0.9, ease: easeOut, delay: 0.26 }}
            >
              a <em>stronger</em>
            </motion.span>
            <motion.span
              className="comm-heading-line"
              initial={{ opacity: 0, y: 28 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: false, amount: 0.3 }}
              transition={{ duration: 0.9, ease: easeOut, delay: 0.42 }}
            >
              neighbourhood.
            </motion.span>
          </h2>

          <motion.p
            className="comm-description"
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: false, amount: 0.3 }}
            transition={{ duration: 0.9, ease: easeOut, delay: 0.6 }}
          >
            Communities on Samavya help you connect with the people around you,
            share everyday needs, organise local events and work together on
            initiatives that make your neighbourhood a better place to live.
          </motion.p>
        </div>

        {/* RIGHT */}
        <motion.div
          className="comm-arch"
          initial={{ opacity: 0, scale: 0.92, y: 30 }}
          whileInView={{ opacity: 1, scale: 1, y: 0 }}
          viewport={{ once: false, amount: 0.2 }}
          transition={{ duration: 1.1, ease: easeOut, delay: 0.2 }}
        >
          {/* vertical hairline threading through all numerals — draws down */}
          <motion.span
            className="comm-items-thread"
            aria-hidden="true"
            initial={{ scaleY: 0 }}
            whileInView={{ scaleY: 1 }}
            viewport={{ once: false, amount: 0.2 }}
            transition={{ duration: 1.6, ease: easeOut, delay: 0.6 }}
          />

          <motion.ol
            className="comm-items"
            initial="hidden"
            whileInView="shown"
            viewport={{ once: false, amount: 0.2 }}
            variants={{
              hidden: {},
              shown: { transition: { staggerChildren: 0.16, delayChildren: 0.55 } },
            }}
          >
            {ITEMS.map((item, i) => (
              <motion.li
                key={item.num}
                className="comm-item"
                variants={{
                  hidden: { opacity: 0, y: 24 },
                  shown: {
                    opacity: 1,
                    y: 0,
                    transition: { duration: 0.8, ease: easeOut },
                  },
                }}
              >
                {/* numeral — scale-in with a small pulse, dot grows in beside it */}
                <motion.span
                  className="comm-item-num-wrap"
                  variants={{
                    hidden: { scale: 0.6, opacity: 0 },
                    shown: {
                      scale: 1,
                      opacity: 1,
                      transition: { duration: 0.7, ease: easeOut },
                    },
                  }}
                >
                  <motion.span
                    className="comm-item-dot"
                    aria-hidden="true"
                    variants={{
                      hidden: { scale: 0 },
                      shown: {
                        scale: 1,
                        transition: { duration: 0.5, ease: easeOut, delay: 0.25 },
                      },
                    }}
                  />
                  <span className="comm-item-num">{item.num}</span>
                </motion.span>

                {/* body — title with aureolin underline sweep, then text */}
                <div className="comm-item-body">
                  <motion.h3
                    className="comm-item-title"
                    variants={{
                      hidden: { opacity: 0, x: 12 },
                      shown: {
                        opacity: 1,
                        x: 0,
                        transition: { duration: 0.7, ease: easeOut, delay: 0.1 },
                      },
                    }}
                  >
                    {item.title}
                    <motion.span
                      className="comm-item-underline"
                      aria-hidden="true"
                      variants={{
                        hidden: { scaleX: 0 },
                        shown: {
                          scaleX: 1,
                          transition: { duration: 0.9, ease: easeOut, delay: 0.5 },
                        },
                      }}
                    />
                  </motion.h3>
                  <motion.p
                    className="comm-item-text"
                    variants={{
                      hidden: { opacity: 0, y: 8 },
                      shown: {
                        opacity: 0.72,
                        y: 0,
                        transition: { duration: 0.7, ease: easeOut, delay: 0.3 },
                      },
                    }}
                  >
                    {item.text}
                  </motion.p>
                </div>

                {i < ITEMS.length - 1 && (
                  <motion.span
                    className="comm-item-rule"
                    aria-hidden="true"
                    variants={{
                      hidden: { scaleX: 0 },
                      shown: {
                        scaleX: 1,
                        transition: { duration: 1.0, ease: easeOut, delay: 0.5 },
                      },
                    }}
                  />
                )}
              </motion.li>
            ))}
          </motion.ol>
        </motion.div>
      </div>
    </section>
  );
}
