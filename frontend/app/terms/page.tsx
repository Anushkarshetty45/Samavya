import type { Metadata } from "next";
import Link from "next/link";

export const metadata: Metadata = {
  title: "Terms & conditions · Samavya",
  description: "The simple terms under which Samavya is offered.",
};

const SECTIONS = [
  {
    heading: "Using Samavya",
    body:
      "Samavya is a neighbourhood coordination platform. Use it to request local services, discover marketplace listings, and participate in your community. Please use it kindly — our community guidelines apply to everyone, including the assistant.",
  },
  {
    heading: "Your account",
    body:
      "You are responsible for keeping your account secure and for the actions you confirm. If something looks wrong, you can pause or close your account from Profile & settings.",
  },
  {
    heading: "Confirming actions",
    body:
      "Nothing happens on your behalf without a confirmation. Bookings, listings, payments, and outbound messages are only sent after you review and approve them.",
  },
  {
    heading: "Providers and listings",
    body:
      "Providers are vetted by their fulfilment history and resolved complaints, not by marketing. Listings and group buys are coordinated by the community. Samavya is not a party to the transactions between neighbours.",
  },
  {
    heading: "Content guidelines",
    body:
      "Do not post content that is unsafe, deceptive, or harmful to others. We will remove content that violates these guidelines and may limit access to the service.",
  },
  {
    heading: "Changes and liability",
    body:
      "Samavya is offered as-is while the product is in development. If these terms change materially, you will see a notice in your dashboard before they take effect. The current version date appears at the bottom of this page.",
  },
];

export default function TermsPage() {
  return (
    <main className="legal-page">
      <div className="legal-topbar">
        <Link href="/" className="legal-back">
          <span aria-hidden="true">←</span>
          <span>Back to Samavya</span>
        </Link>
      </div>

      <article className="legal-article">
        <span className="legal-eyebrow">TERMS · DRAFT</span>
        <h1 className="legal-h">
          Terms &amp; <em>conditions.</em>
        </h1>
        <p className="legal-lead">
          A short, plain-language version of the rules under which Samavya is
          offered. Nothing here is legal advice — it&apos;s how we agree to
          work together.
        </p>

        <div className="legal-body">
          {SECTIONS.map((s) => (
            <section key={s.heading} className="legal-section">
              <h2 className="legal-h2">{s.heading}</h2>
              <p>{s.body}</p>
            </section>
          ))}
        </div>

        <footer className="legal-foot">
          <span>Current version · 2026</span>
          <span className="legal-foot-sep" aria-hidden="true">·</span>
          <Link href="/privacy" className="legal-foot-link">
            Read the privacy policy
          </Link>
        </footer>
      </article>
    </main>
  );
}
