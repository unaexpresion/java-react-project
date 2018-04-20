package com.carlosmedina.javareactproject.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileUtil {

    private static String originalFilename;
    private static String dateTimeStr;
    private static MultipartFile multipartFile;
    private static String extension;
    private static String inputString;
    private static String outputString;
    private static String statusProcess;

    private static final Logger LOGGER = Logger.getLogger("com.carlosmedina.javareactproject.util.FileUtil");

    public static final String DIRECTORY = File.separator + "TempJavaReactFolder" + File.separator;

    private FileUtil() {}

    public static void init(MultipartFile multipartFile) throws IOException {
        FileUtil.multipartFile = multipartFile;
        FileUtil.originalFilename = multipartFile.getOriginalFilename();
        FileUtil.dateTimeStr = LocalDateTime.now().toString().replace(":","").replace("-","");
        FileUtil.extension = FileUtil.findExtension();
        FileUtil.inputString = new String(multipartFile.getBytes(), StandardCharsets.UTF_8);
        FileUtil.saveFile(multipartFile.getBytes(), "INPUT");
    }

    public static void setOutputString(String outputString) {
        FileUtil.outputString = outputString;
    }

    public static String getStatusProcess() {
        return statusProcess;
    }

    public static void setStatusProcess(String statusProcess) {
        FileUtil.statusProcess = statusProcess;
    }

    private static boolean isWorkDaysNumberValid(int number) {
        return number >= 1 && number <= PropertiesUtil.MAXIMUM_WORKDAYSNUMBER;
    }

    private static boolean isItemNumberValid(int number) {
        return number >= 1 && number <= PropertiesUtil.MAXIMUM_ITEMNUMBERS_VALUE;
    }

    private static boolean isItemWeightValid(int number) {
        return number >= 1 && number <= PropertiesUtil.MAXIMUM_ITEMWEIGHT_VALUE;
    }

    public static boolean isValidExtension() {
        if (FileUtil.originalFilename.indexOf('.') < 0) {
            return false;
        } else {
            String extension = FileUtil.originalFilename.substring(
                    FileUtil.originalFilename.lastIndexOf('.') + 1);

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
        if (FileUtil.originalFilename.indexOf('.') < 0) {
            return null;
        } else {
            FileUtil.extension = FileUtil.originalFilename.substring(
                    FileUtil.originalFilename.lastIndexOf('.') + 1);
            return FileUtil.extension;
        }
    }

    public static String getFilename(String fileType) {
        return fileType.concat("_").concat(FileUtil.dateTimeStr).concat(".").concat(FileUtil.extension).toUpperCase();
    }

    public static String getUrlFile(String fileType) {
        return FileUtil.getFilename(fileType);
    }

    public static Path createFile(String fileType) throws IOException {
        Path path = Paths.get(DIRECTORY + FileUtil.getFilename(fileType));

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
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, ex.getMessage());
        }
    }

    public static boolean hasInvalidDigits() {
        InputStream is;
        BufferedReader br;
        int iterationValue = 0;
        int itemNumbers = 0;
        int currentItem = 1;
        int workDaysNumber;
        int itemWeight;
        try {
            is = FileUtil.multipartFile.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

            String line;
            String firstLine = br.readLine();
            if (firstLine == null || !FileUtil.isInteger(firstLine)) {
                LOGGER.log(Level.INFO, "El valor obtenido para el Número de días de trabajo no es un entero");
                return true;
            }

            workDaysNumber = Integer.parseInt(firstLine);

            if (!FileUtil.isWorkDaysNumberValid(workDaysNumber)) {
                LOGGER.log(Level.INFO, "El número de días trabajados supera el límite de: " + PropertiesUtil.MAXIMUM_WORKDAYSNUMBER);
                return true;
            }

            while ((line = br.readLine()) != null) {
                iterationValue++;
                // Valida que cada número en el archivo sea un entero
                if(!FileUtil.isInteger(line)) {
                    return true;
                }

                if (iterationValue == 1) {
                    itemNumbers = Integer.parseInt(line);
                    if (!FileUtil.isItemNumberValid(itemNumbers)) {
                        return true;
                    }
                    continue;
                }

                if (itemNumbers >= currentItem) {
                    itemWeight = Integer.parseInt(line);
                    if (!FileUtil.isItemWeightValid(itemWeight)) {
                        return true;
                    }
                    if (itemNumbers == currentItem) {
                        itemNumbers = 0;
                        currentItem = 0;
                        iterationValue = 0;
                    }
                    currentItem++;
                }
            }

        } catch (IOException ex) {
            LOGGER.log(Level.INFO, ex.getMessage());
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

    public static Date getProcessDate() {
        Date date = new Date(); // your date
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH+1);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        String dateInString = String.valueOf(year).concat("-").concat(String.valueOf(month))
                .concat("-").concat(String.valueOf(day));

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return formatter.parse(dateInString);
        } catch (ParseException ex) {
            LOGGER.log(Level.INFO, ex.getMessage());
        }

        return new Date();

    }



}
