-- Updates Liquid Site Tables to version 0.9 (from 0.8.x)

-- Add public flag for groups
ALTER TABLE `LS_GROUP`
    ADD COLUMN `PUBLIC` SMALLINT DEFAULT 0 NOT NULL;

-- Change content data length to 2^24 characters
ALTER TABLE `LS_ATTRIBUTE`
    MODIFY COLUMN `DATA` MEDIUMTEXT NOT NULL;
