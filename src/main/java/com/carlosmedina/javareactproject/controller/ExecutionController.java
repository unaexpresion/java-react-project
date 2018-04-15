package com.carlosmedina.javareactproject.controller;

import com.carlosmedina.javareactproject.util.FileUtil;
import com.carlosmedina.javareactproject.business.ProcessFile;
import com.carlosmedina.javareactproject.model.Execution;
import com.carlosmedina.javareactproject.repository.ExecutionJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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

    //private FileUtil fileUtil;

    @GetMapping(value = "/all")
    public List<Execution> findAll() {
        return executionJpaRepository.findAll();
    }

    @RequestMapping(value = "/executeFile",
            headers = "content-type=multipart/*",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> executeInputFile(@RequestParam(value = "file", required = true) MultipartFile multipartFile,
                                                HttpServletResponse httpServletResponse) throws IOException {

        Map<String, String> response = new HashMap<>();

        try {
            FileUtil.init(multipartFile);
            ProcessFile processFile = new ProcessFile(multipartFile);
            if (processFile.validateFileResponse().get("status").equalsIgnoreCase("error")) {
                return processFile.validateFileResponse();
            }
            processFile.processInputFile();
            //this.load();
            this.downloadOutputFile(httpServletResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    @RequestMapping(value = "/load", method = RequestMethod.POST)
    private void load(@RequestBody final Execution execution) {
        executionJpaRepository.save(execution);
    }

    public void downloadOutputFile(HttpServletResponse response) throws IOException {
        File file = new File(FileUtil.DIRECTORY + FileUtil.getFilename("OUTPUT"));

        response.setContentType("text/plain");
        response.setHeader("Content-Disposition", "attachment;filename=" + file.getName());
        BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(file));
        BufferedOutputStream outStream = new BufferedOutputStream(response.getOutputStream());

        byte[] buffer = new byte[1024];
        int bytesRead = 0;
        while ((bytesRead = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }
        outStream.flush();
        inStream.close();
    }

}
