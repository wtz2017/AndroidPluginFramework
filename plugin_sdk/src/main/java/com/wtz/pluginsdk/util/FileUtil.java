package com.wtz.pluginsdk.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUtil {
    
    public static void deleteFiles(File[] files) {
        for (File f : files) {
            if (f != null && f.exists()) {
                deleteFile(f);
            }
        }
    }

    public static boolean deleteFile(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            for (File child : children) {
                boolean success = deleteFile(child);
                if (!success) {
                    return false;
                }
            }
        }

        // delete file or empty folder
        return file.delete();
    }
    
    public static boolean copy(File srcFile, File desFile) {
        boolean result = false;
        FileInputStream fis = null;
        FileOutputStream fos = null;

        try {
            if (srcFile == null || !srcFile.exists() || desFile == null) {
                return false;
            }

            File desDir = desFile.getParentFile();
            if (!desDir.exists()) {
                desDir.mkdirs();
            }

            fis = new FileInputStream(srcFile);
            fos = new FileOutputStream(desFile);
            byte buffer[] = new byte[4096];
            int readSize = 0;
            while ((readSize = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, readSize);
                fos.flush();
            }
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                result = false;
            }
        }

        return result;
    }
    
    public static void saveSoFromApk(String apkPath, String soSaveDirPath) {
        String ZIPENTRY_NAME_SEPARATOR = "/";
        File soDir = new File(soSaveDirPath);
        if (!soDir.exists()) {
            soDir.mkdirs();
        }

        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(new File(apkPath));
            Enumeration<?> enumeration = zipFile.entries();
            ZipEntry zipEntry = null;
            while (enumeration.hasMoreElements()) {
                zipEntry = (ZipEntry) enumeration.nextElement();
                System.out.println("zipEntry name: " + zipEntry.getName());

                String name = zipEntry.getName();
                if (name.endsWith(".so")) {
                    FileOutputStream fos = null;
                    BufferedOutputStream dest = null;
                    InputStream in = null;
                    try {
                        in = zipFile.getInputStream(zipEntry);
                        int index = name.lastIndexOf(ZIPENTRY_NAME_SEPARATOR);
                        String fileName = name.substring(index + 1);
                        String subFolderName = name.substring(0, index);
                        File subFolder = new File(soSaveDirPath + File.separator + subFolderName);
                        if (!subFolder.exists()) {
                            subFolder.mkdirs();
                        }
                        String destPath = subFolder.getAbsolutePath() + File.separator + fileName;
                        File file = new File(destPath);
                        if (file.exists()) {
                            file.delete();
                        }
                        file.createNewFile();
                        int count;
                        int DATA_BUFFER = 8 * 1024;
                        byte data[] = new byte[DATA_BUFFER];
                        fos = new FileOutputStream(file);
                        dest = new BufferedOutputStream(fos, DATA_BUFFER);
                        while ((count = in.read(data, 0, DATA_BUFFER)) != -1) {
                            dest.write(data, 0, count);
                        }
                        dest.flush();
                        // 在循环中及时关闭创建的流对象
                        if (dest != null) {
                            dest.close();
                        }
                        if (fos != null) {
                            fos.close();
                        }
                        if (in != null) {
                            in.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (dest != null) {
                                dest.close();
                            }
                            if (in != null) {
                                in.close();
                            }
                            if (fos != null) {
                                fos.close();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
}
