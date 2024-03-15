package app.exception;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.server.exceptions.response.ErrorContext;
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor;
import io.micronaut.http.server.exceptions.response.HateoasErrorResponseProcessor;
import jakarta.inject.Singleton;
import java.util.Optional;


@Singleton
public class Libre311ErrorResponseProcessor implements ErrorResponseProcessor<JsonError> {

    final HateoasErrorResponseProcessor hateoasErrorResponseProcessor;

    public Libre311ErrorResponseProcessor(
        HateoasErrorResponseProcessor hateoasErrorResponseProcessor) {
        this.hateoasErrorResponseProcessor = hateoasErrorResponseProcessor;
    }

    @Override
    @NonNull
    public MutableHttpResponse<JsonError> processResponse(@NonNull ErrorContext errorContext,
        @NonNull MutableHttpResponse<?> baseResponse) {
        MutableHttpResponse<JsonError> res = hateoasErrorResponseProcessor.processResponse(
            errorContext, baseResponse);
        addLogrefToJsonError(res, errorContext);

        return res;
    }

    void addLogrefToJsonError(MutableHttpResponse<JsonError> res, ErrorContext errorContext){
        Optional<JsonError> maybeJsonError = res.getBody();
        Optional<Throwable> maybeRootCause = errorContext.getRootCause();
        if (maybeJsonError.isPresent() && maybeRootCause.isPresent()
            && maybeRootCause.get() instanceof Libre311BaseException) {
            Libre311BaseException libre311BaseException = (Libre311BaseException) maybeRootCause.get();
            maybeJsonError.get().logref(libre311BaseException.getLogref());
        }
    }
}
