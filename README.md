## Setup Application
###
Clone repository
```
git clone https://sunghangga@bitbucket.org/sunghangga/gtfs-auto-import.git
```
###
Install Depedency using Maven. (Make sure [Maven](https://maven.apache.org/install.html) already installed on your machine before running this syntax).
```
mvn clean install
```
###
Make a postgreSQL database on your machine and import [gtfs_v1.0.0.sql](https://maestronicoperationsltd-my.sharepoint.com/:f:/g/personal/angga_putra_maes-electronic_co_id/Eng41HxYYAVFrI5Giz13bCwB24bDxE5CnJNqlDOZUazTDQ?e=RhL27U) file to your database.
###
Setting your application.properties and running app with IDE Eclipse or Intellij

## Running Docker
###
Setup postgreSQL database on your machine
###
Setup docker on your machine
###
Setting Dockerfile or docker-compose.yml based on your project
###
Run docker compose
```
docker compose up --build -d
```