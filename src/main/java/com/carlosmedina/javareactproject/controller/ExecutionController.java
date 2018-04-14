package com.carlosmedina.javareactproject.controller;

import com.carlosmedina.javareactproject.Util.FileUtil;
import com.carlosmedina.javareactproject.business.ProcessFile;
import com.carlosmedina.javareactproject.model.Execution;
import com.carlosmedina.javareactproject.repository.ExecutionJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/execution")
public class ExecutionController {

    // TODO: todas las cadenas de respuestas colocarlas en archivo separado
    // TODO: separar cada validacion del archivo en métodos diferentes

    @Autowired
    private ExecutionJpaRepository executionJpaRepository;

    //private FileUtil fileUtil;

    @GetMapping(value = "/all")
    public List<Execution> findAll() {
        return executionJpaRepository.findAll();
    }

    @RequestMapping(value = "/executeFile",
            headers = "content-type=multipart/*",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> executeInputFile(@RequestParam(value = "file", required = true) MultipartFile multipartFile) throws IOException {

        Map<String, String> response = new HashMap<>();

        try {
            FileUtil.init(multipartFile);
            ProcessFile processFile = new ProcessFile(multipartFile);
            if (processFile.validateFileResponse().get("status").equalsIgnoreCase("error")) {
                return processFile.validateFileResponse();
            }
            processFile.processInputFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    @RequestMapping(value = "/load", method = RequestMethod.POST)
    public String load(@RequestBody final Execution execution) {
        executionJpaRepository.save(execution);
        return "aa";
    }

    private Path createOutputFile() {
        return null;
    }

}
