# ⚡ Electricity Bill

> ICT602 Mobile Technology and Development | Individual Assignment

An Android application to estimate monthly electricity bills based on TNB (Tenaga Nasional Berhad) tiered tariff block pricing. Users can calculate charges, apply rebate, and manage all records in a local SQLite database.

---

## Features

- Select billing month (January – December)
- Enter electricity units used (1 – 1000 kWh)
- Apply rebate percentage (0% – 5%) via slider
- View detailed block-by-block charge breakdown
- Save, view, edit, and delete records (CRUD)
- ListView displays all saved bills (Month + Final Cost)
- About page with app info, instructions, and GitHub link

---

## TNB Tariff Block Rates

| Block | Usage | Rate |
|-------|-------|------|
| Block 1 | 1 – 200 kWh | 21.8 sen/kWh |
| Block 2 | 201 – 300 kWh | 33.4 sen/kWh |
| Block 3 | 301 – 600 kWh | 51.6 sen/kWh |
| Block 4 | 601 – 1000 kWh | 54.6 sen/kWh |

```
Final Cost = Total Charges − (Total Charges × Rebate %)
```

---

## Tech Stack

- **Language:** Java
- **IDE:** Android Studio
- **Database:** SQLite
- **UI:** XML + Material Components
- **Min SDK:** API 24 (Android 7.0)
- **Theme:** Sky Blue + Aqua

---

## Installation

1. Clone this repository
   ```bash
   git clone https://github.com/2022267534-Syahidah/ElectricityBill.git
   ```
2. Open in Android Studio
3. Wait for Gradle sync
4. Run on emulator or physical device (API 24+)

---

## How to Use

1. Select the billing **month**
2. Enter **units used** (1 – 1000 kWh)
3. Set **rebate** percentage (0 – 5%) using the slider
4. Tap **CALCULATE** to see full block breakdown
5. Tap **SAVE TO DATABASE** to store the record
6. Tap **Records** to view all saved bills
7. Tap any record to view, edit, or delete

---

## Project Structure

```
app/src/main/
├── java/com/example/electricitybill/
│   ├── MainActivity.java        # Calculator + save
│   ├── ListActivity.java        # ListView of all records
│   ├── DetailActivity.java      # View full record
│   ├── EditActivity.java        # Edit record
│   ├── AboutActivity.java       # About page
│   ├── DatabaseHelper.java      # SQLite helper
│   ├── BillModel.java           # Data model
│   └── BillCalculator.java      # Calculation logic
└── res/
    ├── layout/                  # XML UI layouts
    ├── values/
    │   ├── colors.xml
    │   ├── strings.xml
    │   └── themes.xml
    └── drawable/                # Icons & backgrounds
```

---

## Developer

| | |
|---|---|
| **Name** | Siti Nursyahidah |
| **Student ID** | 2022267534 |
| **Course** | ICT602 – Mobile Technology and Development |
| **Institution** | Universiti Teknologi MARA (UiTM) |

---

## Demo

📺 YouTube: *[Insert demo video link here]*

---

© 2025 Electricity Bill App. All rights reserved.
