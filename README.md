# Log Parser
*NOTE: This project is created in Windows 10 environment.*
## 1. Prerequisites
- Already have MySql server instance running in your local PC/Laptop,
- Already have MySql desktop client application e.g. HeidiSQL installed (optional),
- Already have the Eclipse IDE installed

## 2. How to Create a Runnable JAR
- Clone this repo to your local,
- Import project to **Eclipse** as **Maven** project,
- Create a new database called **"wallethub"** inside your local MySql,
- Open **"db.properties"** file, which located in **src/main/resources** folder,
- Edit **Username** and **Password** values in that properties file according to your MySql login credential,
- From project root folder, execute maven command:
	> mvn clean install
- Open **target** folder, and you will get a jar named: **parser-0.0.1-jar-with-dependencies.jar**.

## 3. To Load the sample Log file to DB

The sample log file is located in **src/test/resources** folder, called: **access.log**. To load the content of this file to the database, we can execute below command from within **target** folder.
> java -cp "parser-0.0.1-jar-with-dependencies.jar" com.ef.Parser --accesslog=../src/test/resources/access.log

After executing the above command, use your MySql desktop client application (I use **HeidiSQL**), and see that all records in the sample log file already loaded into **tbl_logs** table.

## 4. To Block IPs that exceed daily Threshold

The file explorer is accessible using the button in left corner of the navigation bar. You can create a new file by clicking the **New file** button in the file explorer. You can also create folders by clicking the **New folder** button.