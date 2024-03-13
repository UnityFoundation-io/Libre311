package app.exception;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import io.micronaut.http.server.exceptions.response.ErrorContext;
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Produces
@Singleton
@Requires(classes = {Libre311BaseException.class, ExceptionHandler.class})
public class Libre311ExceptionHandler implements
    ExceptionHandler<Libre311BaseException, HttpResponse<?>> {

    private static final Logger LOG = LoggerFactory.getLogger(Libre311ExceptionHandler.class);

    private final ErrorResponseProcessor<?> responseProcessor;

    public Libre311ExceptionHandler(Libre311ErrorResponseProcessor responseProcessor) {
        this.responseProcessor = responseProcessor;
    }

    @Override
    public HttpResponse<?> handle(HttpRequest request, Libre311BaseException exception) {
        LOG.error(
            String.format("Libre311Exception" + "\nClass: %s" + "\nLogref: %s" + "\nMessage: %s",
                exception.getClass(), exception.getLogref(), exception.getMessage()));

        return responseProcessor.processResponse(
            ErrorContext.builder(request).cause(exception).errorMessage(exception.getMessage())
                .build(), HttpResponse.status(exception.getStatus()));
    }
}
