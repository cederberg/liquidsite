-- Updates Liquid Site Tables to version 0.10 (from 0.9)

-- Create new domain attribute table
CREATE TABLE `LS_DOMAIN_ATTRIBUTE` (
    `DOMAIN` VARCHAR(30) NOT NULL,
    `NAME` VARCHAR(200) NOT NULL,
    `DATA` MEDIUMTEXT NOT NULL,
    PRIMARY KEY (`DOMAIN`, `NAME`)
);

-- Remove unused user preference table
DROP TABLE `LS_PREFERENCE`;
