"use client";

import { motion } from "motion/react";


 

const easeOut = [0.16, 1, 0.3, 1] as const;

export default function ServicesAside() {
  return (
    <aside className="services-aside" aria-label="Services introduction">
      <motion.div
        className="services-eyebrow-row"
        initial={{ opacity: 0, y: 18 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: false, amount: 0.5 }}
        transition={{ duration: 0.8, ease: easeOut }}
      >
        <motion.span
          className="services-eyebrow-rule"
          initial={{ scaleX: 0 }}
          whileInView={{ scaleX: 1 }}
          viewport={{ once: false, amount: 0.5 }}
          transition={{ duration: 0.9, ease: easeOut, delay: 0.2 }}
          aria-hidden="true"
        />
        <span className="section-eyebrow">BUILT AROUND YOUR NEIGHBOURHOOD</span>
      </motion.div>

      <h2 className="services-heading" aria-label="Services at your doorstep.">
        <motion.span
          className="services-heading-line"
          initial={{ opacity: 0, y: 28 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: false, amount: 0.4 }}
          transition={{ duration: 0.9, ease: easeOut, delay: 0.1 }}
        >
          Services at your
        </motion.span>
        <motion.span
          className="services-heading-line"
          initial={{ opacity: 0, y: 28 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: false, amount: 0.4 }}
          transition={{ duration: 0.9, ease: easeOut, delay: 0.28 }}
        >
          <em>doorstep.</em>
        </motion.span>
      </h2>

      <motion.p
        className="services-text"
        initial={{ opacity: 0, y: 20 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: false, amount: 0.4 }}
        transition={{ duration: 1, ease: easeOut, delay: 0.5 }}
      >
        Sixty-five verified categories, from everyday fixes to specialist
        help. These six are the ones your street asks for most.
      </motion.p>
    </aside>
  );
}
