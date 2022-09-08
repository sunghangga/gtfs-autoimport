package com.maestronic.autoimportgtfs.util;

public class File {

    public void clearUploadDirectory(String folderPath) {
        java.io.File folder = new java.io.File(folderPath);
        if (folder.exists()) {
            String[] files = folder.list();
            if (files != null) {
                for (String file : files) {
                    if(new java.io.File(folder.getPath(), file).delete()) Logger.info("File '" + file + "' deleted");
                    else Logger.info("File '" + file + "' cannot delete");
                }
            }
        }
    }
}
