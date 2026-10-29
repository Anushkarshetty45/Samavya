import type { Metadata } from "next";
import AuthScene from "../components/AuthScene";

export const metadata: Metadata = {
  title: "Log in · Samavya",
  description: "Return to your Samavya conversation and your neighbourhood.",
};

export default function LoginPage() {
  return (
    <AuthScene
      mode="login"
      eyebrow="LOG IN"
      heading={
        <>
          Welcome <em>back.</em>
        </>
      }
      description="Pick up where you left off — your conversations, your neighbours, your community."
      fields={[
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
          autoComplete: "current-password",
        },
      ]}
      submitLabel="Continue"
      footerQuestion="New to Samavya?"
      footerLinkLabel="Create an account"
      footerLinkHref="/signup"
    />
  );
}
