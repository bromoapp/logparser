CREATE TABLE IF NOT EXISTS `tbl_logs` (
	`time` DATETIME NULL DEFAULT NULL,
	`ip` VARCHAR(50) NULL DEFAULT NULL,
	`method` VARCHAR(50) NULL DEFAULT NULL,
	`response` VARCHAR(50) NULL DEFAULT NULL,
	`client` VARCHAR(255) NULL DEFAULT NULL
)
ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `tbl_blocked` (
	`time` DATETIME NULL DEFAULT NULL,
	`ip` VARCHAR(50) NULL DEFAULT NULL,
	`hits` INT(11) NULL DEFAULT NULL,
	`threshold` INT(11) NULL DEFAULT NULL,
	`duration` VARCHAR(50) NULL DEFAULT NULL
)
ENGINE=InnoDB;

CREATE PROCEDURE IF NOT EXISTS `daily_passed_threshold_ips`(IN `in_date` VARCHAR(50), IN `in_threshold` INT)
BEGIN
	SELECT src.ip, src.hits FROM (
		SELECT DISTINCT(a.ip) AS 'ip', COUNT(*) AS 'hits' FROM tbl_logs AS a 
		WHERE DATE(a.`time`) = in_date GROUP BY a.ip
	) AS src WHERE src.hits >= in_threshold;
END;

CREATE PROCEDURE IF NOT EXISTS `hourly_passed_threshold_ips`(IN `in_time` VARCHAR(50), IN `in_threshold` INT)
BEGIN
	SELECT src.ip, src.hits FROM (
		SELECT DISTINCT(a.ip) AS 'ip', COUNT(*) AS 'hits' FROM tbl_logs AS a 
		WHERE a.`time` >= in_time AND a.`time` <= DATE_ADD(in_time, INTERVAL 1 HOUR) 
		GROUP BY a.ip
	) AS src WHERE src.hits >= in_threshold;
END;
