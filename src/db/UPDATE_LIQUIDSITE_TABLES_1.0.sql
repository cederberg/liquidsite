-- Updates Liquid Site Tables to version 1.0 (from 0.10.1)

-- Change all text columns to binary
ALTER TABLE `LS_CONFIGURATION`
    MODIFY `NAME` VARBINARY(50) NOT NULL,
    MODIFY `VALUE` VARBINARY(200) NOT NULL;
ALTER TABLE `LS_DOMAIN`
    MODIFY `NAME` VARBINARY(30) NOT NULL,
    MODIFY `DESCRIPTION` VARBINARY(100) NOT NULL;
ALTER TABLE `LS_DOMAIN_ATTRIBUTE`
    MODIFY `DOMAIN` VARBINARY(30) NOT NULL,
    MODIFY `NAME` VARBINARY(200) NOT NULL,
    MODIFY `DATA` MEDIUMBLOB NOT NULL;
ALTER TABLE `LS_USER`
    MODIFY `DOMAIN` VARBINARY(30) NOT NULL,
    MODIFY `NAME` VARBINARY(30) NOT NULL,
    MODIFY `PASSWORD` VARBINARY(30) NOT NULL,
    MODIFY `REAL_NAME` VARBINARY(100) NOT NULL,
    MODIFY `EMAIL` VARBINARY(100) NOT NULL,
    MODIFY `COMMENT` VARBINARY(200) NOT NULL;
ALTER TABLE `LS_GROUP`
    MODIFY `DOMAIN` VARBINARY(30) NOT NULL,
    MODIFY `NAME` VARBINARY(30) NOT NULL,
    MODIFY `DESCRIPTION` VARBINARY(100) NOT NULL,
    MODIFY `COMMENT` VARBINARY(200) NOT NULL;
ALTER TABLE `LS_USER_GROUP`
    MODIFY `DOMAIN` VARBINARY(30) NOT NULL,
    MODIFY `USER` VARBINARY(30) NOT NULL,
    MODIFY `GROUP` VARBINARY(30) NOT NULL;
ALTER TABLE `LS_CONTENT`
    MODIFY `DOMAIN` VARBINARY(30) NOT NULL,
    MODIFY `NAME` VARBINARY(200) NOT NULL,
    MODIFY `AUTHOR` VARBINARY(30) NOT NULL,
    MODIFY `COMMENT` VARBINARY(200) NOT NULL;
ALTER TABLE `LS_ATTRIBUTE`
    MODIFY `DOMAIN` VARBINARY(30) NOT NULL,
    MODIFY `NAME` VARBINARY(200) NOT NULL,
    MODIFY `DATA` MEDIUMBLOB NOT NULL;
ALTER TABLE `LS_PERMISSION`
    MODIFY `DOMAIN` VARBINARY(30) NOT NULL,
    MODIFY `USER` VARBINARY(30) NOT NULL,
    MODIFY `GROUP` VARBINARY(30) NOT NULL;
ALTER TABLE `LS_LOCK`
    MODIFY `DOMAIN` VARBINARY(30) NOT NULL,
    MODIFY `USER` VARBINARY(30) NOT NULL;

-- Set correct UTF-8 character set for all text columns
ALTER TABLE `LS_CONFIGURATION`
    DEFAULT CHARACTER SET utf8,
    MODIFY `NAME` VARCHAR(50) CHARACTER SET utf8 NOT NULL,
    MODIFY `VALUE` VARCHAR(200) CHARACTER SET utf8 NOT NULL;
ALTER TABLE `LS_DOMAIN`
    DEFAULT CHARACTER SET utf8,
    MODIFY `NAME` VARCHAR(30) CHARACTER SET utf8 NOT NULL,
    MODIFY `DESCRIPTION` VARCHAR(100) CHARACTER SET utf8 NOT NULL;
ALTER TABLE `LS_DOMAIN_ATTRIBUTE`
    DEFAULT CHARACTER SET utf8,
    MODIFY `DOMAIN` VARCHAR(30) CHARACTER SET utf8 NOT NULL,
    MODIFY `NAME` VARCHAR(200) CHARACTER SET utf8 NOT NULL,
    MODIFY `DATA` MEDIUMTEXT CHARACTER SET utf8 NOT NULL;
