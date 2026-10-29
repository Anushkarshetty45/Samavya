"use client";

import { useEffect, useRef, useState } from "react";
import Link from "next/link";

type Props = { firstName: string };

export default function ProfileDropdown({ firstName }: Props) {
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    function onDown(e: MouseEvent) {
      if (!ref.current) return;
      if (!ref.current.contains(e.target as Node)) setOpen(false);
    }
    function onKey(e: KeyboardEvent) {
      if (e.key === "Escape") setOpen(false);
    }
    document.addEventListener("mousedown", onDown);
    document.addEventListener("keydown", onKey);
    return () => {
      document.removeEventListener("mousedown", onDown);
      document.removeEventListener("keydown", onKey);
    };
  }, []);

  return (
    <div className="dnav-profile" ref={ref}>
      <button
        type="button"
        className="dnav-profile-btn"
        aria-haspopup="menu"
        aria-expanded={open}
        onClick={() => setOpen((v) => !v)}
      >
        <span className="dnav-avatar" aria-hidden="true">
          {firstName.charAt(0)}
        </span>
        <span className="dnav-profile-name">{firstName}</span>
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true" className="dnav-profile-chev">
          <path d="M6 9l6 6 6-6" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      </button>

      {open && (
        <div className="dnav-pop dnav-pop-right" role="menu">
          <Link href="/dashboard/settings" className="dnav-pop-link" role="menuitem" onClick={() => setOpen(false)}>
            Profile and settings
          </Link>
          <Link href="/dashboard/bookings" className="dnav-pop-link" role="menuitem" onClick={() => setOpen(false)}>
            My bookings
          </Link>
          <Link href="/dashboard/notifications" className="dnav-pop-link" role="menuitem" onClick={() => setOpen(false)}>
            Notifications
          </Link>
        </div>
      )}
    </div>
  );
}
