ALTER DATABASE cloudbrain CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cloudbrain;
ALTER TABLE doctor CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE patient CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE registration CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE medical_record CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