ALTER TABLE `LS_USER`
    DEFAULT CHARACTER SET utf8,
    MODIFY `DOMAIN` VARCHAR(30) CHARACTER SET utf8 NOT NULL,
    MODIFY `NAME` VARCHAR(30) CHARACTER SET utf8 NOT NULL,
    MODIFY `PASSWORD` VARCHAR(30) CHARACTER SET utf8 NOT NULL,
    MODIFY `REAL_NAME` VARCHAR(100) CHARACTER SET utf8 NOT NULL,
    MODIFY `EMAIL` VARCHAR(100) CHARACTER SET utf8 NOT NULL,
    MODIFY `COMMENT` VARCHAR(200) CHARACTER SET utf8 NOT NULL;
ALTER TABLE `LS_GROUP`
    DEFAULT CHARACTER SET utf8,
    MODIFY `DOMAIN` VARCHAR(30) CHARACTER SET utf8 NOT NULL,
    MODIFY `NAME` VARCHAR(30) CHARACTER SET utf8 NOT NULL,
    MODIFY `DESCRIPTION` VARCHAR(100) CHARACTER SET utf8 NOT NULL,
    MODIFY `COMMENT` VARCHAR(200) CHARACTER SET utf8 NOT NULL;
ALTER TABLE `LS_USER_GROUP`
    DEFAULT CHARACTER SET utf8,
    MODIFY `DOMAIN` VARCHAR(30) CHARACTER SET utf8 NOT NULL,
    MODIFY `USER` VARCHAR(30) CHARACTER SET utf8 NOT NULL,
    MODIFY `GROUP` VARCHAR(30) CHARACTER SET utf8 NOT NULL;
ALTER TABLE `LS_CONTENT`
    DEFAULT CHARACTER SET utf8,
    MODIFY `DOMAIN` VARCHAR(30) CHARACTER SET utf8 NOT NULL,
    MODIFY `NAME` VARCHAR(200) CHARACTER SET utf8 NOT NULL,
    MODIFY `AUTHOR` VARCHAR(30) CHARACTER SET utf8 NOT NULL,
    MODIFY `COMMENT` VARCHAR(200) CHARACTER SET utf8 NOT NULL;
ALTER TABLE `LS_ATTRIBUTE`
    DEFAULT CHARACTER SET utf8,
    MODIFY `DOMAIN` VARCHAR(30) CHARACTER SET utf8 NOT NULL,
    MODIFY `NAME` VARCHAR(200) CHARACTER SET utf8 NOT NULL,
    MODIFY `DATA` MEDIUMTEXT CHARACTER SET utf8 NOT NULL;
ALTER TABLE `LS_PERMISSION`
    DEFAULT CHARACTER SET utf8,
    MODIFY `DOMAIN` VARCHAR(30) CHARACTER SET utf8 NOT NULL,
    MODIFY `USER` VARCHAR(30) CHARACTER SET utf8 NOT NULL,
    MODIFY `GROUP` VARCHAR(30) CHARACTER SET utf8 NOT NULL;
ALTER TABLE `LS_LOCK`
    DEFAULT CHARACTER SET utf8,
    MODIFY `DOMAIN` VARCHAR(30) CHARACTER SET utf8 NOT NULL,
    MODIFY `USER` VARCHAR(30) CHARACTER SET utf8 NOT NULL;

-- Set correct UTF-8 character set for database
ALTER DATABASE DEFAULT CHARACTER SET utf8;

-- Change content online and offline to allow NULL values
ALTER TABLE `LS_CONTENT`
    MODIFY `ONLINE` DATETIME NULL,
    MODIFY `OFFLINE` DATETIME NULL;
UPDATE `LS_CONTENT`
    SET `ONLINE` = NULL
    WHERE `ONLINE` = '1970-01-01 00:00:00';
UPDATE `LS_CONTENT`
    SET `OFFLINE` = NULL
    WHERE `OFFLINE` = '1970-01-01 00:00:00';
