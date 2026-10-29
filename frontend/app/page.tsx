import Link from "next/link";
import QuietMechanic from "./components/QuietMechanic";
import SiteHeader from "./components/SiteHeader";
import ServicesScene from "./components/ServicesScene";
import ServicesIndex from "./components/ServicesIndex";
import ServicesAside from "./components/ServicesAside";
import MarketplaceScene from "./components/MarketplaceScene";
import CommunitiesScene from "./components/CommunitiesScene";
import AboutScene from "./components/AboutScene";

export default function Home() {
  return (
    <main className="home-page">
      <SiteHeader />

      {/* ============================ SCENE 01,FOLIAGE HERO ============================ */}
      <div className="hero-scene" data-nav-tone="dark">
        <section className="hero-content" aria-labelledby="hero-heading">
          <div className="eyebrow">
            <span>NEIGHBOURS</span>
            <span>SERVICES</span>
            <span>OPPORTUNITIES</span>
            <span className="eyebrow-second">A STRONGER TOMORROW</span>
          </div>

          <h1 id="hero-heading">
            Your community,
            <br />
            <em>all in one place.</em>
          </h1>

          <p className="hero-description">
            Find trusted local services, buy and rent from your
            neighbourhood, join group buys, and stay connected
            with your community.
          </p>

          <div className="hero-actions">
            <a className="hero-cta hero-cta-primary" href="/signup">
              <span>Start a conversation</span>
              <span className="hero-cta-rule" aria-hidden="true" />
            </a>
            <a className="hero-cta hero-cta-ghost" href="#interstitial">
              <span>See how it works</span>
            </a>
          </div>
        </section>

        <div className="scene-caption">
          <span>LOCAL SERVICES</span>
          <span className="caption-separator">|</span>
          <span>MARKETPLACE</span>
          <span className="caption-separator">|</span>
          <span>GROUP BUYING</span>
          <span className="caption-separator">|</span>
          <span>RENT &amp; SHARE</span>
          <div className="caption-rule" />
        </div>

        <a className="scroll-cue" href="#interstitial" aria-label="Scroll to how it works">
          <span className="scroll-circle">
            <span aria-hidden="true">↓</span>
          </span>
          <span className="scroll-line" />
        </a>
      </div>

      {/* ============================ SCENE 02,THE QUIET MECHANIC ============================ */}
      <QuietMechanic />

      {/* ============================ SCENE 03,SERVICES SPREAD ============================ */}
      <ServicesScene>
        <div className="services-spread">
          <ServicesAside />
          <ServicesIndex />
        </div>
      </ServicesScene>

      {/* ============================ SCENE 04,MARKETPLACE "SHARED LOCALLY" ============================ */}
      <MarketplaceScene />

      {/* ============================ SCENE 05,COMMUNITIES ============================ */}
      <CommunitiesScene />

      {/* ============================ SCENE 06,ABOUT ============================ */}
      <AboutScene />

      {/* ============================ FOOTER ============================ */}
      <footer className="site-footer">
        <div className="footer-grid">
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
          <nav className="footer-nav" aria-label="Footer">
            <a href="#services">Services</a>
            <a href="#marketplace">Marketplace</a>
            <a href="#communities">Communities</a>
            <a href="#about">About</a>
            <Link href="/privacy">Privacy</Link>
            <Link href="/terms">Terms</Link>
          </nav>
          <p className="footer-tag">
            Bringing community needs and local services together,<br />
            one conversation at a time.
          </p>
        </div>
        <div className="footer-rule" />
        <p className="footer-fine">Samavya · 2026</p>
      </footer>
    </main>
  );
}
