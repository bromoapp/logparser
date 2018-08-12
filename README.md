

# Log Parser
*NOTE: This project is created and tested in Windows 10 environment.*

## Using this App

### 1. Prerequisites
- Already have MySql server instance running in your local PC/Laptop,
- Already have MySql desktop client application e.g. HeidiSQL installed (optional),
- Already have the Eclipse IDE installed

### 2. How to Create a Runnable JAR
- Clone this repo to your local,
- Import project to **Eclipse** as **Maven** project,
- Create a new database called **"wallethub"** inside your local MySql,
- Open **"db.properties"** file, which located in **src/main/resources** folder,
- Edit **Username** and **Password** values in that properties file according to your MySql login credential,
- From project root folder, execute maven command:
	> mvn clean install
- Open **target** folder, and you will get a jar named: **parser-0.0.1-jar-with-dependencies.jar**.

### 3. To Load the sample Log file to DB

**Syntax** for loading **.log** file to database:
> java -cp "parser-x.x.x-jar-with-dependencies.jar" com.ef.Parser --accesslog=[/path/to/file/filename.log]

To use the above syntax against our sample log file, which is located in **src/test/resources** folder.  We can execute command below from within **target** folder.
> java -cp "parser-0.0.1-jar-with-dependencies.jar" com.ef.Parser --accesslog=../src/test/resources/access.log

After executing the above command, use your MySql desktop client application (e.g.  **HeidiSQL**), and check that all records from the **access.log** file already loaded into **tbl_logs** table (116,484 records).

### 4. To Block IPs that exceed Daily threshold

Syntax for blocking IPs that exceed daily threshold:
> java -cp "parser-0.0.1-jar-with-dependencies.jar" com.ef.Parser --startDate=[yyyy-MM-dd.HH:mm:ss] --duration=[daily|hourly] --threshold=[integer_value]

To use the above syntax against loaded sample data in database, we can execute below command from within **target** folder.
> java -cp "parser-0.0.1-jar-with-dependencies.jar" com.ef.Parser --startDate=2017-01-01.15:00:00 --duration=daily --threshold=500

After executing the above command, use your MySql desktop client application (e.g. **HeidiSQL**), and check that all blocked IPs that exceed **daily threshold** (500) are already inserted into **tbl_blocked** table (15 records).

### 5. To Block IPs that exceed Hourly threshold

Syntax for blocking IPs that exceed daily threshold:
> java -cp "parser-0.0.1-jar-with-dependencies.jar" com.ef.Parser --startDate=[yyyy-MM-dd.HH:mm:ss] --duration=[daily|hourly] --threshold=[integer_value]

To use the above syntax against loaded sample data in database, we can execute below command from within **target** folder.
> java -cp "parser-0.0.1-jar-with-dependencies.jar" com.ef.Parser --startDate=2017-01-01.15:00:00 --duration=hourly --threshold=200

After executing the above command, use your MySql desktop client application (e.g. **HeidiSQL**), and check that all blocked IPs that exceed **hourly threshold** (200) are already inserted into **tbl_blocked** table (+2 records).

## DB Schema
When this application runs, this application will execute a db schema file which located in **/src/main/resources** folder, so all necessary tables and store procedures will be created. The content of this **schema.sql** file is like below.

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

