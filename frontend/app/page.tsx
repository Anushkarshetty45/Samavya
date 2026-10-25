
const navigation = ["Home", "Services", "Marketplace", "Communities", "About"];

export default function Home() {
  return (
    <main className="home-page">
      <div className="hero-scene">
        <div className="light-panel" aria-hidden="true" />
        <div className="plant-shadow" aria-hidden="true" />
        <div className="floor-light" aria-hidden="true" />

        <header className="navbar">
          <a className="brand" href="/" aria-label="Samavya home">
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
          </a>

          <nav className="nav-links" aria-label="Main navigation">
            {navigation.map((item) => (
              <a
                key={item}
                href={item === "Home" ? "/" : `#${item.toLowerCase()}`}
                className={item === "Home" ? "nav-link active" : "nav-link"}
              >
                {item}
              </a>
            ))}
          </nav>

          <div className="nav-actions">
            <a className="search-link" href="#services" aria-label="Search">
              <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <circle cx="10.8" cy="10.8" r="6.8" />
                <path d="m16 16 5 5" />
              </svg>
            </a>
            <span className="nav-divider" />
            <a className="login-link" href="/login">Log in</a>
            <a className="button button-yellow signup-button" href="/signup">
              Sign up <span aria-hidden="true">↗</span>
            </a>
          </div>
        </header>

        <div className="section-index" aria-hidden="true">
          <span className="index-current">01</span>
          <span className="index-line" />
          <span>02</span>
          <span>03</span>
        </div>

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
            <a className="button button-yellow" href="/signup">
              Get started <span aria-hidden="true">→</span>
            </a>
            <a className="button button-outline" href="#services">
              Explore services
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

        <a className="scroll-cue" href="#services" aria-label="Scroll to services">
          <span className="scroll-circle">
            <span aria-hidden="true">↓</span>
          </span>
          <span className="scroll-line" />
        </a>
      </div>

      <section id="services" className="next-section">
        <span className="next-eyebrow">BUILT AROUND YOUR NEIGHBOURHOOD</span>
        <h2>Everything local, a little closer.</h2>
        <p>
          Discover services, share resources, and find opportunities
          within the communities around you.
        </p>
      </section>
    </main>
  );
}
