package com.maestronic.autoimportgtfs.service;

import com.maestronic.autoimportgtfs.util.GlobalVariable;
import com.maestronic.autoimportgtfs.util.Logger;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class StartUpService {

    @Autowired
    private ImportGtfsService importGtfsService;
    @Autowired
    private ImportChbService importChbService;
    @Autowired
    private ImportPsaService importPsaService;
    private boolean isGtfsStarted = false;
    private boolean isChbStarted = false;
    private boolean isPsaStarted = false;
    @Value("${import.ready.url}")
    private String checkImportUrl;
    @Value("${import.mode}")
    private String importMode;

//    /**
//     * Start the import of the CHB data.
//     */
//    @EventListener(ApplicationReadyEvent.class)
//    @Scheduled(cron = "${cron.expression.auto-import-schedule}")
//    public void startAutoImportChb() {
//        // Check if the chb import is already started
//        if (!isChbStarted) {
//            isChbStarted = true;
//            this.checkApiReady();
//            importChbService.runAutoImport();
//            isChbStarted = false;
//        }
//    }
//
//    /**
//     * Start the import of the PSA data.
//     */
//    @EventListener(ApplicationReadyEvent.class)
//    @Scheduled(cron = "${cron.expression.auto-import-schedule}")
//    public void startAutoImportPsa() {
//        // Check if the psa import is already started
//        if (!isPsaStarted) {
//            isPsaStarted = true;
//            this.checkApiReady();
//            importPsaService.runAutoImport();
//            isPsaStarted = false;
//        }
//    }

    /**
     * Start the import of the GTFS data.
     * To use scrapping method from transitfeed, please uncomment "importGtfsService.runAutoImportScrapping()" and
     *      comment "importGtfsService.runAutoImport()"
     * To use URL direct to zip file, please uncomment "importGtfsService.runAutoImport()" and
     *      comment "importGtfsService.runAutoImportScrapping()"
     */
    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "${cron.expression.auto-import-schedule}")
    public void startAutoImportGtfs() {
        // Check if the gtfs import is already started
        if (!isGtfsStarted) {
            isGtfsStarted = true;
            this.checkApiReady();

            if (importMode.equals(GlobalVariable.IMPORT_MODE)) {
                // For auto import with transitfeed website (use scrapping)
                importGtfsService.runAutoImportScrapping();
            } else {
                // Use for auto import direct to zip file
                importGtfsService.runAutoImport();
            }

            isGtfsStarted = false;
        }
    }

    public void checkApiReady() {
        boolean isNotReady = true;
        while (isNotReady) {
            Response response = null;
            try {
                Request request = new Request.Builder().url(checkImportUrl).build();
                response = new OkHttpClient().newCall(request).execute();
                if (response.code() == 200) {
                    isNotReady = false;
                } else {
                    Thread.sleep(10000);
                }
            } catch (IOException | InterruptedException e) {
                 Logger.error("Error while connecting to API! " + e.getMessage());
            } finally {
                if (response != null) response.close();
            }
        }
    }
}
