package com.am.marketdata.processor.exception;

public class DataValidationException extends ProcessorException {
    private final String dataType;
    private final String validationError;

    public DataValidationException(String dataSource, String dataType, String validationError) {
        super(dataSource, ProcessorErrorType.VALIDATION_ERROR, String.format("Validation failed for %s: %s", dataType, validationError));
        this.dataType = dataType;
        this.validationError = validationError; 
    }

    public DataValidationException(String dataSource, String dataType, String validationError, Throwable cause) {
        super(dataSource, ProcessorErrorType.VALIDATION_ERROR, String.format("Validation failed for %s: %s", dataType, validationError), cause);
        this.dataType = dataType;
        this.validationError = validationError;
    }

    public String getDataType() {
        return dataType;
    }

    public String getValidationError() {
        return validationError;
    }
}
