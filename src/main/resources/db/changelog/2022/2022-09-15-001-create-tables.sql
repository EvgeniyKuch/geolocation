--liquibase formatted sql

--changeset kuchumov:2022-09-15-001-create-tables
CREATE TABLE `city_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `city` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `rule` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `incoming_mail_setting` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `host` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `password` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `port` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `user` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `shop_address` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `shop_point` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address_for_system` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `city` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `work_time` varchar(1023) COLLATE utf8_bin DEFAULT NULL,
  `work_time_raw` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `shop_address_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `shop_address_idx` (`shop_address_id`),
  CONSTRAINT `shop_address_idx` FOREIGN KEY (`shop_address_id`) REFERENCES `shop_address` (`id`)
);
