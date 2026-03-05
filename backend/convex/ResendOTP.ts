import Resend from "@auth/core/providers/resend";
import { Resend as ResendAPI } from "resend";

export const ResendOTP = Resend({
  id: "resend-otp",
  apiKey: process.env.AUTH_RESEND_KEY,
  async generateVerificationToken() {
    // Generate 6-digit numeric code
    const array = new Uint8Array(6);
    crypto.getRandomValues(array);
    return Array.from(array, (b) => (b % 10).toString()).join("");
  },
  async sendVerificationRequest({
    identifier: email,
    provider,
    token,
  }) {
    const resend = new ResendAPI(provider.apiKey);
    const { error } = await resend.emails.send({
      from: "Expense Tracker <onboarding@resend.dev>",
      to: [email],
      subject: "Verify your email - Expense Tracker",
      html: `
        <div style="font-family: Arial, sans-serif; max-width: 480px; margin: 0 auto; padding: 20px;">
          <h2 style="color: #8B593E; text-align: center;">Expense Tracker</h2>
          <p style="font-size: 16px; color: #333;">Your verification code is:</p>
          <div style="background: #f5f5f5; border-radius: 8px; padding: 20px; text-align: center; margin: 20px 0;">
            <span style="font-size: 32px; font-weight: bold; letter-spacing: 4px; color: #8B593E;">${token}</span>
          </div>
          <p style="font-size: 14px; color: #666;">This code expires in 15 minutes. If you didn't request this, please ignore this email.</p>
        </div>
      `,
    });

    if (error) {
      console.error("Resend API Error:", error);
      console.log("=========================================================================");
      console.log(`⚠️  SIMULATED EMAIL TO: ${email} (Failed to send via Resend Sandbox)`);
      console.log(`🔑  YOUR VERIFICATION CODE IS: ${token}`);
      console.log("=========================================================================");
      // We intentionally don't throw here so that you can continue testing the flow locally
      // even if Resend blocks the email delivery due to free-tier Sandbox restrictions.
    }
  },
});
