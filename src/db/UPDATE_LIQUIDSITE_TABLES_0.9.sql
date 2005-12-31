-- Updates Liquid Site Tables to version 0.9 (from 0.8.x)

-- Change domain length to 30 characters
ALTER TABLE `LS_ATTRIBUTE`
    MODIFY COLUMN `DATA` MEDIUMTEXT NOT NULL;
