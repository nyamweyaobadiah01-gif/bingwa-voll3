package com.example.bingwaauto

/** The privacy policy shown inside the app. Keep in step with the published copy. */
object PolicyText {
    val FULL: String = listOf(
        "Last updated: 1 November 2026",

        "Bingwa Auto keeps your payment and order information on your own phone and does not send it " +
            "to any server. This policy explains what the Android app handles and why.",

        "1. Overview\n" +
            "Bingwa Auto is an Android app published by Obadiah Tech (\"we\", \"the publisher\"). It runs on " +
            "the phone of the person who installs it (\"you\", \"the operator\"). When an M-PESA payment message " +
            "arrives, the app matches the amount to a bundle in a list you set up, and sends the Safaricom USSD " +
            "code you entered for that bundle.\n\n" +
            "This policy covers the Bingwa Auto app only.",

        "2. Information the app handles\n" +
            "The app reads only incoming SMS whose sender is \"MPESA\". Other messages are ignored. From a " +
            "\"money received\" message it records:\n" +
            "• the M-PESA transaction code\n" +
            "• the amount paid\n" +
            "• the sender's name and phone number, when the message shows them\n" +
            "• the time the order was received\n" +
            "• the order's status (for example sent, failed or held) and the reply the mobile network gave " +
            "to the USSD request\n\n" +
            "The app also keeps the settings you enter: your bundle list, the on/off switch, your auto-send " +
            "limit, and your last M-PESA balance (used to spot fake messages).\n\n" +
            "The app does not collect your location, contacts, photos, device identifiers, advertising ID or " +
            "any account details. It never asks for, reads or stores your M-PESA PIN. It does not keep the " +
            "full text of the SMS messages it reads.",

        "3. How the information is used\n" +
            "The app uses the information only to run the service you set it up for:\n" +
            "• to match a payment to a bundle in your list and send the USSD code for it\n" +
            "• to avoid delivering twice for the same M-PESA transaction code\n" +
            "• to show you an order history, so you can see what was sent, what failed and what was held\n" +
            "• to check that the balance in a payment message fits your last known balance, which helps " +
            "catch fake messages\n\n" +
            "It is not used for advertising, profiling or analytics.",

        "4. Storage and security\n" +
            "All information stays in the app's private storage on your phone. The app does not ask Android " +
            "for internet permission, so it cannot upload your data to a server. It has no analytics, " +
            "advertising or crash-reporting tools, and Android backup is turned off for it.\n\n" +
            "Because the data lives on the phone, its safety depends on the phone. Use a screen lock, keep " +
            "the phone's software up to date, and use a spare phone and line for the app if you can. Anyone " +
            "who can unlock the phone can see the order history.",

        "5. Sharing\n" +
            "We do not sell, rent or share your information, and we do not receive it, because it never " +
            "leaves your phone through the app.\n\n" +
            "The one outward action is the USSD request the app sends to the mobile network. It contains the " +
            "code you entered and the customer's phone number, just as if you had dialled it by hand. " +
            "Safaricom handles that request under its own privacy terms.",

        "6. Permissions the app asks for\n" +
            "• Receive SMS: to notice new M-PESA payment messages as they arrive\n" +
            "• Phone (make calls): to send the USSD code to the mobile network\n\n" +
            "The app does not ask to read your existing messages, send SMS, or access your contacts. You can " +
            "withdraw either permission at any time in your phone's settings, and the app will then stop " +
            "working until you allow it again.",

        "7. Keeping and deleting your information\n" +
            "Order records stay on the phone until you remove them. To erase them, open Settings in the app " +
            "and tap Clear order history. You can also erase everything by opening your phone's Settings > " +
            "Apps > Bingwa Auto > Storage and tapping Clear storage. Uninstalling the app also removes all its " +
            "data. Because nothing is sent to us, we hold no copy that we could delete for you.",

        "8. Your responsibilities, children, changes and contact\n" +
            "Your customers' information. If you use the app to serve customers, you handle their phone " +
            "numbers and names. Under Kenya's Data Protection Act, 2019, a person or business that decides why " +
            "and how personal data is processed may have duties, such as registering with the Office of the " +
            "Data Protection Commissioner, telling customers what you do with their details, and keeping those " +
            "details secure. Check what applies to you. This policy is not legal advice.\n\n" +
            "Children. The app is a business tool and is not meant for children.\n\n" +
            "Changes. This policy may be updated from time to time, depending on the needs of the market or " +
            "improvements to the app. If a change affects how your information is handled, we will notify you " +
            "through the contact details you have shared with us before it takes effect. The date at the top " +
            "of this policy will show when it was last updated.\n\n" +
            "Contact. For questions about this policy, contact Obadiah Tech on 0708590547 or at " +
            "nyamweyaobadiah01@gmail.com.",

        "Your privacy, our priority. Powered by Obadiah Tech.\n© 2026 Obadiah Tech. All rights reserved."
    ).joinToString("\n\n")
}
