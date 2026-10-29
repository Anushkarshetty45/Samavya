"use client";

import { useRef } from "react";
import { motion, useScroll, useTransform } from "motion/react";



const easeOut = [0.16, 1, 0.3, 1] as const;

export default function QuietMechanic() {
  const ref = useRef<HTMLElement>(null);

  const { scrollYProgress } = useScroll({
    target: ref,
    offset: ["start end", "end start"],
  });

  const imageY = useTransform(scrollYProgress, [0, 1], ["-6%", "6%"]);
  const imageScale = useTransform(scrollYProgress, [0, 1], [1.08, 1.0]);
  const overlayOpacity = useTransform(
    scrollYProgress,
    [0, 0.3, 0.7, 1],
    [0.55, 0.72, 0.72, 0.6],
  );

  return (
    <section
      id="interstitial"
      ref={ref}
      className="qm-section"
      aria-label="The quiet mechanic"
      data-nav-tone="dark"
    >
      {/* image + overlay */}
      <motion.div
        className="qm-image"
        style={{ y: imageY, scale: imageScale }}
        aria-hidden="true"
      />
      <motion.div
        className="qm-overlay"
        style={{ opacity: overlayOpacity }}
        aria-hidden="true"
      />

      <div className="qm-content">
        {/* main copy */}
        <div className="qm-main">
          <motion.span
            className="qm-eyebrow"
            initial={{ opacity: 0, y: 16 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: false, amount: 0.4 }}
            transition={{ duration: 0.7, ease: easeOut }}
          >
            THE QUIET MECHANIC
          </motion.span>

          <motion.h2
            className="qm-prose"
            initial={{ opacity: 0, y: 30 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: false, amount: 0.3 }}
            transition={{ duration: 1, ease: easeOut, delay: 0.15 }}
          >
            You say what you need.
            <br />
            Samavya listens, <em>and so does the street.</em>
          </motion.h2>

          <motion.p
            className="qm-sub"
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: false, amount: 0.3 }}
            transition={{ duration: 0.9, ease: easeOut, delay: 0.35 }}
          >
            A single thread holds the request, the matched neighbour, the
            fulfilment, and the small resolutions after. Nothing is scattered;
            nothing is forgotten.
          </motion.p>

          <motion.div
            className="qm-steps"
            initial="hidden"
            whileInView="shown"
            viewport={{ once: false, amount: 0.3 }}
            variants={{
              hidden: {},
              shown: { transition: { staggerChildren: 0.14, delayChildren: 0.5 } },
            }}
          >
            {[
              { mark: "i.", text: "You describe the need, in your own words." },
              { mark: "ii.", text: "Samavya finds the right neighbour, visit, or group." },
              { mark: "iii.", text: "Everyone confirms, meets, and closes the thread together." },
            ].map((s) => (
              <motion.div
                key={s.mark}
                className="qm-step"
                variants={{
                  hidden: { opacity: 0, y: 22 },
                  shown: { opacity: 1, y: 0, transition: { duration: 0.8, ease: easeOut } },
                }}
              >
                <span className="qm-step-mark">{s.mark}</span>
                <p>{s.text}</p>
              </motion.div>
            ))}
          </motion.div>
        </div>
      </div>
    </section>
  );
}
