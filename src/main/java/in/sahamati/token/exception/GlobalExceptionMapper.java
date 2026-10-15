package in.sahamati.token.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Throwable exception) {
        LOG.error("Unhandled exception", exception);
        ErrorPayload payload = new ErrorPayload();
        payload.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        payload.setError("Internal Server Error");
        payload.setMessage(exception.getMessage() != null ? exception.getMessage() : "Unexpected error");
        payload.setPath(""); // path not available here; could be injected if needed
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(payload)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
