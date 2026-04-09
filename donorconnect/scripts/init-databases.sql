-- DonorConnect: Initialize all service databases
CREATE DATABASE IF NOT EXISTS auth_db;
CREATE DATABASE IF NOT EXISTS config_db;
CREATE DATABASE IF NOT EXISTS donor_db;
CREATE DATABASE IF NOT EXISTS blood_db;
CREATE DATABASE IF NOT EXISTS transfusion_db;
CREATE DATABASE IF NOT EXISTS safety_db;
CREATE DATABASE IF NOT EXISTS billing_db;
CREATE DATABASE IF NOT EXISTS reporting_db;
CREATE DATABASE IF NOT EXISTS notification_db;

-- Grant privileges
GRANT ALL PRIVILEGES ON auth_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON config_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON donor_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON blood_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON transfusion_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON safety_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON billing_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON reporting_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON notification_db.* TO 'root'@'%';
FLUSH PRIVILEGES;
