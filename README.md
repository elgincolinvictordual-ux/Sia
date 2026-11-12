# SIAS Grades

Android app that opens the PRMSU SIAS page and shows your grades in a clean sheet.

## How it works
- Catches the `curreval` response while you browse
- Grades = `Previous[].Grade` / 100, units = `Units` / 100 (the top-level `Grade` is scrambled and ignored)
- GWA is weighted by units across graded subjects
- A slide-in banner tells you to open **Reports > Curriculum Evaluation** after login
- A floating **Grades** button appears once the data is loaded; tap it any time to reopen the sheet
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
