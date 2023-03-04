--liquibase formatted sql

--changeset kuchumov:2022-11-02-001-create-tables-geo-request
CREATE TABLE `geolocation`.`geo_request` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `api_key` VARCHAR(255) NULL,
  `address` VARCHAR(1023) NULL,
  `date_time` DATETIME NULL,
  `latitude` DOUBLE NULL,
  `longitude` DOUBLE NULL,
  `answer` LONGTEXT NULL,
  PRIMARY KEY (`id`)
);

