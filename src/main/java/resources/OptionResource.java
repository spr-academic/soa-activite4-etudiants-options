package resources;

import entities.Option;
import metiers.OptionBusiness;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("options")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OptionResource {
    private final OptionBusiness business = new OptionBusiness();

    @POST
    public Response create(Option option) {
        requireValidBody(option);
        return business.addOption(option)
                ? Response.ok(option).build() : Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    public List<Option> list(@QueryParam("domaine") String domaine) {
        return domaine == null ? business.getListeOptions() : business.getOptionsByDomaine(domaine);
    }

    @GET
    @Path("{code}")
    public Option get(@PathParam("code") int code) {
        Option option = business.getOptionByCode(code);
        if (option == null) throw new NotFoundException();
        return option;
    }

    @PUT
    @Path("{code}")
    public Response update(@PathParam("code") int code, Option option) {
        requireValidBody(option);
        if (option.getCodeOption() != code) throw new BadRequestException("Le code du corps doit correspondre à l'URI.");
        return business.updateOption(code, option)
                ? Response.ok(business.getOptionByCode(code)).build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("{code}")
    public Response delete(@PathParam("code") int code) {
        return business.deleteOption(code)
                ? Response.noContent().build() : Response.status(Response.Status.NOT_FOUND).build();
    }

    private static void requireValidBody(Option option) {
        if (option == null || option.getCodeOption() <= 0) throw new BadRequestException("Un codeOption positif est requis.");
    }
}
