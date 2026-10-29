"use client";

import { useState, type ReactNode } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { motion } from "motion/react";

const easeOut = [0.16, 1, 0.3, 1] as const;

type Field = {
  name: string;
  label: string;
  type: "text" | "email" | "password";
  autoComplete: string;
  placeholder?: string;
};

type AuthSceneProps = {
  mode: "login" | "signup";
  eyebrow: string;
  heading: ReactNode;
  description: string;
  fields: Field[];
  submitLabel: string;
  footerQuestion: string;
  footerLinkLabel: string;
  footerLinkHref: string;
};

export default function AuthScene({
  mode,
  eyebrow,
  heading,
  description,
  fields,
  submitLabel,
  footerQuestion,
  footerLinkLabel,
  footerLinkHref,
}: AuthSceneProps) {
  const router = useRouter();
  const [values, setValues] = useState<Record<string, string>>(() =>
    Object.fromEntries(fields.map((f) => [f.name, ""])),
  );
  const [submitting, setSubmitting] = useState(false);

  function handleChange(name: string) {
    return (e: React.ChangeEvent<HTMLInputElement>) =>
      setValues((v) => ({ ...v, [name]: e.target.value }));
  }

  function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    // Frontend-only sign-in / sign-up — no backend wired yet.
    // Any valid-looking input proceeds straight to the dashboard.
    setSubmitting(true);
    router.push("/dashboard/services");
  }

  return (
    <main className="auth-scene" data-mode={mode}>
      {/* ================= LEFT PANE — editorial brand side ================= */}
      <aside className="auth-aside" aria-hidden="true">
        <div className="auth-aside-bg" />
        <div className="auth-aside-grain" />

        <Link href="/" className="auth-brand" aria-label="Samavya home" aria-hidden={false}>
          <svg className="auth-brand-mark" viewBox="0 0 40 44" fill="none">
            <path d="M20 40V19C20 9 12 3 3 3C3 16 8 25 20 28" />
            <path d="M20 40V19C20 9 28 3 37 3C37 16 32 25 20 28" />
            <path d="M20 40V25" />
          </svg>
          <span>Samavya</span>
        </Link>

        <motion.blockquote
          className="auth-aside-quote"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 1.0, ease: easeOut, delay: 0.2 }}
        >
          <p>
            A street that <em>looks after itself</em> is worth more than any
            marketplace could be.
          </p>
          <footer>
            <span className="auth-aside-rule" aria-hidden="true" />
            <span className="auth-aside-attr">THE SAMAVYA NOTE</span>
          </footer>
        </motion.blockquote>

        <div className="auth-aside-foot">
          <span className="auth-aside-dot" />
          <span>SAMAVYA · 2026</span>
        </div>
      </aside>

      {/* ================= RIGHT PANE — form ================= */}
      <section className="auth-form-pane">
        <nav className="auth-topbar">
          <Link href="/" className="auth-back">
            <span aria-hidden="true">←</span>
            <span>Back to home</span>
          </Link>
          <Link href={footerLinkHref} className="auth-switch">
            {footerLinkLabel}
          </Link>
        </nav>

        <motion.div
          className="auth-form-wrap"
          initial="hidden"
          animate="shown"
          variants={{
            hidden: {},
            shown: { transition: { staggerChildren: 0.08, delayChildren: 0.1 } },
          }}
        >
          <motion.div
            className="auth-eyebrow-row"
            variants={{
              hidden: { opacity: 0, y: 14 },
              shown: { opacity: 1, y: 0, transition: { duration: 0.8, ease: easeOut } },
            }}
          >
            <span className="auth-eyebrow">{eyebrow}</span>
            <motion.span
              className="auth-eyebrow-rule"
              initial={{ scaleX: 0 }}
              animate={{ scaleX: 1 }}
              transition={{ duration: 1.1, ease: easeOut, delay: 0.4 }}
              aria-hidden="true"
            />
          </motion.div>

          <motion.h1
            className="auth-heading"
            variants={{
              hidden: { opacity: 0, y: 24 },
              shown: { opacity: 1, y: 0, transition: { duration: 0.9, ease: easeOut } },
            }}
          >
            {heading}
          </motion.h1>

          <motion.p
            className="auth-description"
            variants={{
              hidden: { opacity: 0, y: 18 },
              shown: { opacity: 1, y: 0, transition: { duration: 0.8, ease: easeOut } },
            }}
          >
            {description}
          </motion.p>

          <motion.form
            className="auth-form"
            onSubmit={handleSubmit}
            variants={{
              hidden: {},
              shown: { transition: { staggerChildren: 0.08, delayChildren: 0.2 } },
            }}
          >
            {fields.map((field) => (
              <motion.label
                key={field.name}
                className="auth-field"
                variants={{
                  hidden: { opacity: 0, y: 18 },
                  shown: { opacity: 1, y: 0, transition: { duration: 0.7, ease: easeOut } },
                }}
              >
                <span className="auth-field-label">{field.label}</span>
                <input
                  className="auth-field-input"
                  type={field.type}
                  name={field.name}
                  value={values[field.name] ?? ""}
                  onChange={handleChange(field.name)}
                  autoComplete={field.autoComplete}
                  placeholder={field.placeholder}
                  required
                />
                <span className="auth-field-rule" aria-hidden="true" />
              </motion.label>
            ))}

            {mode === "login" && (
              <motion.div
                className="auth-utility"
                variants={{
                  hidden: { opacity: 0, y: 10 },
                  shown: { opacity: 1, y: 0, transition: { duration: 0.6, ease: easeOut } },
                }}
              >
                <Link href="/forgot" className="auth-ghost-link">
                  Forgot password?
                </Link>
              </motion.div>
            )}

            {mode === "signup" && (
              <motion.p
                className="auth-fine-print"
                variants={{
                  hidden: { opacity: 0, y: 10 },
                  shown: { opacity: 1, y: 0, transition: { duration: 0.6, ease: easeOut } },
                }}
              >
                By creating an account you agree to the Samavya community
                guidelines. Your words stay yours.
              </motion.p>
            )}

            <motion.button
              type="submit"
              className="auth-submit"
              disabled={submitting}
              variants={{
                hidden: { opacity: 0, y: 14 },
                shown: { opacity: 1, y: 0, transition: { duration: 0.8, ease: easeOut } },
              }}
            >
              <span>{submitting ? "Taking you in…" : submitLabel}</span>
              <span className="auth-submit-rule" aria-hidden="true" />
            </motion.button>
          </motion.form>

          <motion.p
            className="auth-footline"
            variants={{
              hidden: { opacity: 0 },
              shown: { opacity: 1, transition: { duration: 0.7, ease: easeOut } },
            }}
          >
            {footerQuestion}{" "}
            <Link href={footerLinkHref} className="auth-footline-link">
              {footerLinkLabel}
            </Link>
          </motion.p>
        </motion.div>
      </section>
    </main>
  );
}
