"use client";

import { useEffect, useState } from "react";
import Link from "next/link";


export default function SiteHeader() {
  const [tone, setTone] = useState<"dark" | "light">("dark");

  useEffect(() => {
    const sections = Array.from(
      document.querySelectorAll<HTMLElement>("[data-nav-tone]"),
    );
    if (sections.length === 0) return;

    const headerHeight = 72;

    const compute = () => {
      // pick the section whose top is above the sampling line
      // and whose bottom is below it — the one currently under the header.
      const sampleY = headerHeight + 24;
      let active: HTMLElement | null = null;
      for (const s of sections) {
        const rect = s.getBoundingClientRect();
        if (rect.top <= sampleY && rect.bottom > sampleY) {
          active = s;
          break;
        }
      }
      if (!active) return;
      const next = (active.dataset.navTone as "dark" | "light") || "dark";
      setTone((prev) => (prev === next ? prev : next));
    };

    compute();
    window.addEventListener("scroll", compute, { passive: true });
    window.addEventListener("resize", compute);
    return () => {
      window.removeEventListener("scroll", compute);
      window.removeEventListener("resize", compute);
    };
  }, []);

  const className =
    tone === "light" ? "site-header site-header-tone-light" : "site-header";

  return (
    <header className={className}>
      <div className="site-header-band" aria-hidden="true" />
      <div className="site-header-inner">
        <nav className="nav-primary" aria-label="Primary">
          <a className="nav-link" href="#services">Services</a>
          <a className="nav-link" href="#marketplace">Marketplace</a>
          <a className="nav-link" href="#communities">Communities</a>
          <a className="nav-link" href="#about">About</a>
        </nav>

        <Link className="brand" href="/" aria-label="Samavya home">
          <svg
            className="brand-mark"
            viewBox="0 0 40 44"
            fill="none"
            aria-hidden="true"
          >
            <path d="M20 40V19C20 9 12 3 3 3C3 16 8 25 20 28" />
            <path d="M20 40V19C20 9 28 3 37 3C37 16 32 25 20 28" />
            <path d="M20 40V25" />
          </svg>
          <span>Samavya</span>
        </Link>

        <div className="nav-actions">
          <Link className="login-link" href="/login">Log in</Link>
          <span className="nav-divider" />
          <Link className="login-link login-link-accent" href="/signup">Sign up</Link>
        </div>
      </div>
    </header>
  );
}
