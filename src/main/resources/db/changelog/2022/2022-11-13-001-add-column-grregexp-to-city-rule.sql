--liquibase formatted sql

--changeset kuchumov:2022-11-13-001-add-column-grregexp-to-city-rule
ALTER TABLE `geolocation`.`city_rule`
ADD COLUMN `grregexp` VARCHAR(1023) NULL AFTER `rule`;
