package in.sahamati.token.resource;

import in.sahamati.token.dto.TokenResponse;
import in.sahamati.token.service.TokenService;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/member")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MemberTokenResource {

    @Inject
    TokenService tokenService;

    /**
     * Generate (or return existing valid) entity member token for the given memberId.
     * POST /api/v1/member/{memberId}/token
     */
    @POST
    @Path("/{memberId}/token")
    public Response generateToken(@PathParam("memberId") @NotBlank String memberId) {
        TokenResponse tr = tokenService.generateEntityMemberToken(memberId);
        return Response.status(Response.Status.OK).entity(tr).build();
    }
}
