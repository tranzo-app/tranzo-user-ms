# Production deployment (AWS)

Use profile **prod** so the app uses **PostgreSQL** (e.g. Amazon RDS). Data is stored in RDS and is **not lost** when the application stops or is redeployed.

## 1. Create PostgreSQL database on AWS

- **Amazon RDS**: Create a PostgreSQL DB (e.g. 15 or 16).
- Note: **Endpoint**, **Port** (usually 5432), **Database name**, **Master username**, **Master password**.
- Ensure the DB is in the same VPC as your app (or that security groups allow the app to reach RDS on port 5432).

## 2. Set environment variables

Set these where your app runs (ECS task definition, Elastic Beanstalk, Lambda, or EC2):

| Variable | Description | Example |
|----------|-------------|--------|
| `SPRING_PROFILES_ACTIVE` | Must be `prod` | `prod` |
| `SPRING_DATASOURCE_URL` | JDBC URL for PostgreSQL | `jdbc:postgresql://your-rds-endpoint.region.rds.amazonaws.com:5432/tranzo_db` |
| `SPRING_DATASOURCE_USERNAME` | DB username | RDS master username |
| `SPRING_DATASOURCE_PASSWORD` | DB password | RDS master password |
| `SPRING_JWT_SECRET` | Secret for JWT signing (use a strong value) | e.g. from AWS Secrets Manager |
| `AWS_S3_MEDIA_BUCKET` | S3 bucket for media | Your bucket name |
| `AWS_REGION` | AWS region (optional, default `ap-south-1`) | `ap-south-1` |

Optional JWT tuning: `JWT_ACCESS_EXPIRY_MINUTES`, `JWT_REFRESH_EXPIRY_DAYS`, `JWT_REGISTRATION_EXPIRY_MINUTES`.

## 3. Run the application with prod profile

- **Start command**: ensure the JVM sees `SPRING_PROFILES_ACTIVE=prod` (and the variables above).
- Example: `java -Dspring.profiles.active=prod -jar tranzo-user-ms.jar`
- Or set `SPRING_PROFILES_ACTIVE=prod` in the environment.

On first run, **Flyway** will apply `V1__create_all_tables.sql` and create all tables in RDS. On later restarts, Flyway skips already-applied migrations; **data in RDS persists** when the app stops.

## 4. Security

- Do **not** commit real `SPRING_DATASOURCE_*` or `SPRING_JWT_SECRET` values.
- Prefer **AWS Secrets Manager** or **Parameter Store** and inject them into the task/instance environment at runtime.
- Keep RDS in a private subnet; allow access only from the app’s security group.
