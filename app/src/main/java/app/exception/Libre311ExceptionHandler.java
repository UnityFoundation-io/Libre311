package app.exception;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import io.micronaut.http.server.exceptions.response.ErrorContext;
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Produces
@Singleton
@Requires(classes = {Libre311BaseException.class, ExceptionHandler.class})
public class Libre311ExceptionHandler implements
    ExceptionHandler<Libre311BaseException, HttpResponse<?>> {

    private static final Logger LOG = LoggerFactory.getLogger(Libre311ExceptionHandler.class);

    private final ErrorResponseProcessor<JsonError> responseProcessor;

    public Libre311ExceptionHandler(ErrorResponseProcessor<JsonError> responseProcessor) {
        this.responseProcessor = responseProcessor;
    }

    @Override
    public HttpResponse<?> handle(HttpRequest request, Libre311BaseException exception) {
        LOG.error(
            String.format("Libre311Exception" + "\nClass: %s" + "\nLogref: %s" + "\nMessage: %s",
                exception.getClass(), exception.getLogref(), exception.getMessage()));
        exception.printStackTrace();
        return processResponse(ErrorContext.builder(request)
                .cause(exception)
                .errorMessage(exception.getMessage())
                .build(), HttpResponse.status(exception.getStatus()));
    }

    private MutableHttpResponse<JsonError> processResponse(@NonNull ErrorContext errorContext,
                                                           @NonNull MutableHttpResponse<?> baseResponse) {
        MutableHttpResponse<JsonError> res = responseProcessor.processResponse(errorContext, baseResponse);
        addLogrefToJsonError(res, errorContext);
        return res;
    }

    private void addLogrefToJsonError(MutableHttpResponse<JsonError> res, ErrorContext errorContext){
        Optional<JsonError> maybeJsonError = res.getBody();
        Optional<Throwable> maybeRootCause = errorContext.getRootCause();

        if (maybeJsonError.isPresent() && maybeRootCause.isPresent()
                && maybeRootCause.get() instanceof Libre311BaseException libre311BaseException) {
            maybeJsonError.get().logref(libre311BaseException.getLogref());
        }
    }
}
