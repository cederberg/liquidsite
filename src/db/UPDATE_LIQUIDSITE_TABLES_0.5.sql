-- Updates Liquid Site Tables to version 0.5 (from 0.4)

-- Update content category numbers
UPDATE `LS_CONTENT` SET `CATEGORY` = 6 WHERE `CATEGORY` = 5;
UPDATE `LS_CONTENT` SET `CATEGORY` = 5 WHERE `CATEGORY` = 4;
UPDATE `LS_CONTENT` SET `CATEGORY` = 4 WHERE `CATEGORY` = 3;
UPDATE `LS_CONTENT` SET `CATEGORY` = 3 WHERE `CATEGORY` = 2;