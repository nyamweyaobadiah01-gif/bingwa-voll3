# Bingwa Auto

By Obadiah Tech. An Android app for your own phone and Safaricom line. When an M-PESA "money received" SMS arrives, it matches the amount to a bundle in YOUR list and sends the USSD code you entered for that bundle, with the customer's number filled in.

Nothing is hard-coded. You enter every bundle and USSD code yourself.

## Screens
- **Setup:** allow SMS and Phone permissions, optionally allow background running.
- **Home:** Auto-send switch, sent today, last M-PESA balance, and real counts of completed, pending, failed, held, unmatched and no-number orders.
- **Orders:** full history. Tap a failed or held order to send it again.
- **Bundles:** edit the list, one per line: `amount|label|USSD code with {phone}`
- **Settings:** auto-send limit, clear order history.
- **Privacy:** the privacy policy.

## Build
Push to GitHub and the workflow in `.github/workflows/main.yml` builds `app-debug.apk` (Actions tab > latest run > Artifacts). Or open the folder in Android Studio.

## Built-in safety
- Off by default. Nothing is sent until you turn it on.
- Only SMS from the sender "MPESA", only "money received" messages.
- Each M-PESA transaction code is processed once.
- Fake-SMS check: the balance in the message must equal your last known balance plus the payment, otherwise the order is held.
- Orders above your auto-send limit (KSh 500 by default) are held for approval.
- Orders marked SENDING or FAILED are never retried by themselves.
- Codes starting with *334 (the M-PESA menu) are refused. The app never asks for your PIN.

## Known limits
- Single-string USSD codes only. Multi-step menus are not supported.
- Success is judged from the network's reply text, so test with small amounts first.
- Uses the default SIM.
- Keep the phone on, charged and online, and exclude the app from battery optimisation.
- Check Safaricom's terms for reselling and automated USSD before using it with customers.
