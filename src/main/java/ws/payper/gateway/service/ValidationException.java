package ws.payper.gateway.service;

import edu.emory.mathcs.backport.java.util.Arrays;

import java.util.ArrayList;
import java.util.List;

public class ValidationException extends RuntimeException {

    private ValidationErrorDetails details;

    public ValidationException(String message, List<Error> errors) {
        this.details = new ValidationErrorDetails(message, errors);
    }

    @Override
    public String getMessage() {
        return details.getMessage();
    }

    public ValidationErrorDetails getDetails() {
        return details;
    }

    public static class ValidationErrorDetails {

        private String message;

        private List<Error> errors;

        public ValidationErrorDetails(String message, List<Error> errors) {
            this.message = message;
            this.errors = errors;
        }

        public String getMessage() {
            return message;
        }

        public List<Error> getErrors() {
            return errors;
        }
    }

    public static class Builder {

        private String message;

        private List<Error> error;

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder field(String fieldKey, Arg... args) {
            Error field = new Error(fieldKey, Arrays.asList(args));
            this.error.add(field);
            return this;
        }

        public ValidationException build() {
            return new ValidationException(message, error);
        }
    }

    public static class Error {

        private String key;

        private List<Arg> args = new ArrayList<>();

        public Error(String key) {
            this.key = key;
        }

        public Error(String key, Arg... args) {
            this.key = key;
            this.args = Arrays.asList(args);
        }

        public Error(String key, List<Arg> args) {
            this.key = key;
            this.args = args;
        }

        public String getKey() {
            return key;
        }

        public List<Arg> getArgs() {
            return args;
        }
    }

    public static class Arg {

        private String key;

        private String value;

        public Arg(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public static Arg of(String key, String value) {
            return new Arg(key, value);
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }
}
