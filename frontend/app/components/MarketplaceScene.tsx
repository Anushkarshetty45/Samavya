"use client";

import { useRef } from "react";
import { motion, useScroll, useTransform } from "motion/react";

const easeOut = [0.16, 1, 0.3, 1] as const;


const DESCRIPTION =
  "Browse, buy, rent, or join group purchases from people and producers in your neighbourhood, all through one conversation.";

const NOTE =
  "A shelf of tools you can borrow from the house two doors down. A box of mangoes split six ways. Groceries ordered together, delivered once.";

export default function MarketplaceScene() {
  const ref = useRef<HTMLElement>(null);

  const { scrollYProgress } = useScroll({
    target: ref,
    offset: ["start end", "end start"],
  });

  // very subtle breathing — kept small so the arches never shift noticeably
  const bgY = useTransform(scrollYProgress, [0, 1], ["-0.6%", "0.6%"]);
  const bgScale = useTransform(scrollYProgress, [0, 0.5, 1], [1.0, 1.01, 1.0]);

  return (
    <section
      id="marketplace"
      ref={ref}
      className="mkt-section"
      aria-label="Marketplace"
      data-nav-tone="dark"
    >
      <div className="mkt-bg-wrap" aria-hidden="true">
        <motion.div className="mkt-bg" style={{ y: bgY, scale: bgScale }} />
      </div>

      <div className="mkt-stage">
        {/* LEFT — editorial column over the stucco half of the background */}
        <div className="mkt-content">
          <motion.div
            className="mkt-eyebrow-row"
            initial={{ opacity: 0, y: 16 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: false, amount: 0.3 }}
            transition={{ duration: 0.8, ease: easeOut }}
          >
            <span className="mkt-eyebrow">MARKETPLACE</span>
            <motion.span
              className="mkt-eyebrow-rule"
              initial={{ scaleX: 0 }}
              whileInView={{ scaleX: 1 }}
              viewport={{ once: false, amount: 0.3 }}
              transition={{ duration: 1.1, ease: easeOut, delay: 0.2 }}
              aria-hidden="true"
            />
          </motion.div>

          <h2 className="mkt-heading" aria-label="More value, shared locally.">
            <motion.span
              className="mkt-heading-line"
              initial={{ opacity: 0, y: 30 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: false, amount: 0.3 }}
              transition={{ duration: 1.0, ease: easeOut, delay: 0.1 }}
            >
              More value,
            </motion.span>
            <motion.span
              className="mkt-heading-line mkt-heading-emph"
              initial={{ opacity: 0, y: 30 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: false, amount: 0.3 }}
              transition={{ duration: 1.0, ease: easeOut, delay: 0.3 }}
            >
              <em>shared locally.</em>
            </motion.span>
          </h2>

          <motion.p
            className="mkt-description"
            initial={{ opacity: 0, y: 24 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: false, amount: 0.3 }}
            transition={{ duration: 1.0, ease: easeOut, delay: 0.55 }}
          >
            {DESCRIPTION}
          </motion.p>

          <div className="mkt-note-wrap">
            <motion.span
              className="mkt-note-rule"
              initial={{ scaleY: 0 }}
              whileInView={{ scaleY: 1 }}
              viewport={{ once: false, amount: 0.3 }}
              transition={{ duration: 1.0, ease: easeOut, delay: 0.9 }}
              aria-hidden="true"
            />
            <motion.p
              className="mkt-note"
              initial={{ opacity: 0, y: 16 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: false, amount: 0.3 }}
              transition={{ duration: 1.0, ease: easeOut, delay: 1.15 }}
            >
              {NOTE}
            </motion.p>
          </div>
        </div>

        {/* right spacer so editorial copy stays in the left half of the stage */}
        <div className="mkt-arches" aria-hidden="true" />
      </div>
    </section>
  );
}
