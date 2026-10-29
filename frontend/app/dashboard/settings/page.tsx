import type { Metadata } from "next";
import PlaceholderPage from "@/components/dashboard/PlaceholderPage";
import { demoUser } from "@/lib/demo-data";

export const metadata: Metadata = { title: "Profile & Settings · Samavya" };

export default function SettingsPage() {
  return (
    <PlaceholderPage
      eyebrow="PROFILE & SETTINGS"
      heading={<>Your profile, <em>your words.</em></>}
      description="Control how the assistant represents you, what gets shared with neighbours, and the preferences that shape your Samavya experience."
    >
      <div className="dash-settings-grid">
        <section className="dash-settings-card">
          <h2 className="dash-settings-h">Profile</h2>
          <dl className="dash-settings-dl">
            <div><dt>Name</dt><dd>{demoUser.firstName}</dd></div>
            <div><dt>Neighbourhood</dt><dd>{demoUser.neighbourhood}</dd></div>
            <div><dt>Preferred language</dt><dd>English</dd></div>
          </dl>
          <p className="dash-settings-note">
            Placeholder values. Editing is not wired to a backend.
          </p>
        </section>

        <section className="dash-settings-card">
          <h2 className="dash-settings-h">Privacy</h2>
          <ul className="dash-settings-list">
            <li>
              <span>Share only what each request needs</span>
              <span className="dash-settings-pill">On</span>
            </li>
            <li>
              <span>Confirm every action before it happens</span>
              <span className="dash-settings-pill">On</span>
            </li>
            <li>
              <span>Hide my activity from public listings</span>
              <span className="dash-settings-pill dash-settings-pill-off">Off</span>
            </li>
          </ul>
        </section>

        <section className="dash-settings-card">
          <h2 className="dash-settings-h">Notifications</h2>
          <ul className="dash-settings-list">
            <li><span>Service updates</span><span className="dash-settings-pill">On</span></li>
            <li><span>Group buy reminders</span><span className="dash-settings-pill">On</span></li>
            <li><span>Community announcements</span><span className="dash-settings-pill">On</span></li>
            <li><span>Promotional messages</span><span className="dash-settings-pill dash-settings-pill-off">Off</span></li>
          </ul>
        </section>
      </div>
    </PlaceholderPage>
  );
}
