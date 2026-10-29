import type { Metadata } from "next";
import Link from "next/link";

export const metadata: Metadata = {
  title: "Privacy policy · Samavya",
  description: "How Samavya handles your words, data, and participation.",
};

const SECTIONS = [
  {
    heading: "What we hold",
    body:
      "Samavya holds only what the service requires: your account details, the requests you make, the people you choose to coordinate with, and the record of actions you confirm. We do not sell or share your data for advertising.",
  },
  {
    heading: "What leaves your account",
    body:
      "Only what each request needs is sent outward, and only to the people who need to see it. A service request shares the minimum necessary details with the provider you select. A community post is visible only within that community.",
  },
  {
    heading: "The AI assistant",
    body:
      "The assistant proposes options and drafts actions, but it never changes your account on its own. Every booking, listing, purchase, or outbound message is reviewed and confirmed by you before it happens.",
  },
  {
    heading: "Your words stay yours",
    body:
      "Conversations with the assistant are retained to continue the thread you started. You can clear a conversation at any time. Transcripts are not used to train external models.",
  },
  {
    heading: "Access and control",
    body:
      "You can export your data, correct it, or close your account from Profile & settings. If you need help, write to the address below and a human will respond.",
  },
  {
    heading: "Changes to this policy",
    body:
      "If this policy changes materially, you will see a notice in your dashboard before it takes effect. The current version date appears at the bottom of this page.",
  },
];

export default function PrivacyPage() {
  return (
    <main className="legal-page">
      <div className="legal-topbar">
        <Link href="/" className="legal-back">
          <span aria-hidden="true">←</span>
          <span>Back to Samavya</span>
        </Link>
      </div>

      <article className="legal-article">
        <span className="legal-eyebrow">PRIVACY · DRAFT</span>
        <h1 className="legal-h">
          Privacy <em>policy.</em>
        </h1>
        <p className="legal-lead">
          Samavya is built around a single idea: your words stay yours.
          This page explains, in plain language, what we hold, what leaves,
          and what you control.
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
          <Link href="/terms" className="legal-foot-link">
            Read the terms &amp; conditions
          </Link>
        </footer>
      </article>
    </main>
  );
}
