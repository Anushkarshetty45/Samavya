"use client";

import { useEffect, useRef, useState } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { topNav, demoUser } from "@/lib/demo-data";
import ProfileDropdown from "./ProfileDropdown";

export default function DashboardNavigation() {
  const pathname = usePathname();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [notifOpen, setNotifOpen] = useState(false);
  const [searchQ, setSearchQ] = useState("");
  const notifRef = useRef<HTMLDivElement | null>(null);

  // Close popover on outside click
  useEffect(() => {
    function onDown(e: MouseEvent) {
      if (!notifRef.current) return;
      if (!notifRef.current.contains(e.target as Node)) setNotifOpen(false);
    }
    document.addEventListener("mousedown", onDown);
    return () => document.removeEventListener("mousedown", onDown);
  }, []);

  function onSearchSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    // frontend-only; no backend search wired. Navigate to the overview so the
    // user can continue the conversation from there.
    if (searchQ.trim().length > 0) {
      // keep it a form submit so Enter works; no router.push needed for demo
    }
  }

  return (
    <header className="dnav" role="banner">
      <div className="dnav-inner">
        {/* Left — Samavya wordmark links to the public homepage (/) */}
        <Link href="/" className="dnav-brand" aria-label="Samavya home">
          <svg viewBox="0 0 40 44" fill="none" aria-hidden="true" className="dnav-brand-mark">
            <path d="M20 40V19C20 9 12 3 3 3C3 16 8 25 20 28" />
            <path d="M20 40V19C20 9 28 3 37 3C37 16 32 25 20 28" />
            <path d="M20 40V25" />
          </svg>
          <span>Samavya</span>
        </Link>

        {/* Centre — primary nav (desktop) */}
        <nav className="dnav-primary" aria-label="Primary">
          {topNav.map((item) => {
            const active = item.href === "/dashboard"
              ? pathname === item.href
              : pathname.startsWith(item.href);
            return (
              <Link
                key={item.href}
                href={item.href}
                className={`dnav-link ${active ? "is-active" : ""}`}
                aria-current={active ? "page" : undefined}
              >
                {item.label}
              </Link>
            );
          })}
        </nav>

        {/* Right — search pill, notification bell, profile */}
        <div className="dnav-right">
          <form className="dnav-search" role="search" onSubmit={onSearchSubmit}>
            <span className="dnav-search-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="none">
                <circle cx="11" cy="11" r="7" stroke="currentColor" strokeWidth="1.6" />
                <path d="M20 20L16.5 16.5" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
              </svg>
            </span>
            <label htmlFor="dnav-search-input" className="sr-only">Search anything</label>
            <input
              id="dnav-search-input"
              type="search"
              className="dnav-search-input"
              placeholder="Search anything…"
              value={searchQ}
              onChange={(e) => setSearchQ(e.target.value)}
              autoComplete="off"
            />
          </form>

          <div className="dnav-notif" ref={notifRef}>
            <button
              type="button"
              className="dnav-icon-btn"
              aria-label="Notifications"
              aria-expanded={notifOpen}
              onClick={() => setNotifOpen((v) => !v)}
            >
              <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M6 15V11a6 6 0 0 1 12 0v4l1.5 2H4.5L6 15Z" stroke="currentColor" strokeWidth="1.5" strokeLinejoin="round" />
                <path d="M10 19a2 2 0 0 0 4 0" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
              </svg>
              <span className="dnav-dot" aria-hidden="true" />
            </button>
            {notifOpen && (
              <div className="dnav-pop" role="menu">
                <p className="dnav-pop-title">Notifications</p>
                <ul className="dnav-pop-list">
                  <li>Your plumbing booking was accepted.</li>
                  <li>A group buy you joined needs 2 more members.</li>
                </ul>
              </div>
            )}
          </div>

          <ProfileDropdown firstName={demoUser.firstName} />

          <button
            type="button"
            className="dnav-burger"
            aria-label={mobileOpen ? "Close menu" : "Open menu"}
            aria-expanded={mobileOpen}
            onClick={() => setMobileOpen((v) => !v)}
          >
            <span />
            <span />
            <span />
          </button>
        </div>
      </div>

      {/* Mobile drawer */}
      {mobileOpen && (
        <div className="dnav-mobile" role="dialog" aria-label="Primary navigation">
          <form className="dnav-search dnav-search-mobile" role="search" onSubmit={onSearchSubmit}>
            <span className="dnav-search-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="none">
                <circle cx="11" cy="11" r="7" stroke="currentColor" strokeWidth="1.6" />
                <path d="M20 20L16.5 16.5" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
              </svg>
            </span>
            <input
              type="search"
              className="dnav-search-input"
              placeholder="Search anything…"
              value={searchQ}
              onChange={(e) => setSearchQ(e.target.value)}
            />
          </form>
          <nav aria-label="Primary (mobile)">
            {topNav.map((item) => {
              const active = item.href === "/dashboard"
                ? pathname === item.href
                : pathname.startsWith(item.href);
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  className={`dnav-mobile-link ${active ? "is-active" : ""}`}
                  onClick={() => setMobileOpen(false)}
                >
                  {item.label}
                </Link>
              );
            })}
          </nav>
        </div>
      )}
    </header>
  );
}
