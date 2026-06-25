# NotionFuse 🚀

NotionFuse is a powerful backend engine designed to automate the synchronization and merging of data between multiple Notion databases. It allows you to unify disparate data sources into a single, organized Notion workspace with automatic, real-time syncing.

## 🌟 Key Features
* **Database Synchronization:** Seamlessly merge records from two different Notion databases.
* **Real-time Sync:** Automatically detects changes in source databases and updates the merged database.
* **Custom Mapping:** Configure how properties (columns) from different sources map to your master database.
* **Secure Authentication:** Built with Spring Security and OAuth 2.0 integration for Notion.

## 👥 Beta Testing & User Management
I manage a dedicated Discord community for early adopters to handle onboarding, triage bug reports, and prioritize feature requests. Maintaining this direct line of communication allows for rapid iteration and ensures the development roadmap aligns with actual user needs.

Early testers receive a 15% lifetime discount upon the Pro release.
[💬 Join the Beta Discord Server Here](https://discord.gg/pvuJvFzwfW)

## 🛠 Prerequisites
Before you begin, ensure you have the following installed:
* [Docker](https://www.docker.com/products/docker-desktop/) and [Docker Compose](https://docs.docker.com/compose/).
* [Java 17](https://www.oracle.com/java/technologies/downloads/) or higher.
* [Git](https://git-scm.com/).

## 🚀 Getting Started

### 1. Clone the repository
```bash
git clone https://github.com/mhamadghourani/notionFuse-backend.git
```

## ⚙️ Configuration Details

This project requires specific environment variables to manage database connectivity, OAuth flow, and external integrations.

### Database Configuration
* **SPRING_DATASOURCE_URL**: The JDBC connection string for your PostgreSQL database.
    * `sslmode=require`: Ensures the connection to your database (e.g., Supabase) is encrypted.
    * `prepareThreshold=0`: Optimizes performance for drivers that perform statement preparation.
* **SPRING_DATASOURCE_USERNAME**: The username for accessing your PostgreSQL database.
* **SPRING_DATASOURCE_PASSWORD**: The password for accessing your PostgreSQL database.

### Notion OAuth Configuration
These variables allow your application to perform secure, server-to-server communication with Notion on behalf of your users.
* **NOTION_CLIENT_ID**: The unique ID provided by the Notion Developer Portal for your integration.
* **NOTION_CLIENT_SECRET**: The secret key associated with your Notion integration.
* **NOTION_REDIRECT_URI**: The callback URL where Notion sends the user after they authorize your application.

### Security & Notifications
* **JWT_SECRET_KEY**: A cryptographically strong, random string used to sign and verify JSON Web Tokens (JWT) for secure user sessions.
* **BREVO_API_KEY**: Your API key for Brevo, used to handle automated email notifications and service alerts from your backend.
# 3. Build and Run
Use Docker Compose to build and start the application:
```
docker compose up -d --build
```
# 4. Verify the Connection
Check that the backend service is running:
```
docker compose logs -f
```
## 📝 License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
