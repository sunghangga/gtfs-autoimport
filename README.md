## Setup Application

Clone repository.
```
git clone https://sunghangga@bitbucket.org/maestronic/gtfs-auto-import.git
```
Install Depedency using Maven. (Make sure [Maven](https://maven.apache.org/install.html) already installed on your machine before running this syntax).
```
mvn clean install
```
Make a postgreSQL database on your machine and import the newest GTFS database file.

Setting your application.properties and running app with IDE Eclipse or Intellij.

## Running Docker

Setup postgreSQL database on your machine.

Setup docker on your machine.

Setting Dockerfile or docker-compose.yml based on your project.

Start gtfs-app docker application before start auto import application.

Run auto import application using docker compose.
```
docker-compose --env-file .via.env up --build -d
```
To enable scrapping mode, give "scrapping" value in variable "IMPORT_MODE" and change "GTFS_SOURCE_URL" into transitfeed website.
```
IMPORT_MODE=scrapping
GTFS_SOURCE_URL=https://transitfeeds.com/p/via-metropolitan-transit/62 //this URL will be direct to page of GTFS list data source VIA
```
To disable srapping mode, give empty string in "IMPORT_MODE" variable and change "GTFS_SOURCE_URL" into direct zip file URL.
```
IMPORT_MODE=
GTFS_SOURCE_URL=http://www.viainfo.net/BusService/google_transit.zip //this URL will be direct to zip file VIA
```