Student-attendance-management-system
Tech Stack
Java (Swing)
Maven
Run
mvn clean install run Main.java


               STUDENT NAME AND ID
NAME:                                                              ID:
1. Seid Mohammed                                               ----1714/16

    







## Setup Guide: 
Step 1 — Install Java JDK 17+
Go to https://adoptium.net......java -version
Step 2 — Install MySQL 8.0+
Go to https://dev.mysql.com/downloads/installer/  ....  mysql --version
Step 3 — Install Apache Maven
Go to https://maven.apache.org/download.cgi
..Environment Variables ... mvn -version

Open CMD
Navigate to the project:
cd C:\path\to\StudentAttendanceSystem
Build:
mvn clean package -DskipTests
Wait for BUILD SUCCESS — this downloads dependencies (~80MB first time)
... or
cd "c:\Users\SEID\OneDrive\Desktop\atte.java\StudentAttendanceSystem" ; C:\apache-maven-3.9.15\bin\mvn.cmd clean package -DskipTests 2>&1 | Select-String "BUILD|ERROR"

step 7 — Run the Application: java -jar target\StudentAttendanceSystem-1.0.0-jar-with-dependencies.jar


... If the target folder with the JAR already exists, just run:

java -jar target\StudentAttendanceSystem-1.0.0
 

## the setup order to mark attendance:
1.Admin → Departments → add one
2.Admin → Courses → add one (linked to dept)
3.Admin → Classes → add one (linked to course)
4.Admin → Subjects → add one (linked to course)
5.Admin → Assign Teachers → link class + subject + your teacher
6.Admin → Students → create a student
7.Admin → Enrollments → select the class, enroll the student
7.Teacher login → Mark Attendance → select class-subject → Load Students → mark → Save






// Admin

--- admin@attendance.com
--- admin123