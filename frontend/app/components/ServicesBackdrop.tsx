"use client";

import { useRef } from "react";
import { motion, useScroll, useTransform } from "motion/react";


export default function ServicesBackdrop({
  targetRef,
}: {
  targetRef: React.RefObject<HTMLElement | null>;
}) {
  const localRef = useRef<HTMLDivElement>(null);

  const { scrollYProgress } = useScroll({
    target: targetRef,
    offset: ["start end", "end start"],
  });

  const sunY = useTransform(scrollYProgress, [0, 1], ["-8%", "-24%"]);
  const sunOpacity = useTransform(
    scrollYProgress,
    [0, 0.3, 0.7, 1],
    [0.3, 0.6, 0.6, 0.4],
  );

  return (
    <div ref={localRef} className="svc-backdrop" aria-hidden="true">
      {/* 1. base */}
      <div className="svc-base" />

      {/* 2. aureolin sun — top-right radial glow, slow breathe + scroll drift */}
      <motion.div
        className="svc-sun"
        style={{ y: sunY, opacity: sunOpacity }}
        animate={{ scale: [1, 1.08, 1] }}
        transition={{ duration: 18, repeat: Infinity, ease: "easeInOut" }}
      />

      {/* 3. bistre dust grain */}
      <div className="svc-grain" />

      {/* 4. top + bottom fades */}
      <div className="svc-fade-top" />
      <div className="svc-fade-bottom" />
    </div>
  );
}
