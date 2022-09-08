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
public class ImportPsaService implements GlobalVariable {

    @Autowired
    private ImportRepository importRepository;
    @Value("${psa.source.url}")
    private String psaSourceUrl;
    @Value("${psa.download.dir}")
    private String downloadDir;
    @Value("${psa.import.url}")
    private String importPsaUrl;

    public void runAutoImport() {
        try {
            String filePath = null;
            // Check if new dataset exists
            Logger.info("Checking for new PSA datasets...");
            Dataset dataset = this.getNewPsaDataset();
            // If file exists then download it
            if (dataset != null) {
                Logger.info("Downloading new PSA datasets...");
                filePath = this.downloadFileFromUrl(dataset);
            }
            // If filePath is not null then import it
            if (filePath != null) {
                Logger.info("Importing new PSA datasets...");
                this.importDataset(filePath, dataset);
            }
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
    }

    public Dataset getNewPsaDataset() {
        try {
            Document document = Jsoup.connect(psaSourceUrl).get();
            Element element = document.getElementsByTag("tbody").select("tr").get(1);
            Element link = element.getElementsByClass("link").get(0);

            // Get release date
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
            Date parsedDate = dateFormat.parse(element.getElementsByClass("date").text());
            LocalDateTime releaseDateTime = parsedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

            List<Import> importList = importRepository.findImportDataByFileType(PSA_FILE_TYPE,
                    PageRequest.of(0,1));
            // Check if import data exists
            if (importList.size() == 0) {
                Logger.info("New PSA dataset found!");
                return new Dataset(link.text(), link.absUrl("href"), releaseDateTime);
            }

            // If filename not exists
            if (!importRepository.existsByFileName(link.text())) {
                Logger.info("New PSA dataset found!");
                // Check if last import status is in progress then skip
                if (importList.get(0).getStatus().equals(IMPORT_STATUS_IN_PROGRESS)) {
                    Logger.error("There is an PSA import process still in progress!");
                    return null;
                }
                return new Dataset(link.text(), link.absUrl("href"), releaseDateTime);
            } else {
                Logger.info("New PSA dataset not found!");
            }
        } catch (Exception e) {
            String logMessage = "Error while checking for new PSA dataset: " + e.getMessage();
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
            String logMessage = "Error while downloading PSA file from url: " + e.getMessage();
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
            Request postRequest = new Request.Builder().url(importPsaUrl).post(requestBody).build();
            response = new OkHttpClient().newCall(postRequest).execute();

            // Check if import data success
            if (response.code() == 200) {
                Logger.info("Import PSA dataset has send to API!");
            } else {
                Logger.error("Import PSA dataset failed!");
            }
        } catch (Exception e) {
            String logMessage = "Error while importing PSA dataset: " + e.getMessage();
            throw new RuntimeException(logMessage);
        } finally {
            if (response != null) response.close();
        }
    }
}
