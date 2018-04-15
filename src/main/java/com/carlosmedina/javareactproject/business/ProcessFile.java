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

public class ProcessFile {

    private MultipartFile multipartFile;

    public ProcessFile(MultipartFile multipartFile) {
        this.multipartFile = multipartFile;
    }



    private boolean isWorkDaysNumberValid(int number) {
        return number >= 1 && number <= PropertiesUtil.MAXIMUM_WORKDAYSNUMBER;
    }

    private boolean isItemNumbersValueValid(int number) {
        return number >= 1 && number <= PropertiesUtil.MAXIMUM_ITEMNUMBERS_VALUE;
    }

    private boolean isItemWeightValueValid(int number) {
        return number >= 1 && number <= PropertiesUtil.MAXIMUM_ITEMWEIGHT_VALUE;
    }

    public Map<String, String> processInputFile() {

        BufferedReader br;
        int workDaysNumber = 0;
        int packagesNumberForCurrentDay = 0;
        int iterationByDay = 0;
        int dayNumber = 0;
        List<Integer> packagesInCurrentDay = new ArrayList<>();
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
            if (firstLine == null || !FileUtil.isInteger(firstLine)) {
                System.err.println("El valor obtenido para el Número de días de trabajo no es un entero");
                return null; // RETORNAR MENSAJE EN MAP
            }

            workDaysNumber = Integer.parseInt(firstLine);

            if (!this.isWorkDaysNumberValid(workDaysNumber)) {
                System.err.println("El número de días trabajados supera el límite de: " + PropertiesUtil.MAXIMUM_WORKDAYSNUMBER);
                return null; // RETORNAR MENSAJE EN MAP
            }

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
                    //System.out.println(packagesNumberForCurrentDay);
                    higherArr.clear();
                    lessArr.clear();
                    //generalArrayPos = this.prepareArrayList(packagesNumberForCurrentDay);
                    continue;
                }


