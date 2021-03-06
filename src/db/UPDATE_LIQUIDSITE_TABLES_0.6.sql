-- Updates Liquid Site Tables to version 0.6 (from 0.5)

-- Remove the ContentPage SECTION attribute
DELETE FROM `LS_ATTRIBUTE` WHERE `NAME` = 'SECTION';

-- Add STATUS column to LS_CONTENT
ALTER TABLE `LS_CONTENT`
    ADD COLUMN `STATUS` SMALLINT NOT NULL;
UPDATE `LS_CONTENT` SET `STATUS` = 0;
