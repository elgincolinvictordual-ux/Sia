# SIAS Grades

Android app that opens the PRMSU SIAS page and shows your grades in a clean sheet.

## How it works
- Catches the `curreval` response while you browse
- Grades = `Previous[].Grade` / 100, units = `Units` / 100 (the top-level `Grade` is scrambled and ignored)
- GWA is weighted by units across graded subjects. It uses the 2-decimal rounded grades the site sends, so it can differ slightly from the official GWA on the page (the sheet says so)
- After login, the app automatically taps Reports > Curriculum Evaluation on the visible page (a brief flash, under a second) so you don't have to, and captures the grade response as soon as it loads
- A floating atom button appears once the data is loaded; tap it to open the grades sheet (it hides while the sheet is open)
- On logout (tapping Logout, a logout request, or the login page showing again) the grades and button are cleared, so the next user starts clean
- Back button: closes the sheet, then goes back in the site, then press twice to exit

## Get the APK
1. Push this project to a GitHub repo
2. Open the **Actions** tab and wait for **Build APK** to finish
3. Download `SIAS-Grades` from the run's Artifacts, unzip, install the APK

Push a tag such as `v1.1` to also get a download under **Releases**.

The APK is signed with `app/sias.keystore` (kept in the repo on purpose), so new builds install over old ones.
If you installed an older build signed with a different key, uninstall it once first.

## Change the website
Edit `SIAS_URL` in `app/src/main/java/com/theunknown/siasgrades/MainActivity.kt`.

## Sharing this with classmates
Release builds are shrunk and obfuscated (R8), and the network path used to fetch grades
is not stored as plain text in the source, as a light layer of protection if the APK is
decompiled. The bigger step is on GitHub: keep this repository **Private** and only hand
out the built APK (from Actions artifacts or a Release), not repo access. Obfuscating code
does nothing if the source itself is public.