                iterationByDay++;
                if (packagesNumberForCurrentDay >= iterationByDay) {
                    //System.out.println("Evaluando día: " + dayNumber);
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

                        int packagesNumberIteration = 0;
                        //int[] highestLessArrPos = this.prepareArray(lessArr.size());
                        List<Integer> highestLessArrPosList = new ArrayList<>();
                        //int[] lesserLessArrPos = this.prepareArray(lessArr.size());
                        List<Integer> lesserLessArrPosList = new ArrayList<>(); //this.prepareArrayList(lessArr.size());

                        //while (packagesNumberForCurrentDay > packagesNumberIteration) {
                        int arrayLessIteration = -1;
                        for (int arrayLessValue: lessArr) {
                            if (highestLessArrPosList.size() + lesserLessArrPosList.size() == lessArr.size()) {
                                break;
                            }
                            arrayLessIteration++;
                            // Terminó la cantidad de paquetes en el día iterado
                            // Se realizan las operaciones para ordenar estratégicamente los paquetes

                            // Obtener el mayor de los menores (highestLess)
                            int highestLessPos = 0;
                            int highestLessIteration = -1;
                            int highestLess = 0;
                            for (int higher : lessArr) {
                                highestLessIteration++;
                                if (this.isValueInArr(highestLessArrPosList, highestLessIteration)) {
                                    continue;
                                }
                                if (this.isValueInArr(lesserLessArrPosList, highestLessIteration)) {
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
                            //highestLessArrPos[packagesNumberIteration] = highestLessPos;
                            highestLessArrPosList.add(highestLessPos);

                            /* Calcular cuántos paquetes necesarios se pueden añadir para que el producto del mayor de los menores
                            por la cantidad de paquetes necesarios a añadir llegue o pase las 50 libras. */
                            numberPackagesNeeded = this.packagesNeededForMinimunPounds(highestLess);

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
                                    if (this.isValueInArr(highestLessArrPosList, lesserLessIteration)) {
                                        continue;
                                    }
                                    if (this.isValueInArr(lesserLessArrPosList, lesserLessIteration)) {
                                        continue;
                                    }
                                    if (lesserLess >= less) {
                                        lesserLessPos = lesserLessIteration;
                                        lesserLess = less;
                                    }
                                }
                                //lesserLessArrPos[packagesNumberIteration] = lesserLessPos;
                                lesserLessArrPosList.add(lesserLessPos);
                                //generalArrayPos.add(lesserLess);
                                numberPackagesNeededIteration++;
                            }

                            int sumPositions = this.sumOfPositions(packagesNumberForCurrentDay, higherArr,
                                    highestLessArrPosList, lesserLessArrPosList);

                            if (packagesNumberForCurrentDay >= sumPositions) {
                                travelNumbers++;
                            }


                        } // Cierre del while (packagesNumberForCurrentDay >= packagesNumberIteration)

                    }

                }

                // Se recogieron todos los paquetes del día
                if (packagesNumberForCurrentDay == iterationByDay) {
                    // Escribir viaje en el archivo OUTPUT
                    System.out.println("Case #" + dayNumber + " : " + travelNumbers);
                    outputText.append("Case #" + dayNumber + " : " + travelNumbers).append(System.lineSeparator());
                    travelNumbers = 0;
                    // Iteración de viajes por día regresa a 0
                    iterationByDay = 0;
                }

            }

            FileUtil.saveFile(String.valueOf(outputText).getBytes(), "OUTPUT");
            FileUtil.setOutputString(outputText.toString());
            FileUtil.setStatusProcess("EXITOSO");

        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        } catch (NumberFormatException ex) {
            System.err.println(ex.getMessage());
        }
        return null;
    }

    public Map<String, String> validateFileResponse() {
        Map<String, String> response = new HashMap<>();

        if (!FileUtil.isValidExtension()) {
            response.put("status", "error");
            response.put("message", "El archivo cargado no parece tener el formato adecuado, por favor agregue un archivo con formato adecuado (.txt).");
            return response;
        }

        if (FileUtil.isEmptyFile()){
            response.put("status", "error");
            response.put("message", "El archivo está vacío, por favor agregue un archivo con contenido.");
            return response;
        }

        if (FileUtil.hasInvalidDigits()) {
            response.put("status", "error");
            response.put("message", "Al parecer algunos de los valores del archivo no corresponden con dígitos de tipo entero.");
            return response;
        }

        response.put("status", "success");

        return response;
    }

    private int packagesNeededForMinimunPounds(int number) {
        if (PropertiesUtil.MINIMUM_POUNDS_VALUE % number == 0) {
            return (PropertiesUtil.MINIMUM_POUNDS_VALUE / number) - 1;
        } else {
            return PropertiesUtil.MINIMUM_POUNDS_VALUE / number;
        }
    }

    private int[] prepareArray(int size) {
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = -1;
        }
        return arr;
    }

    private List<Integer> prepareArrayList(int num) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            list.add(-1);
        }
        return list;
    }

    private boolean isValueInArr(int[] arr, int number) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == number) {
                return true;
            }
        }
        return false;
    }

    private boolean isValueInArr(List<Integer> arr, int number) {
        for (int num : arr) {
            if (num == number) {
                return true;
            }
        }
        return false;
    }

    private int arrayPositionsOccupied(int[] arrA, int[] arrB) {
        int countPosA = 0, countPosB = 0;
        for (int ia = 0; ia < arrA.length; ia++) {
            if (arrA[ia] != -1) {
                countPosA++;
            }
        }
        for (int ib = 0; ib < arrB.length; ib++) {
            if (arrB[ib] != -1) {
                countPosB++;
            }
        }
        return countPosA + countPosB;
    }

    private int arrayPositionsOccupied(List<Integer> arrA, List<Integer> arrB) {
        int countPosA = 0, countPosB = 0;
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

    private int sumOfPositions(int packagesNumber, List<Integer> higherArr, int[] highestLessArrPos, int[] lesserLessArrPos) {
        int positionsOccupied = this.arrayPositionsOccupied(highestLessArrPos, lesserLessArrPos);
        int totalPositions = higherArr.size() + positionsOccupied;
        return totalPositions;
    }

    private int sumOfPositions(int packagesNumber, List<Integer> higherArr, List<Integer> highestLessArrPos, List<Integer> lesserLessArrPos) {
        int positionsOccupied = this.arrayPositionsOccupied(highestLessArrPos, lesserLessArrPos);
        int totalPositions = higherArr.size() + positionsOccupied;
        return totalPositions;
    }

}