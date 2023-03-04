--liquibase formatted sql

--changeset kuchumov:2022-11-08-001-create-indexes
ALTER TABLE `geolocation`.`city_rule` ADD INDEX `idx_rule` (`rule` ASC);
ALTER TABLE `geolocation`.`shop_point` ADD INDEX `city_idx` (`city` ASC);
ALTER TABLE `geolocation`.`shop_address` ADD INDEX `address_idx` (`address` ASC);
