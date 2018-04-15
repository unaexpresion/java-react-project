package com.carlosmedina.javareactproject.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class Execution {

    private Long id;
    private long executorDNI;
    private Date executionDate;
    private String urlFile;
    private String statusProcess;

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getExecutorDNI() {
        return executorDNI;
    }

    public void setExecutorDNI(long executorDNI) {
        this.executorDNI = executorDNI;
    }

    public Date getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(Date executionDate) {
        this.executionDate = executionDate;
    }

    public String getUrlFile() {
        return urlFile;
    }

    public void setUrlFile(String urlFile) {
        this.urlFile = urlFile;
    }

    public String getStatusProcess() {
        return statusProcess;
    }

    public void setStatusProcess(String statusProcess) {
        this.statusProcess = statusProcess;
    }
}
