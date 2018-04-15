package com.carlosmedina.javareactproject.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

public class FileUtil {

    private static String originalFilename;
    private static String dateTimeStr;
    private static MultipartFile multipartFile;
    private static String extension;
    public static String inputString;
    public static String outputString;

    public static final String DIRECTORY = File.separator + "TempJavaReactFolder" + File.separator;

    public static void init(MultipartFile multipartFile) throws IOException {
        FileUtil.multipartFile = multipartFile;
        FileUtil.originalFilename = multipartFile.getOriginalFilename();
        FileUtil.dateTimeStr = LocalDateTime.now().toString().replace(":","").replace("-","");
        FileUtil.extension = FileUtil.findExtension();
        FileUtil.inputString = new String(multipartFile.getBytes(), StandardCharsets.UTF_8);
        FileUtil.saveFile(multipartFile.getBytes(), "INPUT");
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    private String getOriginalFilename() {
        return this.originalFilename;
    }

    public void setDateTimeStr(String dateTimeStr) {
        this.dateTimeStr = dateTimeStr;
    }

    public String getDateTimeStr() {
        return dateTimeStr;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public static String getInputString() {
        return inputString;
    }

    public static void setInputString(String inputString) {
        FileUtil.inputString = inputString;
    }

    public static String getOutputString() {
        return outputString;
    }

    public static void setOutputString(String outputString) {
        FileUtil.outputString = outputString;
    }

    public static boolean isValidExtension() {
        if (FileUtil.originalFilename.indexOf(".") < 0) {
            return false;
        } else {
            String extension = FileUtil.originalFilename.substring(
                    FileUtil.originalFilename.lastIndexOf(".") + 1);

            if (!extension.equalsIgnoreCase("txt")) {
                return false;
            }
        }
        return true;
    }

    public static boolean isEmptyFile() {
        return FileUtil.multipartFile.isEmpty();
    }

    private static String findExtension() {
        if (FileUtil.originalFilename.indexOf(".") < 0) {
            return null;
        } else {
            return FileUtil.extension = FileUtil.originalFilename.substring(
                    FileUtil.originalFilename.lastIndexOf(".") + 1);
        }
    }

    public static String getFilename(String fileType) {
        return fileType.concat("_").concat(FileUtil.dateTimeStr).concat(".").concat(FileUtil.extension).toUpperCase();
    }

    public static Path createFile(String fileType) throws IOException {
        Path path = Paths.get(DIRECTORY + FileUtil.getFilename(fileType));
        System.out.println(Files.isDirectory(Paths.get(DIRECTORY)));
        System.out.println(Files.notExists(path));

        // Verificamos si el directorio existe, si no lo creamos
        if (!Files.isDirectory(Paths.get(DIRECTORY))) {
            Files.createDirectories(path.getParent());
        }
        // Verificamos si el archivo en el directorio existe, si no lo creamos
        if (Files.notExists(path)) {
            return Files.createFile(path);
        }

        return null;
    }

    public static void saveFile(byte[] content, String fileType) {
        try {
            Path inputFile = FileUtil.createFile(fileType);
            if (Files.isWritable(inputFile)) {
                Files.write(inputFile, content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean hasInvalidDigits() {
        InputStream is = null;
        BufferedReader br = null;
        try {
            is = FileUtil.multipartFile.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = br.readLine()) != null) {
                // Valida que cada número en el archivo sea un entero
                if(!FileUtil.isInteger(line)) {
                    return true;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean isInteger(String s) {
        return FileUtil.isInteger(s,10);
    }

    public static boolean isInteger(String s, int radix) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
    }



}
