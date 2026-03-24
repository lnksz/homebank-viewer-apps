# HBViewer

Linux-first Android/Desktop viewer for HomeBank `.xhb` files.

License: BSD 3-Clause. See `LICENSE`.

Current scope:

- import a HomeBank `.xhb` file
- list accounts or search across all accounts
- list transactions for a selected account
- search transactions by free text
- filter transactions by income / expense
- filter transactions by amount thresholds
- show income and expense amounts with distinct colors
- inspect transaction details and splits

Tech stack:

- Kotlin Multiplatform
- Compose Multiplatform
- Kotlin coroutines
- read-only in-memory import

Notes:

- The project now targets Java 25.
- Desktop file picking uses FileKit for better native Linux integration.
- Desktop app runs with `./gradlew desktopApp:run` on Java 25.
- Android Studio can be used for the Android target.
- Imported data is currently kept in memory only, so you need to re-open the `.xhb` file after restarting the app.

Run desktop:

```bash
gradle desktopApp:run
```

Desktop usage:

- click `Open HomeBank file`
- choose your `.xhb`
- use `All accounts` to search across every account
- use the free-text, type, and amount filters to narrow transactions
