# Update-100k-csv-lines-into-10M-records-database-table
Update and insert 100 thousand CSV lines to 10 million records table of MS SQL Server using Spring Boot and MyBatis in a few seconds.

This project was generated with [Spring Initializr](https://start.spring.io/) version **2.6.5**, Java **11**

## Context
In my application, there is a `CUSTOMER` table containing more than 10 million records. And daily, another application will export CRM data to a CSV file and upload it to an S3 bucket. This CSV file usually contains about 30 thousand lines. In there, more than 1 thousand lines are new customers and the remaining are existing customers in the `CUSTOMER` table need to be updated their information.
1 cron job reads the CSV file and updates/inserts it to the `CUSTOMER` table. The issue here is if we update each of nearly 30k existing records into the `CUSTOMER` table with 10M records existing in there because it will take a few hours to complete.
After googling for some solutions, I got a suggestion with the concept **INSERT ON DUPLICATE KEY UPDATE**. But this solution is usually only used in MySQL or PostgreSQL and not supported by MS SQL Server, instead of this, MS SQL Server has another solution to resolve this issue, which is the **MERGE** concept. Then, I have applied to my application then I take notes in a short demo here to reuse later and share someone finding solutions to resolve similar issues.

## Idea
1. Just insert all CSV lines to a temporary table `CUSTOMER_TEMP` by using the MyBatis batch insert.
2. Call a SQL script by MyBatis ScriptRunner to merge data from the `CUSTOMER_TEMP` to the `CUSTOMER` table by the internal database engine.

For demo purposes, I'm using spring-boot-start-batch, but for demo purposes, I only write a short Java class to read the CSV file `customers_100k_lines.csv` from the current project folder instead of the S3 bucket, then batch insert to `CUSTOMER_TEMP` and call ScriptRunner to merge the data to the `CUSTOMER` table. The CSV file contains 50k lines are existing records in the CUSTOMER table should be updated and 500k are new ones, should be inserted. I'm also prepared 2 SQL scripts to create the `CUSTOMER_TEMP` and `CUSTOMER` table with 10M records existing. If you want to run this source code, please correct the database information in the application.yml file, then you can check the total time spent, and the time spent for batch insert and data merging in log files. Hope this is helpful for someone. My personal blog: https://koacervate.blogspot.com/2022/03/how-to-update-and-insert-100k-csv-lines.html

    Total records in CUSTOMER table before: 10000000
    Total CSV lines: 100000. Total inserted to CUSTOMER_TEMP table: 100000 by 10 batchs. Time spent: 15589 (ms).
    Time spent to merge data from CUSTOMER_TEMP to CUSTOMER table: 1325 (ms).
    Total records in CUSTOMER table after: 10100000

References:
https://ichihedge.wordpress.com/2020/01/12/mybatis-save-or-update/
https://www.sqlshack.com/understanding-the-sql-merge-statement/
https://docs.microsoft.com/en-us/sql/t-sql/statements/merge-transact-sql?view=sql-server-ver15
