-- Updates Liquid Site Tables to version 0.6 (from 0.5)

-- Remove the ContentPage SECTION attribute
DELETE FROM `LS_ATTRIBUTE` WHERE `NAME` = 'SECTION';
