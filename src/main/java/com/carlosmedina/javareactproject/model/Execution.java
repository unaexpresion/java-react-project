package com.carlosmedina.javareactproject.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class Execution {

    private Long id;
    private int executorDNI;
    private Date executionDate;
    private String executionInput;
    private String executionOutput;

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getExecutorDNI() {
        return executorDNI;
    }

    public void setExecutorDNI(int executorDNI) {
        this.executorDNI = executorDNI;
    }

    public Date getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(Date executionDate) {
        this.executionDate = executionDate;
    }

    public String getExecutionInput() {
        return executionInput;
    }

    public void setExecutionInput(String executionInput) {
        this.executionInput = executionInput;
    }

    public String getExecutionOutput() {
        return executionOutput;
    }

    public void setExecutionOutput(String executionOutput) {
        this.executionOutput = executionOutput;
    }
}
