# AWS SNS SMS with origination for Afghanistan – step-by-step guide

This guide walks you through using **AWS SNS** to send SMS with **origination suitable for Afghanistan** (sender ID, E.164 numbers, one-way messaging).

---

## Afghanistan-specific rules (summary)

| Item | Details |
|------|--------|
| **Country code** | +93 |
| **Number format** | E.164: `+93 7X XXX XXXX` (e.g. +93701234567) |
| **Origination** | Use a **Sender ID** (alphanumeric name). Long codes and short codes are **not** supported for Afghanistan. |
| **Two-way SMS** | Not supported – outbound only. |
| **MMS** | Not supported (or falls back to SMS with link). |
| **Sender ID** | Recommended to pre-register with local authorities/carriers for better delivery. |

AWS supports sending SMS **to** Afghanistan; “origination from Afghanistan” here means your **sender identity** is configured so that messages are compliant for delivery to Afghan numbers (using a Sender ID).

---

## Step 1: AWS account and region

1. Log in to the [AWS Console](https://console.aws.amazon.com/).
2. Choose a region where **SNS SMS** is available (e.g. **ap-south-1**, **us-east-1**). Your app already uses `ap-south-1` for S3; you can use the same for SNS.
3. Ensure your account is out of the **SNS SMS sandbox** if you need to send to arbitrary numbers (see Step 5).

---

## Step 2: IAM permissions for SNS SMS

1. In AWS Console go to **IAM** → **Users** (or **Roles** if your app runs on EC2/ECS).
2. Open the user/role used by your application.
3. **Add permissions** → **Attach policies directly** (or create an inline policy).
4. Either attach **AmazonSNSFullAccess** (broad) or a custom policy that allows:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "sns:Publish",
        "sns:SetSMSAttributes",
        "sns:GetSMSAttributes"
      ],
      "Resource": "*"
    }
  ]
}
```

5. Save. If using access keys, ensure the app has `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` (or use IAM role when on AWS).

---

## Step 3: Enable SMS in SNS and set default type

1. In AWS Console search for **SNS** (Simple Notification Service) and open it.
2. In the left pane go to **Text messaging (SMS)** (or **Mobile** → **Text messaging (SMS)** depending on console layout).
3. **Preferences** (or **Account settings**):
   - Set **Default message type** to **Transactional** (for OTPs) or **Promotional** (for marketing). For login OTP use **Transactional**.
   - Set **Default SMS sender ID** (see Step 4).
   - Optionally set **Default SMS maximum price** to avoid unexpected cost.

---

## Step 4: Sender ID for Afghanistan (origination identity)

For Afghanistan you must use a **Sender ID** (alphanumeric name), not a phone number.

1. In SNS go to **Text messaging (SMS)** → **Origination identities** (or **Sender IDs** in newer flows).
2. **Create** or **Request** a sender ID:
   - **Sender ID**: e.g. `Tranzo` or `YourBrand` (typically 3–11 alphanumeric characters; rules vary by country).
   - **Country**: Select **Afghanistan (AF)** if the option is available.
3. Some countries require **pre-registration** of the sender ID with a regulator or carrier. Check local requirements for Afghanistan and complete registration if needed so carriers don’t block or filter your messages.
4. Note the **Sender ID** value; you’ll use it when publishing SMS (or set it as account default in Step 3).

If the SNS console flow has moved to **AWS End User Messaging SMS**, create/register the sender ID there and ensure it’s linked for use with SNS in the same account/region.

---

## Step 5: Leave SNS SMS sandbox (if needed)

1. In SNS → **Text messaging (SMS)** → **Sandbox** (or **Account status**).
2. By default, SMS is in **sandbox**: you can only send to verified destination numbers.
3. To send to **any** Afghan number (e.g. for OTP):
   - Request **production access** for SMS (link or button in the same page).
   - Fill in use case (e.g. “OTP for user login”), expected volume, and opt-in method.
   - Wait for AWS to approve (can take a few days).

Until then, add the test numbers in **Sandbox destination phone numbers** and use those for testing.

---

## Step 6: Set application attributes (optional but recommended)

1. In SNS → **Text messaging (SMS)** → **Application configuration** (or **Settings**).
2. You can set:
   - **DefaultSenderID**: Your sender ID (e.g. `Tranzo`) so every SMS uses it when sending to countries that support it (e.g. Afghanistan).
   - **DefaultSMSType**: `Transactional` or `Promotional`.

Or set attributes per request in code (see Step 8).

---

## Step 7: Phone number format for Afghanistan

- Use **E.164**: country code + number, no spaces or leading zero.
- Afghanistan: `+93` + 9 digits (e.g. mobile: 70xxxxxxx).
- Example: `+93701234567`.

Normalize in your app (strip spaces, add `+93` if user enters 07xxx).

---

## Step 8: Send SMS from your application (Java/Spring)

### 8.1 Add SNS dependency

In `pom.xml` (alongside your existing `software.amazon.awssdk` S3 dependency):

```xml
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>sns</artifactId>
    <version>2.28.0</version>
