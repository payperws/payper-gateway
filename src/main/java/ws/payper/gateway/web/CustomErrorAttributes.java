package ws.payper.gateway.web;

import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import ws.payper.gateway.service.ValidationException;

import java.util.Map;

@Component
public class CustomErrorAttributes<T extends Throwable> extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request,
                                                  boolean includeStackTrace) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(request, includeStackTrace);
        addErrorDetails(errorAttributes, request);
        return errorAttributes;
    }

    private void addErrorDetails(Map<String, Object> errorAttributes, ServerRequest request) {
        Throwable ex = getError(request);

        if (ex instanceof ValidationException) {
            errorAttributes.put("error", ex.getMessage());
            errorAttributes.put("status", 400);
            errorAttributes.put("details", ((ValidationException) ex).getDetails());
        }
    }
}
