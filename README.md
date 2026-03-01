# Cherkabe Temple Receipt Printer

A JavaFX-based desktop application for managing temple operations at Shri Shasthara Subrahmanyeshwara Devasthana, Cherkabe, Karnataka, India.

## Features

- **Seva Management** - Record and print receipts for temple ritual services
- **Donation Management** - Handle monetary donations with proper receipts
- **Karyakrama Management** - Manage events and programs
- **Special Poojas (Vishesha Pooje)** - Handle special ritual services
- **Shashwatha Pooja** - Ancestral ritual services tracking
- **In-Kind Donations** - Track material donations
- **Receipt Printing** - Thermal printer support via ESCPOS protocol
- **Dashboard** - View statistics and daily summaries
- **History & Filtering** - Search and filter past transactions
- **PDF & Excel Export** - Export receipts and reports
- **Auto-Update** - Checks GitHub for new versions
- **Automatic Backups** - Daily database backups

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Java 23 |
| UI Framework | JavaFX 23.0.1 (FXML) |
| Database | H2 (in-memory/file) |
| Database Pooling | HikariCP |
| Logging | SLF4J + Logback |
| PDF Generation | Apache PDFBox |
| Excel Export | Apache POI |
| Receipt Printing | ESCPOS-Coffee |
| Build Tool | Gradle |
| Password Hashing | BCrypt |

## Prerequisites

- Java Development Kit (JDK) 23 or higher
- Gradle (wrapper included)

## Build & Run

```bash
# Build the project
./gradlew build

# Run the application
./gradlew run
```

## Project Structure

```
Temple_Software/
├── src/main/java/com/pranav/temple_software/
│   ├── Main.java                    # Application entry point
│   ├── Launcher.java                # JavaFX Application class
│   ├── controllers/                 # UI Controllers
│   │   ├── MainController.java
│   │   ├── LoginController.java
│   │   ├── AdminLoginController.java
│   │   ├── AdminPanelController.java
│   │   └── menuControllers/          # Menu-specific controllers
│   │       ├── SevaManager/
│   │       ├── DonationManager/
│   │       ├── KaryakramaManager/
│   │       ├── ShashwathaPoojaManager/
│   │       ├── VisheshaPoojeManager/
│   │       ├── InKindDonationManager/
│   │       └── History/
│   ├── models/                       # Data models
│   │   ├── Seva.java
│   │   ├── Donations.java
│   │   ├── DevoteeDetails.java
│   │   ├── Karyakrama.java
│   │   ├── ShashwathaPoojaReceipt.java
│   │   └── ...
│   ├── repositories/                # Data access layer
│   │   ├── SevaRepository.java
│   │   ├── DonationRepository.java
│   │   ├── KaryakramaRepository.java
│   │   └── ...
│   ├── services/                    # Business logic
│   │   ├── ReceiptServices.java
│   │   ├── ValidationServices.java
│   │   └── Tables.java
│   └── utils/                        # Utilities
│       ├── DatabaseManager.java
│       ├── ReceiptPrinter.java
│       ├── EscPosPrinterService.java
│       ├── ConfigManager.java
│       └── BackupService.java
├── src/main/resources/
│   ├── fxml/                         # JavaFX UI definitions
│   ├── css/                          # Stylesheets
│   ├── images/                       # Icons and images
│   └── fonts/                        # Font files
├── data/                             # Data directory
└── build/                            # Build output
```

## Configuration

### Temple Details

Edit `src/main/resources/config.properties` to customize temple information:

```properties
temple.name=ಶ್ರೀ ಶಾಸ್ತಾರ ಸುಬ್ರಹ್ಮಣ್ಯೇಶ್ವರ ದೇವಸ್ಥಾನ
temple.location=ಚೇರ್ಕಬೆ
temple.postal=ಅಂಚೆ : ಪಡ್ರೆ 671552
temple.phone=6282525216, 9526431593
```

### Database Configuration

Edit `src/main/resources/application.properties` to configure database:

```properties
spring.datasource.url=jdbc:h2:file:./data/temple_db
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
```

## Database Tables

The application manages the following core tables:
- **Sevas** - Available ritual services
- **VishesjaPooje** - Special poojas
- **Donations** - Donation types
- **Receipts** - Seva receipts
- **Receipt_Items** - Individual items on receipts
- **DonationReceipts** - Donation receipts
- **Karyakramagalu** - Event/program types
- **KaryakramaReceipts** - Event receipts
- **ShashwathaPoojaReceipts** - Ancestral ritual receipts
- **InKindDonations** - Material donations
- **Credentials** - Password storage (BCrypt hashed)

## License

Internal use only - Cherkabe Temple.
