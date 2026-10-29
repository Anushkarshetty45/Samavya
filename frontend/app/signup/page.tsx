import type { Metadata } from "next";
import AuthScene from "../components/AuthScene";

export const metadata: Metadata = {
  title: "Sign up · Samavya",
  description: "Join Samavya and bring your neighbourhood closer, one conversation at a time.",
};

export default function SignupPage() {
  return (
    <AuthScene
      mode="signup"
      eyebrow="CREATE AN ACCOUNT"
      heading={
        <>
          Begin a quieter <em>neighbourhood.</em>
        </>
      }
      description="Find trusted local services, share with the people around you, and discover the help that's three floors away."
      fields={[
        {
          name: "name",
          label: "Full name",
          type: "text",
          autoComplete: "name",
          placeholder: "Your name",
        },
        {
          name: "email",
          label: "Email",
          type: "email",
          autoComplete: "email",
          placeholder: "you@neighbourhood.com",
        },
        {
          name: "password",
          label: "Password",
          type: "password",
          autoComplete: "new-password",
        },
      ]}
      submitLabel="Create account"
      footerQuestion="Already with Samavya?"
      footerLinkLabel="Log in"
      footerLinkHref="/login"
    />
  );
}
