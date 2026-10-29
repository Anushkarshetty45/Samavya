"use client";

import { motion } from "motion/react";
import type { ReactNode } from "react";

const easeOut = [0.16, 1, 0.3, 1] as const;

type Props = {
  eyebrow: string;
  heading: ReactNode;
  description: string;
  children?: ReactNode;
};

export default function PlaceholderPage({
  eyebrow,
  heading,
  description,
  children,
}: Props) {
  return (
    <div className="dash-page">
      <motion.span
        className="dash-section-eyebrow"
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.7, ease: easeOut }}
      >
        {eyebrow}
      </motion.span>
      <motion.h1
        className="dash-page-h"
        initial={{ opacity: 0, y: 18 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.8, ease: easeOut, delay: 0.08 }}
      >
        {heading}
      </motion.h1>
      <motion.p
        className="dash-page-p"
        initial={{ opacity: 0, y: 14 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.8, ease: easeOut, delay: 0.18 }}
      >
        {description}
      </motion.p>
      {children}
    </div>
  );
}