</dependency>
```

(Use the same version as your S3 SDK if possible.)

### 8.2 Configuration

In `application.yaml` (or `application-prod.yaml`):

```yaml
app:
  sms:
    enabled: ${AWS_SNS_SMS_ENABLED:false}
    sender-id: ${AWS_SNS_SENDER_ID:Tranzo}   # Sender ID for Afghanistan
aws:
  sns:
    region: ${AWS_REGION:ap-south-1}
```

Environment variables for production:

- `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` (or IAM role).
- `AWS_REGION` (e.g. `ap-south-1`).
- `AWS_SNS_SMS_ENABLED=true`.
- `AWS_SNS_SENDER_ID=YourBrand`.

### 8.3 SNS client and SMS service

- Create an `SnsClient` (same region as in config).
- Implement a small `SmsService` that:
  - Takes phone number (E.164) and message body.
  - Calls `snsClient.publish(PublishRequest.builder().phoneNumber(phone).message(message).build())`.
- For Afghanistan, set **message attributes** so that the **SenderID** is used (if not using account default):
  - Attribute name: `AWS.SNS.SMS.SenderID`, type `String`, value = your sender ID.
  - Attribute name: `AWS.SNS.SMS.SMSType`, type `String`, value = `Transactional`.

(Exact attribute names may vary slightly by SDK version; check [SNS Publish API](https://docs.aws.amazon.com/sns/latest/api/API_Publish.html) and [SMS attributes](https://docs.aws.amazon.com/sns/latest/dg/sms_attributes.html).)

### 8.4 Use in OtpService

In `OtpService.sendOtp()`:

- After generating the OTP and putting it in the cache, if `app.sms.enabled` is true and the request contains a valid Afghan number (e.g. E.164 +93…):
  - Call your SMS service: `smsService.sendSms(phoneNumber, "Your OTP is " + otp + ". Valid for 10 minutes.")`.
- Keep the existing log/email TODO if you use email as well.

---

## Step 9: Test

1. **Sandbox**: Add an Afghan test number (+93…) in SNS Sandbox destinations and send an OTP. Check phone and SNS → **Text messaging** → **Delivery status** (or CloudWatch metrics).
2. **Production**: After approval, send to a real number and confirm delivery and sender ID display.

---

## Step 10: Monitoring and costs

- **CloudWatch**: SNS metrics (NumberOfMessagesSent, NumberOfNotificationsFailed, etc.) in the same region.
- **SNS**: **Text messaging** → **Delivery status** (or equivalent) for recent sends.
- **Pricing**: Per-SMS cost for Afghanistan in [SNS pricing](https://aws.amazon.com/sns/sms-pricing/). Use **Transactional** for OTP to avoid strict opt-in rules that apply to Promotional in some regions.

---

## Checklist

- [ ] IAM permissions for SNS Publish (and Get/SetSMSAttributes if needed).
- [ ] SNS SMS preferences: default type (Transactional), default sender ID if desired.
- [ ] Sender ID created/requested for Afghanistan; local registration done if required.
- [ ] Sandbox: test numbers added; or production access requested and approved.
- [ ] App: dependency, config, SNS client, SMS service, E.164 normalization for +93.
- [ ] OtpService (or similar) calls SMS service when SMS is enabled and number is present.
- [ ] Monitoring and budget (CloudWatch, cost alerts).

---

## References

- [Origination identities for Amazon SNS SMS](https://docs.aws.amazon.com/sns/latest/dg/channels-sms-originating-identities.html)
- [AWS End User Messaging SMS – Sender IDs](https://docs.aws.amazon.com/sms-voice/latest/userguide/sender-id.html)
- [Supported countries and regions for SMS](https://docs.aws.amazon.com/sms-voice/latest/userguide/phone-numbers-sms-by-country.html)
- [SNS Publish API](https://docs.aws.amazon.com/sns/latest/api/API_Publish.html)
