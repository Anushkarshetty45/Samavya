import "./globals.css";
import "lenis/dist/lenis.css";
import type { Metadata } from "next";
import SmoothScrollProvider from "./smooth-scroll-provider";
import ScrollReveals from "./scroll-reveals";

export const metadata: Metadata = {
  title: "Samavya — Your community, all in one place",
  description:
    "Find trusted local services, buy and rent from your neighbourhood, join group buys, and stay connected with your community.",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="en" className="h-full antialiased">
      <head>
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="" />
        <link
          href="https://fonts.googleapis.com/css2?family=Cormorant+Garamond:ital,wght@0,400;0,500;0,600;0,700;1,400;1,500&family=DM+Sans:wght@400;500;600;700&family=Playfair+Display:ital,wght@0,500;0,600;0,700;1,500;1,600&family=Explora&display=swap"
          rel="stylesheet"
        />
      </head>
      <body className="min-h-full flex flex-col">
        <SmoothScrollProvider>
          <ScrollReveals />
          {children}
        </SmoothScrollProvider>
      </body>
    </html>
  );
}
