package com.carlosmedina.javareactproject.business;

import com.carlosmedina.javareactproject.util.FileUtil;
import com.carlosmedina.javareactproject.util.PropertiesUtil;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProcessFile {

    private static final Logger LOGGER = Logger.getLogger("com.carlosmedina.javareactproject.business.ProcessFile");

    private MultipartFile multipartFile;

    public ProcessFile(MultipartFile multipartFile) {
        this.multipartFile = multipartFile;
    }

    public Map<String, String> processInputFile() {

        BufferedReader br;
        int packagesNumberForCurrentDay = 0;
        int iterationByDay = 0;
        int dayNumber = 0;
        int iterationValue = 0;
        List<Integer> higherArr = new ArrayList<>();
        List<Integer> lessArr = new ArrayList<>();
        StringBuilder outputText = new StringBuilder();
        int numberPackagesNeeded = 0;

        try {
            String line;
            int packageWeight = 0;
            InputStream is = this.multipartFile.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

            // La primera línea, se refiere a la cantidad de días de trabajo
            String firstLine = br.readLine();
            LOGGER.log(Level.INFO, "The first line is: " + firstLine);

            List<Integer> generalArray = new ArrayList<>();
            int travelNumbers = 0;
            while ((line = br.readLine()) != null) {
                iterationValue++;
                if (FileUtil.isInteger(line)) {
                    packageWeight = Integer.parseInt(line);
                }

                if (iterationValue == 1) {
                    packagesNumberForCurrentDay = packageWeight;
                    dayNumber++;
                    higherArr.clear();
                    lessArr.clear();
                    continue;
                }


                iterationByDay++;
                if (packagesNumberForCurrentDay >= iterationByDay) {
                    // REALIZAR OPERACION


                    // Almacenar todos los >= 50 en un array (higherArr), son viajes seguros.
                    if (packageWeight >= PropertiesUtil.MINIMUM_POUNDS_VALUE) {
                        higherArr.add(packageWeight);
                        travelNumbers++;
                        generalArray.add(packageWeight);
                    } else {
                        // Almacenar todos los < 50 en otro array (lessArr).
                        if (packageWeight < PropertiesUtil.MINIMUM_POUNDS_VALUE) {
                            lessArr.add(packageWeight);
                            generalArray.add(packageWeight);
                        }
                    }

                    // Se retornan a cero las variables de control para reiniciar a un nuevo día de trabajo
                    if (packagesNumberForCurrentDay == iterationByDay) {
                        iterationValue = 0;

                        List<Integer> highestLessArrPosList = new ArrayList<>();
                        List<Integer> lesserLessArrPosList = new ArrayList<>();

                        for (int ignored : lessArr) {
                            if (highestLessArrPosList.size() + lesserLessArrPosList.size() == lessArr.size()) {
                                break;
                            }
                            // Terminó la cantidad de paquetes en el día iterado
                            // Se realizan las operaciones para ordenar estratégicamente los paquetes

                            // Obtener el mayor de los menores (highestLess)
                            int highestLessPos = 0;
                            int highestLessIteration = -1;
                            int highestLess = 0;
                            for (int higher : lessArr) {
                                highestLessIteration++;
                                if (this.isValueInArr(highestLessArrPosList, highestLessIteration)
                                        || this.isValueInArr(lesserLessArrPosList, highestLessIteration)) {
                                    continue;
                                }
                                if (higher > highestLess) {
                                    // Almacenar la posición del mayor de los menores
                                    highestLessPos = highestLessIteration;
                                    // Alamcenar el mayor de los menores
                                    highestLess = higher;
                                }
                            }

                            // Guardar en un arreglo las posiciones de los mayores de los menores que se han tratado
                            highestLessArrPosList.add(highestLessPos);

                            /* Calcular cuántos paquetes necesarios se pueden añadir para que el producto del mayor de los menores
                            por la cantidad de paquetes necesarios a añadir llegue o pase las 50 libras. */
                            if (highestLess > 0) {
                                numberPackagesNeeded = this.packagesNeededForMinimunPounds(highestLess);
                            }

                            /* Validar que la cantidad de paquetes necesarios a añadir sean los suficientes
                            realizar otro viaje */
                            if ((highestLessArrPosList.size() + lesserLessArrPosList.size()
                                    + numberPackagesNeeded > packagesNumberForCurrentDay)
                                    || (highestLessArrPosList.size() + lesserLessArrPosList.size() == lessArr.size())) {
                                if (travelNumbers == 0) travelNumbers++;
                                break;
                            }

                            if (numberPackagesNeeded > (lessArr.size() - (highestLessArrPosList.size() + lesserLessArrPosList.size()))) {
                                travelNumbers++;
                                break;
                            }


                            /* Teniendo la cantidad de paquetes necesarios, obtener esa cantidad de los menores del array de menores
                            (lessArr) para garantizar que no se hagan malas estratégias de combinación de paquetes,
                            realizando la mayor cantidad de viajes con el mínimo de libras requerido. */

                            // Obtener el valor menor del array de los menores
                            // (o los valores menores del array de los menores)
                            int lesserLessIteration = -1;
                            int lesserLess;
                            int lesserLessPos = 0;
                            int numberPackagesNeededIteration = 0;
                            while (numberPackagesNeededIteration < numberPackagesNeeded) {
                                lesserLessIteration = -1;
                                lesserLess = highestLess;
                                for (int less : lessArr) {
                                    lesserLessIteration++;
                                    if (this.isValueInArr(highestLessArrPosList, lesserLessIteration)
                                            || this.isValueInArr(lesserLessArrPosList, lesserLessIteration)) {
                                        continue;
                                    }
                                    if (lesserLess >= less) {
                                        lesserLessPos = lesserLessIteration;
                                        lesserLess = less;
                                    }
                                }
                                lesserLessArrPosList.add(lesserLessPos);
                                numberPackagesNeededIteration++;
                            }

                            int sumPositions = this.sumOfPositions(higherArr,
                                    highestLessArrPosList, lesserLessArrPosList);

                            if (packagesNumberForCurrentDay >= sumPositions) {
                                travelNumbers++;
                            }


                        } // Cierre del while

                    }

                }

                // Se recogieron todos los paquetes del día
                if (packagesNumberForCurrentDay == iterationByDay) {
                    // Escribir viaje en el archivo OUTPUT
                    outputText.append("Case #" + dayNumber + " : " + travelNumbers).append(System.lineSeparator());
                    travelNumbers = 0;
                    // Iteración de viajes por día regresa a 0
                    iterationByDay = 0;
                }

            }

            FileUtil.saveFile(String.valueOf(outputText).getBytes(), "OUTPUT");
            FileUtil.setOutputString(outputText.toString());
            FileUtil.setStatusProcess("EXITOSO");

        } catch (IOException | NumberFormatException ex) {
            LOGGER.log(Level.INFO, ex.getMessage());
        }
        return null;
    }

    public Map<String, String> validateFileResponse() {
        Map<String, String> response = new HashMap<>();

        if (!FileUtil.isValidExtension()) {
            response.put(PropertiesUtil.STATUS, PropertiesUtil.ERROR);
            response.put(PropertiesUtil.MESSAGE, "El archivo cargado no parece tener el formato adecuado, por favor agregue un archivo con formato adecuado (.txt).");
            return response;
        }

        if (FileUtil.isEmptyFile()){
            response.put(PropertiesUtil.STATUS, PropertiesUtil.ERROR);
            response.put(PropertiesUtil.MESSAGE, "El archivo está vacío, por favor agregue un archivo con contenido.");
            return response;
        }

        if (FileUtil.hasInvalidDigits()) {
            response.put(PropertiesUtil.STATUS, PropertiesUtil.ERROR);
            response.put(PropertiesUtil.MESSAGE, "Al parecer algunos de los valores del archivo no corresponden con dígitos de tipo entero.");
            return response;
        }

        response.put(PropertiesUtil.STATUS, PropertiesUtil.SUCCESS);

        return response;
    }

    private int packagesNeededForMinimunPounds(int number) {
        try {
            if (PropertiesUtil.MINIMUM_POUNDS_VALUE % number == 0) {
                return (PropertiesUtil.MINIMUM_POUNDS_VALUE / number) - 1;
            } else {
                return PropertiesUtil.MINIMUM_POUNDS_VALUE / number;
            }
        } catch (ArithmeticException ex) {
            LOGGER.log(Level.INFO, "Excepción lanzada al ejecutar división entre cero");
            return 0;
        }
    }

    private boolean isValueInArr(List<Integer> arr, int number) {
        for (int num : arr) {
            if (num == number) {
                return true;
            }
        }
        return false;
    }

    private int arrayPositionsOccupied(List<Integer> arrA, List<Integer> arrB) {
        int countPosA = 0;
        int countPosB = 0;
        for (int numA : arrA) {
            if (numA != -1) {
                countPosA++;
            }
        }
        for (int numB : arrB) {
            if (numB != -1) {
                countPosB++;
            }
        }

        return countPosA + countPosB;
    }

    private int sumOfPositions(List<Integer> higherArr, List<Integer> highestLessArrPos, List<Integer> lesserLessArrPos) {
        int positionsOccupied = this.arrayPositionsOccupied(highestLessArrPos, lesserLessArrPos);
        return higherArr.size() + positionsOccupied;
    }

}