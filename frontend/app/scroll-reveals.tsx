"use client";

import { useEffect } from "react";
import { gsap } from "gsap";
import { ScrollTrigger } from "gsap/ScrollTrigger";

gsap.registerPlugin(ScrollTrigger);

export default function ScrollReveals() {
  useEffect(() => {
    const ctx = gsap.context(() => {
      gsap.utils.toArray<HTMLElement>(".reveal-up").forEach((el) => {
        gsap.fromTo(
          el,
          { opacity: 0, y: 40 },
          {
            opacity: 1,
            y: 0,
            duration: 1.1,
            ease: "power3.out",
            scrollTrigger: {
              trigger: el,
              start: "top 85%",
              toggleActions: "play none none reverse",
            },
          },
        );
      });

      gsap.utils.toArray<HTMLElement>(".reveal-stagger").forEach((parent) => {
        const items = parent.querySelectorAll<HTMLElement>(":scope > *");
        gsap.fromTo(
          items,
          { opacity: 0, y: 30 },
          {
            opacity: 1,
            y: 0,
            duration: 0.9,
            stagger: 0.12,
            ease: "power3.out",
            scrollTrigger: {
              trigger: parent,
              start: "top 80%",
              toggleActions: "play none none reverse",
            },
          },
        );
      });

      gsap.utils.toArray<HTMLElement>(".parallax-slow").forEach((el) => {
        gsap.to(el, {
          yPercent: -15,
          ease: "none",
          scrollTrigger: {
            trigger: el,
            start: "top bottom",
            end: "bottom top",
            scrub: true,
          },
        });
      });

      // Horizontal scroll-rule — a hairline draws itself across while in view.
      gsap.utils.toArray<HTMLElement>(".reveal-rule").forEach((el) => {
        gsap.fromTo(
          el,
          { scaleX: 0, transformOrigin: "left center" },
          {
            scaleX: 1,
            duration: 1.4,
            ease: "power3.out",
            scrollTrigger: {
              trigger: el,
              start: "top 90%",
              toggleActions: "play none none reverse",
            },
          },
        );
      });

      // Slow drift up on scroll — subtle lift while the element is in view.
      gsap.utils.toArray<HTMLElement>(".drift-up").forEach((el) => {
        gsap.fromTo(
          el,
          { y: 60 },
          {
            y: -20,
            ease: "none",
            scrollTrigger: {
              trigger: el,
              start: "top bottom",
              end: "bottom top",
              scrub: 1.2,
            },
          },
        );
      });

      // Letter-reveal — splits a heading into chars that fade in on scroll.
      gsap.utils.toArray<HTMLElement>(".reveal-chars").forEach((el) => {
        if (el.dataset.split === "done") return;
        const text = el.textContent ?? "";
        el.textContent = "";
        const frag = document.createDocumentFragment();
        for (const ch of text) {
          const span = document.createElement("span");
          span.className = "reveal-char";
          span.textContent = ch === " " ? " " : ch;
          frag.appendChild(span);
        }
        el.appendChild(frag);
        el.dataset.split = "done";

        gsap.fromTo(
          el.querySelectorAll(".reveal-char"),
          { opacity: 0, y: 30, rotateX: -45 },
          {
            opacity: 1,
            y: 0,
            rotateX: 0,
            duration: 0.8,
            stagger: 0.02,
            ease: "power3.out",
            scrollTrigger: {
              trigger: el,
              start: "top 85%",
              toggleActions: "play none none reverse",
            },
          },
        );
      });

      // Scroll-linked colour wash — accent color sweeps across a line of text.
      gsap.utils.toArray<HTMLElement>(".reveal-wash").forEach((el) => {
        gsap.fromTo(
          el,
          { backgroundSize: "0% 100%" },
          {
            backgroundSize: "100% 100%",
            ease: "power2.inOut",
            scrollTrigger: {
              trigger: el,
              start: "top 80%",
              end: "top 40%",
              scrub: 0.6,
            },
          },
        );
      });
    });

    return () => ctx.revert();
  }, []);

  return null;
}
