# SIAS Grades

Android app that opens the PRMSU SIAS page, catches the `curreval` response and shows your grades on top of the page.

- Grades come from `Previous[].Grade` divided by 100
- Units are divided by 100
- GWA is weighted by units across graded subjects
- The top-level `Grade` field is ignored (scrambled value)

## Get the APK
1. Push this project to a GitHub repo
2. Open the **Actions** tab and wait for **Build APK** to finish
3. Download `SIAS-Grades` from the run's Artifacts, unzip, install the APK

To get a download link under **Releases**, push a tag such as `v1.0`.

## Change the website
Edit `SIAS_URL` in `app/src/main/java/com/theunknown/siasgrades/MainActivity.kt`.
