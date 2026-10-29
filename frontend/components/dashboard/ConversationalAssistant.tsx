"use client";

import { useEffect, useRef, useState } from "react";
import { motion } from "motion/react";
import { examplePrompts } from "@/lib/demo-data";

const easeOut = [0.16, 1, 0.3, 1] as const;

type DemoMessage = { role: "you" | "samavya"; text: string };

function simulatedReply(prompt: string): string {
  const q = prompt.toLowerCase();
  if (q.includes("plumber")) {
    return "I can shortlist nearby plumbers in your community. You can review the options before confirming anything.";
  }
  if (q.includes("drill") || q.includes("rent")) {
    return "Let me help you find nearby rental options. You can review the available options before confirming anything.";
  }
  if (q.includes("vegetable") || q.includes("vegetables") || q.includes("produce")) {
    return "A local producer added fresh seasonal vegetables yesterday. I can show you what's available.";
  }
  if (q.includes("group") || q.includes("together") || q.includes("bulk")) {
    return "A group purchase for rice and pulses is collecting participants. I can show you the current list and the deadline.";
  }
  return "I'll organise the options and propose next steps. Nothing is booked or sent until you confirm.";
}

const MAX_MESSAGES = 12;

type Props = {
  /** Panel title displayed above the input. */
  title?: string;
  /** Placeholder text for the input. */
  placeholder?: string;
  /** Suggested prompt chips. Defaults to the generic examplePrompts list. */
  prompts?: readonly string[];
  /** Reply engine. Receives the user's text, returns Samavya's response. */
  replyFn?: (prompt: string) => string;
  /** When true, renders the overview hero layout (big title, round send,
   *  'Try asking anything' annotation). Default is the compact card. */
  heroStyle?: boolean;
};

export default function ConversationalAssistant({
  title = "Conversational assistant",
  placeholder = "What do you need help with today?",
  prompts = examplePrompts,
  replyFn = simulatedReply,
  heroStyle = false,
}: Props = {}) {
  const [input, setInput] = useState("");
  const [messages, setMessages] = useState<DemoMessage[]>([]);
  const [thinking, setThinking] = useState(false);
  const threadRef = useRef<HTMLDivElement | null>(null);

  // Auto-scroll the thread to the latest message as it arrives.
  useEffect(() => {
    const el = threadRef.current;
    if (!el) return;
    el.scrollTo({ top: el.scrollHeight, behavior: "smooth" });
  }, [messages, thinking]);

  function send(text: string) {
    const q = text.trim();
    if (!q) return;
    const userMsg: DemoMessage = { role: "you", text: q };
    setMessages((m) => [...m, userMsg].slice(-MAX_MESSAGES));
    setInput("");
    setThinking(true);
    window.setTimeout(() => {
      const reply: DemoMessage = { role: "samavya", text: replyFn(q) };
      setMessages((m) => [...m, reply].slice(-MAX_MESSAGES));
      setThinking(false);
    }, 420);
  }

  function resetThread() {
    setMessages([]);
    setThinking(false);
  }

  function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    send(input);
  }

  return (
    <motion.section
      className={`dasst ${heroStyle ? "dasst-hero" : ""}`}
      aria-labelledby="dasst-h"
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.8, ease: easeOut, delay: 0.3 }}
    >
      {heroStyle ? (
        <h2 id="dasst-h" className="dasst-hero-title">
          {title}
        </h2>
      ) : (
        <div className="dasst-head">
          <h2 id="dasst-h" className="dasst-title">
            {title}
          </h2>
          <span className="dasst-tag">Demo preview</span>
        </div>
      )}

      <div className="dasst-form-wrap">
        <form className="dasst-form" onSubmit={handleSubmit}>
          <label htmlFor="dasst-input" className="sr-only">
            {placeholder}
          </label>
          {heroStyle && (
            <span className="dasst-leading" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="none">
                <circle cx="11" cy="11" r="7" stroke="currentColor" strokeWidth="1.6" />
                <path d="M20 20L16.5 16.5" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
              </svg>
            </span>
          )}
          <input
            id="dasst-input"
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder={placeholder}
            className="dasst-input"
            autoComplete="off"
          />
          {heroStyle ? (
            <button type="submit" className="dasst-send-round" aria-label="Send">
              <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
            </button>
          ) : (
            <button type="submit" className="dasst-send" aria-label="Send">
              Send
            </button>
          )}
        </form>
        {heroStyle && (
          <aside className="dasst-annot" aria-hidden="true">
            <span className="dasst-annot-text">Try asking anything</span>
            <svg className="dasst-annot-arrow" viewBox="0 0 60 48" fill="none">
              <path
                d="M8 4 Q 28 6, 42 20 T 50 40"
                stroke="currentColor"
                strokeWidth="1.4"
                strokeLinecap="round"
                fill="none"
              />
              <path
                d="M44 36 L 52 42 L 44 44"
                stroke="currentColor"
                strokeWidth="1.4"
                strokeLinecap="round"
                strokeLinejoin="round"
                fill="none"
              />
            </svg>
          </aside>
        )}
      </div>

      <div className="dasst-prompts" role="list" aria-label="Suggested prompts">
        {prompts.map((p) => (
          <button
            key={p}
            type="button"
            className="dasst-chip"
            onClick={() => setInput(p)}
            role="listitem"
          >
            {p}
          </button>
        ))}
      </div>

      {(messages.length > 0 || thinking) && (
        <div className="dasst-thread-wrap">
          <div className="dasst-thread-head">
            <span className="dasst-thread-label">Conversation preview</span>
            <button
              type="button"
              className="dasst-clear"
              onClick={resetThread}
              aria-label="Clear conversation"
            >
              Clear
            </button>
          </div>
          <div className="dasst-thread" aria-live="polite" ref={threadRef}>
            {messages.map((m, i) => (
              <div key={i} className={`dasst-msg dasst-msg-${m.role}`}>
                <span className="dasst-who">{m.role === "you" ? "You" : "Samavya"}</span>
                <p className="dasst-msg-text">{m.text}</p>
              </div>
            ))}
            {thinking && (
              <div className="dasst-msg dasst-msg-samavya">
                <span className="dasst-who">Samavya</span>
                <p className="dasst-msg-text dasst-msg-dots">
                  <span /><span /><span />
                </p>
              </div>
            )}
          </div>
        </div>
      )}
    </motion.section>
  );
}
