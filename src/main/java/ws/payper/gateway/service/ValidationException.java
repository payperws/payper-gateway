package ws.payper.gateway.service;

import edu.emory.mathcs.backport.java.util.Arrays;

import java.util.List;

public class ValidationException extends RuntimeException {

    private ValidationErrorDetails details;

    public ValidationException(String message, List<InvalidField> invalidFields) {
        this.details = new ValidationErrorDetails(message, invalidFields);
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

        private List<InvalidField> invalidFields;

        public ValidationErrorDetails(String message, List<InvalidField> invalidFields) {
            this.message = message;
            this.invalidFields = invalidFields;
        }

        public String getMessage() {
            return message;
        }

        public List<InvalidField> getInvalidFields() {
            return invalidFields;
        }
    }

    public static class Builder {

        private String message;

        private List<InvalidField> invalidField;

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder field(String fieldKey, Arg... args) {
            InvalidField field = new InvalidField(fieldKey, Arrays.asList(args));
            this.invalidField.add(field);
            return this;
        }

        public ValidationException build() {
            return new ValidationException(message, invalidField);
        }
    }

    public static class InvalidField {

        private String key;

        private List<Arg> args;

        public InvalidField(String key) {
            this.key = key;
        }

        public InvalidField(String key, String... args) {
            this.key = key;
            this.args = Arrays.asList(args);
        }

        public InvalidField(String key, List<Arg> args) {
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

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }
}
