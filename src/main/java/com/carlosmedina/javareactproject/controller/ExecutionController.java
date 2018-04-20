package com.carlosmedina.javareactproject.controller;

import com.carlosmedina.javareactproject.util.FileUtil;
import com.carlosmedina.javareactproject.business.ProcessFile;
import com.carlosmedina.javareactproject.model.Execution;
import com.carlosmedina.javareactproject.repository.ExecutionJpaRepository;
import com.carlosmedina.javareactproject.util.PropertiesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/execution")
@CrossOrigin(origins = "http://localhost:3000")
public class ExecutionController {

    private static final Logger LOGGER = Logger.getLogger("com.carlosmedina.javareactproject.controller.ExecutionController");

    @Autowired
    private ExecutionJpaRepository executionJpaRepository;

    @GetMapping(value = "/all")
    public List<Execution> findAll() {
        return executionJpaRepository.findAll();
    }

    @RequestMapping(value = "/executeFile",
            headers = "content-type=multipart/*",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InputStreamResource> executeInputFile(
            @RequestParam(value = "file", required = true) MultipartFile multipartFile,
            @RequestParam(value = "executorDNI") String executorDNI) throws IOException {

        Execution execution = new Execution();
        execution.setExecutorDNI(Long.parseLong(executorDNI));
        execution.setExecutionDate(FileUtil.getProcessDate());

        try {
            FileUtil.init(multipartFile);
            ProcessFile processFile = new ProcessFile(multipartFile);

            // Inicia proceso de validación del archivo de entrada
            Map<String, String> processValidation = processFile.validateFileResponse();
            if (processValidation.get(PropertiesUtil.STATUS).equalsIgnoreCase(PropertiesUtil.ERROR)) {
                execution.setUrlFile(FileUtil.getUrlFile(PropertiesUtil.OUTPUT_WITH_ERRORS));
                execution.setStatusProcess("CON ERRORES");
                this.load(execution);
                return this.sendErrorFile(processValidation.get(PropertiesUtil.MESSAGE));
            }

            processFile.processInputFile();

            execution.setUrlFile(FileUtil.getUrlFile(PropertiesUtil.OUTPUT));
            execution.setStatusProcess(FileUtil.getStatusProcess());
            this.load(execution);
            return this.downloadOutputFile(FileUtil.getFilename(PropertiesUtil.OUTPUT));
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, ex.getMessage());
        }
        return null;
    }

    private void load(@RequestBody final Execution execution) {
        executionJpaRepository.save(execution);
    }

    public ResponseEntity<InputStreamResource> downloadOutputFile(String filename) throws IOException {
        File file = new File(FileUtil.DIRECTORY + filename);

        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment;filename=" + file.getName())
                .contentType(MediaType.TEXT_PLAIN).contentLength(file.length())
                .body(resource);
    }

    @RequestMapping(value = "/downloadFile",
            headers = "content-type=multipart/*",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InputStreamResource> downloadFile(
            @RequestParam(value = "filename") String filename) throws IOException {
        LOGGER.log(Level.INFO, filename);
        return this.downloadOutputFile(filename);
    }

    public ResponseEntity<InputStreamResource> sendErrorFile(String response) throws IOException {

        FileUtil.saveFile(response.getBytes(StandardCharsets.UTF_8),PropertiesUtil.OUTPUT_WITH_ERRORS);
        File file = new File(FileUtil.DIRECTORY + FileUtil.getFilename(PropertiesUtil.OUTPUT_WITH_ERRORS));

        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment;filename=" + file.getName())
                .contentType(MediaType.TEXT_PLAIN).contentLength(file.length())
                .body(resource);
    }

}
