package com.maestronic.autoimportgtfs.service;

import com.maestronic.autoimportgtfs.dto.DatasetDto;
import com.maestronic.autoimportgtfs.dto.ReportDto;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
    @Value("${webhook.url}")
    private String webhookUrl;
    private ReportDto report;
    private List<Map<String, Object>> fileDetails = new ArrayList<>();

    public ReportDto runAutoImport(ReportDto reportDto) {
        try {
            DatasetDto datasetDto = new DatasetDto(GTFS_DEFAULT_NAME, gtfsSourceUrl);
            fileDetails = new ArrayList<>();
            report = reportDto;

            // Download datasetDto from URL
            Logger.info("Downloading GTFS datasets...");
            String filePath = this.downloadFileFromUrl(datasetDto);

            // Check attribute of GTFS file
            LocalDateTime newCreationTime = this.checkNewGtfsVersion(filePath);
            datasetDto.setReleaseDate(newCreationTime);

            // If filePath is not null then import it
            if (newCreationTime != null) {
                Logger.info("Importing new GTFS datasets...");
                this.importDataset(filePath, datasetDto);
            }

            report.setFiles(fileDetails);
            report.setFileSize(new java.io.File(filePath).length());
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }

        return report;
    }

    public ReportDto runAutoImportScrapping(ReportDto reportDto) {
        try {
            String filePath = null;
            report = reportDto;

            // Check if new datasetDto exists
            Logger.info("Checking for new GTFS datasets...");
            DatasetDto datasetDto = this.getNewGtfsDataset();
            // If gtfs file exists then download it
            if (datasetDto != null) {
                Logger.info("Downloading new GTFS datasets...");
                filePath = this.downloadFileFromUrl(datasetDto);
            }
            // If filePath is not null then import it
            if (filePath != null) {
                Logger.info("Importing new GTFS datasets...");
                this.importDataset(filePath, datasetDto);
            }
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }

        return report;
    }

    private LocalDateTime getNewestCreationTime(String filePath) {

        try {
            FileTime creationTime = null;
            ZipFile zip = new ZipFile(filePath);
            for (Enumeration e = zip.entries(); e.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                if (!entry.isDirectory()) {
                    FileTime creationTimeNext = entry.getLastModifiedTime();
                    if (creationTime == null) {
                        creationTime = creationTimeNext;
                        continue;
                    }
                    else if (creationTime.compareTo(creationTimeNext) < 0) {
                        creationTime = creationTimeNext;
                    }
                    fileDetails.add(new HashMap<String, Object>() {{
                        put("fileName", entry.getName());
                        put("size", entry.getSize());
                        put("lastModifiedTime", entry.getLastModifiedTime());
                    }});
                }
            }
            zip.close();
            return LocalDateTime.parse(LocalDateTime.ofInstant(creationTime.toInstant(), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private LocalDateTime checkNewGtfsVersion(String filePath) {
        String logMessage;
        try {
            // Check validation of each file
            LocalDateTime newestCreationTime = getNewestCreationTime(filePath);

            // Compare the newest creation time with released date time on database
            // Get last import
            List<Import> importList = importRepository.findImportDataByFileType(GTFS_FILE_TYPE,
                    PageRequest.of(0,1));
            // Check if import data exists
            if (importList.size() == 0) {
                Logger.info("New GTFS dataset found!");
                return newestCreationTime;
            }
            // Check if release date is null
            // Check the date from website is latter than the last import or import status is failed
            if (importList.get(0).getReleaseDate() == null || (importList.get(0).getReleaseDate()
                    .isBefore(newestCreationTime) || importList.get(0).getStatus().equals(IMPORT_STATUS_FAILED))) {
                Logger.info("New GTFS dataset found!");
                // Check if last import status is in progress then skip
                if (importList.get(0).getStatus().equals(IMPORT_STATUS_IN_PROGRESS)) {
                    Logger.error("There is an GTFS import process still in progress!");
                    return null;
                }
                return newestCreationTime;
            } else {
                Logger.info("New GTFS dataset not found!");
            }
        } catch (Exception e) {
            logMessage = "Check GTFS version failed. " + e.getMessage();
            Logger.error(logMessage);
            throw new RuntimeException(logMessage);
        }
        return null;
    }

    public DatasetDto getNewGtfsDataset() {
        try {
            Document document = Jsoup.connect(gtfsSourceUrl).get();
            Elements elements = document.getElementsByTag("tbody").select("tr").get(0).getElementsByTag("a");
            Element link = elements.get(elements.size() - 1);

            // Get release date
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
            Date parsedDate = dateFormat.parse(elements.get(0).text());
            LocalDateTime releaseDateTime = LocalDateTime.parse(LocalDateTime.ofInstant(parsedDate.toInstant(), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));

            // Get last import
            List<Import> importList = importRepository.findImportDataByFileType(GTFS_FILE_TYPE,
                    PageRequest.of(0,1));
            // Check if import data exists
            if (importList.size() == 0) {
                Logger.info("New GTFS dataset found!");
                return new DatasetDto(GTFS_DEFAULT_NAME, link.absUrl("href"), releaseDateTime);
            }
            // Check if release date is null
            // Check the date from website is latter than the last import or import status is failed
            if (importList.get(0).getReleaseDate() == null || (importList.get(0).getReleaseDate()
                    .isBefore(releaseDateTime) || importList.get(0).getStatus().equals(IMPORT_STATUS_FAILED))) {
                Logger.info("New GTFS dataset found!");
                // Check if last import status is in progress then skip
                if (importList.get(0).getStatus().equals(IMPORT_STATUS_IN_PROGRESS)) {
                    Logger.error("There is an GTFS import process still in progress!");
                    return null;
                }
                return new DatasetDto(GTFS_DEFAULT_NAME, link.absUrl("href"), releaseDateTime);
            } else {
                Logger.info("New GTFS dataset not found!");
            }
        } catch (Exception e) {
            String logMessage = "Error while checking for new GTFS dataset: " + e.getMessage();
            throw new RuntimeException(logMessage);
        }
        return null;
    }

    private String downloadFileFromUrl(DatasetDto datasetDto) {
        try {
            Path pathDir = Paths.get(downloadDir);
            Path filePath = Paths.get(downloadDir, datasetDto.getFileName());
            // Create path destination download file if not exists
            if (!Files.exists(pathDir)) {
                Files.createDirectories(pathDir);
            }
            // Delete all file in dir
            new File().clearUploadDirectory(pathDir.toString());
            // Download file from url
            HttpURLConnection httpConnect = (HttpURLConnection) new URL(datasetDto.getDownloadUrl()).openConnection();
            httpConnect.addRequestProperty("User-Agent", "Mozilla");

            InputStream in = httpConnect.getInputStream();
            Files.copy(in, filePath);

            return filePath.toString();
        } catch (Exception e) {
            e.printStackTrace();
            String logMessage = "Error while downloading GTFS file from url: " + e.getMessage();
            throw new RuntimeException(logMessage);
        }
    }

    private void importDataset(String filePath, DatasetDto datasetDto) {

        Response response = null;
        String releaseDate = datasetDto.getReleaseDate().toString();
        String taskName = "Auto-import " + datasetDto.getFileName();
        try {
            // Create request to import data
            RequestBody requestBody = new MultipartBody.Builder()
                    .addFormDataPart("release_date", releaseDate)
                    .addFormDataPart("task_name", taskName).setType(MultipartBody.FORM)
                    .addFormDataPart("file", datasetDto.getFileName(), RequestBody.create(new java.io.File(filePath), MediaType.parse("application/zip")))
                    .build();
            Request postRequest = new Request.Builder().url(importGtfsUrl).post(requestBody).build();
            response = new OkHttpClient().newCall(postRequest).execute();

            // Check if import data success
            if (response.code() == 200) {
                Logger.info("Import GTFS datasetDto has send to API!");
                report.setStatus(GlobalVariable.GTFS_NEW_DATA);
            } else {
                Logger.error("Import GTFS datasetDto failed! " + response);
            }
        } catch (Exception e) {
            String logMessage = "Error while importing GTFS datasetDto: " + e.getMessage();
            throw new RuntimeException(logMessage);
        } finally {
            if (response != null) response.close();

            Map<String, Object> res = new HashMap<>();
            res.put("protocol", response.protocol());
            res.put("code", response.code());
            res.put("message", response.message());
            res.put("url", response.request().url());
            // Set to report
            report.setRequestDetails(new HashMap<String, Object>() {{
                put("releaseDate", releaseDate);
                put("taskName", taskName);
                put("file", filePath);
                put("requestUrl", importGtfsUrl);
            }});
            report.setResponseDetails(res);
        }
    }

    public void sendReport(ReportDto reportDto) {

        Response response = null;
        try {
            // Create request to import data
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("jobRunTime", reportDto.getJobRunTime() == null ? "" : reportDto.getJobRunTime().toString())
                    .addFormDataPart("fileSize", reportDto.getFileSize() == null ? "" : reportDto.getFileSize().toString())
                    .addFormDataPart("status", reportDto.getStatus())
                    .addFormDataPart("extractedFiles", reportDto.getFiles() == null ? "" : reportDto.getFiles().toString())
                    .addFormDataPart("requestDetails", reportDto.getRequestDetails() == null ? "" : reportDto.getRequestDetails().toString())
                    .addFormDataPart("responseDetails", reportDto.getResponseDetails() == null ? "" : reportDto.getResponseDetails().toString())
                    .build();
            Request postRequest = new Request.Builder().url(webhookUrl).post(requestBody).build();
            response = new OkHttpClient().newCall(postRequest).execute();
        } catch (Exception e) {
            String logMessage = "Error while send report: " + e.getMessage();
            throw new RuntimeException(logMessage);
        } finally {
            if (response != null) response.close();
        }
    }
}
