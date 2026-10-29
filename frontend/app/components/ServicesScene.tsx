"use client";

import { useRef } from "react";
import ServicesBackdrop from "./ServicesBackdrop";


export default function ServicesScene({ children }: { children: React.ReactNode }) {
  const ref = useRef<HTMLElement>(null);
  return (
    <section
      id="services"
      ref={ref}
      className="page-section section-cream services-section"
      data-nav-tone="dark"
    >
      <ServicesBackdrop targetRef={ref} />
      <div className="svc-content">{children}</div>
    </section>
  );
}
