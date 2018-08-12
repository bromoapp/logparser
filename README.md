# Log Parser
*NOTE: This project is created in Windows 10 environment.*
## 1. Prerequisites
- Already have MySql server instance running in your local PC/Laptop,
- Already have the Eclipse IDE installed
## 2. How to Create a Runnable JAR
- Clone this repo to your local,
- Import project to **Eclipse** as **Maven** project,
- Create a new database called **"wallethub"** inside your local MySql,
- Open **"db.properties"** file, which located in **src/main/resources** folder,
- Edit **Username** and **Password** values in that properties file according to your MySql login credential,
- Execute maven command:
	> mvn clean install
- Open **target** folder, and you will get a jar named: **parser-0.0.1-jar-with-dependencies.jar**.

## 3. How to Load the sample Log file to DB

The sample log file is located in **src/test/resources** folder, called: **access.log**. To load the content of this file is by executing below command, from within **target** folder.
> java -cp "parser-0.0.1-jar-with-dependencies.jar" com.ef.Parser --accesslog=../src/test/resources/access.log

After executing the above command, you can open your MySql desktop client application (I use **HeidiSQL**), and see that all records in the sample log file already loaded into **tbl_logs** table.