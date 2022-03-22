package com.maestronic.autoimportgtfs.service;

import com.maestronic.autoimportgtfs.entity.Dataset;
import com.maestronic.autoimportgtfs.entity.Import;
import com.maestronic.autoimportgtfs.repository.ImportRepository;
import com.maestronic.autoimportgtfs.util.File;
import com.maestronic.autoimportgtfs.util.GlobalVariable;
import com.maestronic.autoimportgtfs.util.Logger;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class ImportGtfsService implements GlobalVariable {

    @Autowired
    private ImportRepository importRepository;
    @Value("${gtfs.source.url}")
    private String gtfsSourceUrl;
    @Value("${gtfs.download.dir}")
    private String downloadDir;
    @Value("${gtfs.import.url}")
    private String importGtfsUrl;

    public void runAutoImport() {
        try {
            String filePath = null;
            // Check if new dataset exists
            Logger.info("Checking for new GTFS datasets...");
            Dataset dataset = this.getNewGtfsDataset();
            // If gtfs file exists then download it
            if (dataset != null) {
                Logger.info("Downloading new GTFS datasets...");
                filePath = this.downloadFileFromUrl(dataset);
            }
            // If filePath is not null then import it
            if (filePath != null) {
                Logger.info("Importing new GTFS datasets...");
                this.importDataset(filePath, dataset);
            }
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
    }

    public Dataset getNewGtfsDataset() {
        try {
            Document document = Jsoup.connect(gtfsSourceUrl).get();
            Elements elements = document.getElementsByTag("tbody").select("tr").get(0).getElementsByTag("a");
            Element link = elements.get(elements.size() - 1);

            // Get release date
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
            Date parsedDate = dateFormat.parse(elements.get(0).text());
            LocalDateTime releaseDateTime = parsedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

            // Get last import
            List<Import> importList = importRepository.findImportDataByFileType(GTFS_FILE_TYPE,
                    PageRequest.of(0,1));
            // Check if import data exists
            if (importList.size() == 0) {
                Logger.info("New GTFS dataset found!");
                return new Dataset(GTFS_DEFAULT_NAME, link.absUrl("href"), releaseDateTime);
            }
            // Check if release date is null
            // Check the date from website is latter than the last import or import status is failed
            if (importList.get(0).getReleaseDate() == null || (importList.get(0).getReleaseDate()
                    .isBefore(releaseDateTime) || importList.get(0).getStatus().equals(IMPORT_STATUS_FAILED))) {
                Logger.info("New GTFS dataset found!");
                // Check if last import status is in progress then skip
                if (importList.get(0).getStatus().equals(IMPORT_STATUS_IN_PROGRESS)) {
                    Logger.error("There is an GTFS import process still in progress!");
                }
                return new Dataset(GTFS_DEFAULT_NAME, link.absUrl("href"), releaseDateTime);
            } else {
                Logger.info("New GTFS dataset not found!");
            }
        } catch (Exception e) {
            String logMessage = "Error while checking for new GTFS dataset: " + e.getMessage();
            throw new RuntimeException(logMessage);
        }
        return null;
    }

    public String downloadFileFromUrl(Dataset dataset) {
        try {
            Path pathDir = Paths.get(downloadDir);
            Path filePath = Paths.get(downloadDir, dataset.getFileName());
            // Create path destination download file if not exists
            if (!Files.exists(pathDir)) {
                Files.createDirectories(pathDir);
            }
            // Delete all file in dir
            new File().clearUploadDirectory(pathDir.toString());
            // Download file from url
            URL fileUrl = new URL(dataset.getDownloadUrl());
            try (InputStream in = fileUrl.openStream()) {
                Files.copy(in, filePath);
            }
            return filePath.toString();
        } catch (Exception e) {
            String logMessage = "Error while downloading GTFS file from url: " + e.getMessage();
            throw new RuntimeException(logMessage);
        }
    }

    public void importDataset(String filePath, Dataset dataset) {

        Response response = null;
        try {
            // Create request to import data
            RequestBody requestBody = new MultipartBody.Builder()
                    .addFormDataPart("release_date", dataset.getReleaseDate().toString())
                    .addFormDataPart("task_name", "Auto-import " + dataset.getFileName()).setType(MultipartBody.FORM)
                    .addFormDataPart("file", dataset.getFileName(), RequestBody.create(new java.io.File(filePath), MediaType.parse("application/zip")))
                    .build();
            Request postRequest = new Request.Builder().url(importGtfsUrl).post(requestBody).build();
            response = new OkHttpClient().newCall(postRequest).execute();

            // Check if import data success
            if (response.code() == 200) {
                Logger.info("Import GTFS dataset has send to API!");
            } else {
                Logger.error("Import GTFS dataset failed!");
            }
        } catch (Exception e) {
            String logMessage = "Error while importing GTFS dataset: " + e.getMessage();
            throw new RuntimeException(logMessage);
        } finally {
            if (response != null) response.close();
        }
    }
}
