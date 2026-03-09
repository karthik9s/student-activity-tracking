package com.college.activitytracker.dto;

import java.util.ArrayList;
import java.util.List;

public class BulkUploadResultDTO {
    
    private int totalRows;
    private int successCount;
    private int errorCount;
    private List<RowError> errors = new ArrayList<>();

    public BulkUploadResultDTO() {
    }

    public BulkUploadResultDTO(int totalRows, int successCount, int errorCount, List<RowError> errors) {
        this.totalRows = totalRows;
        this.successCount = successCount;
        this.errorCount = errorCount;
        this.errors = errors != null ? errors : new ArrayList<>();
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public List<RowError> getErrors() {
        return errors;
    }

    public void setErrors(List<RowError> errors) {
        this.errors = errors;
    }

    public static class RowError {
        private int rowNumber;
        private String rollNumber;
        private List<String> errors;

        public RowError() {
        }

        public RowError(int rowNumber, String rollNumber, List<String> errors) {
            this.rowNumber = rowNumber;
            this.rollNumber = rollNumber;
            this.errors = errors;
        }

        public int getRowNumber() {
            return rowNumber;
        }

        public void setRowNumber(int rowNumber) {
            this.rowNumber = rowNumber;
        }

        public String getRollNumber() {
            return rollNumber;
        }

        public void setRollNumber(String rollNumber) {
            this.rollNumber = rollNumber;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void setErrors(List<String> errors) {
            this.errors = errors;
        }
    }
}
