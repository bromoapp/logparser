
# Log Parser
*NOTE: This project is created in Windows 10 environment.*

## Prerequisites
- Already have MySql server instance running in your local PC/Laptop,
- Already have MySql desktop client application e.g. HeidiSQL installed (optional),
- Already have the Eclipse IDE installed

## How to Create a Runnable JAR
- Clone this repo to your local,
- Import project to **Eclipse** as **Maven** project,
- Create a new database called **"wallethub"** inside your local MySql,
- Open **"db.properties"** file, which located in **src/main/resources** folder,
- Edit **Username** and **Password** values in that properties file according to your MySql login credential,
- From project root folder, execute maven command:
	> mvn clean install
- Open **target** folder, and you will get a jar named: **parser-0.0.1-jar-with-dependencies.jar**.

## To Load the sample Log file to DB

**Syntax** for loading **.log** file to database:
> java -cp "parser-x.x.x-jar-with-dependencies.jar" com.ef.Parser --accesslog=[/path/to/file/filename.log]

To use the above syntax against our sample log file, which is located in **src/test/resources** folder.  We can execute command below from within **target** folder.
> java -cp "parser-0.0.1-jar-with-dependencies.jar" com.ef.Parser --accesslog=../src/test/resources/access.log

After executing the above command, use your MySql desktop client application (e.g.  **HeidiSQL**), and see that all records in the sample log file already loaded into **tbl_logs** table.

## To Block IPs that exceed daily Threshold

Syntax for blocking IPs that exceed daily threshold:
> java -cp "parser-0.0.1-jar-with-dependencies.jar" com.ef.Parser --startDate=[yyyy-MM-dd.HH:mm:ss] --duration=[daily|hourly] --threshold=[integer_value]

To use the above syntax against loaded sample data in database, we can execute below command from within **target** folder.
> java -cp "parser-0.0.1-jar-with-dependencies.jar" com.ef.Parser --startDate=2017-01-01.15:00:00 --duration=daily --threshold=500

After executing the above command, use your MySql desktop client application (e.g. **HeidiSQL**), and see that all blocked IPs that exceed daily threshold (500) are already inserted into **tbl_blocked** table.
