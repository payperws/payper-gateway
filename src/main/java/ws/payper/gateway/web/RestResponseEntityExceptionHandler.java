package ws.payper.gateway.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ws.payper.gateway.service.ValidationException;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = { ValidationException.class })
    protected ResponseEntity<Object> validation(ValidationException ex, WebRequest request) {

        String bodyOfResponse = "This should be application specific";
        return handleExceptionInternal(ex, ex.getDetails(),
                new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }
}