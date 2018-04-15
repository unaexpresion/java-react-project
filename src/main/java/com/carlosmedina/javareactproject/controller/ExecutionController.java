package com.carlosmedina.javareactproject.controller;

import com.carlosmedina.javareactproject.util.FileUtil;
import com.carlosmedina.javareactproject.business.ProcessFile;
import com.carlosmedina.javareactproject.model.Execution;
import com.carlosmedina.javareactproject.repository.ExecutionJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Path;
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

    @GetMapping(value = "/all")
    public List<Execution> findAll() {
        return executionJpaRepository.findAll();
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/executeFile",
            headers = "content-type=multipart/*",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InputStreamResource> executeInputFile(@RequestParam(value = "file", required = true) MultipartFile multipartFile,
                                                HttpServletResponse httpServletResponse) throws IOException {

        Map<String, String> response = new HashMap<>();

        try {
            FileUtil.init(multipartFile);
            ProcessFile processFile = new ProcessFile(multipartFile);
            if (processFile.validateFileResponse().get("status").equalsIgnoreCase("error")) {
                //return processFile.validateFileResponse();
            }
            processFile.processInputFile();
            System.out.println("algo aqui");
            return this.downloadOutputFile(httpServletResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/load", method = RequestMethod.POST)
    private void load(@RequestBody final Execution execution) {
        executionJpaRepository.save(execution);
    }

    public ResponseEntity<InputStreamResource> downloadOutputFile(HttpServletResponse response) throws IOException {
        File file = new File(FileUtil.DIRECTORY + FileUtil.getFilename("OUTPUT"));

        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment;filename=" + file.getName())
                .contentType(MediaType.TEXT_PLAIN).contentLength(file.length())
                .body(resource);
    }

}
