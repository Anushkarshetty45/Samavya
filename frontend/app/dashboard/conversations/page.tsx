import type { Metadata } from "next";
import ConversationalAssistant from "@/components/dashboard/ConversationalAssistant";
import PlaceholderPage from "@/components/dashboard/PlaceholderPage";

export const metadata: Metadata = { title: "Conversations · Samavya" };

export default function ConversationsPage() {
  return (
    <PlaceholderPage
      eyebrow="CONVERSATIONS"
      heading={<>Talk to Samavya, <em>in your own words.</em></>}
      description="Describe what you need. The assistant organises options, surfaces nearby neighbours, and waits for your confirmation before anything happens."
    >
      <ConversationalAssistant />
    </PlaceholderPage>
  );
}
