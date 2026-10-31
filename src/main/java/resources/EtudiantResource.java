package resources;

import entities.Etudiant;
import entities.Option;
import metiers.EtudiantBusiness;
import metiers.OptionBusiness;
import dto.EtudiantsXml;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("etudiants")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EtudiantResource {
    private final EtudiantBusiness business = new EtudiantBusiness();
    private final OptionBusiness options = new OptionBusiness();

    @POST
    public Response create(Etudiant etudiant) {
        requireValidBody(etudiant);
        return business.addEtudiant(etudiant)
                ? Response.ok(etudiant).build() : Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    public List<Etudiant> list() {
        return business.getAllEtudiants();
    }

    @GET
    @Path("{identifiant}")
    public Etudiant get(@PathParam("identifiant") String identifiant) {
        Etudiant etudiant = business.getEtudiantByIdentifiant(identifiant);
        if (etudiant == null) throw new NotFoundException();
        return etudiant;
    }

    @PUT
    @Path("{identifiant}")
    public Response update(@PathParam("identifiant") String identifiant, Etudiant etudiant) {
        requireValidBody(etudiant);
        if (!identifiant.equals(etudiant.getIdentifiant())) throw new BadRequestException("L'identifiant du corps doit correspondre à l'URI.");
        return business.updateEtudiant(identifiant, etudiant)
                ? Response.ok(etudiant).build() : Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("{identifiant}")
    public Response delete(@PathParam("identifiant") String identifiant) {
        return business.deleteEtudiant(identifiant)
                ? Response.noContent().build() : Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("option")
    @Produces(MediaType.APPLICATION_XML)
    public EtudiantsXml byOption(@QueryParam("codeOption") Integer codeOption) {
        if (codeOption == null) throw new BadRequestException("Le paramètre codeOption est requis.");
        Option option = options.getOptionByCode(codeOption);
        if (option == null) throw new NotFoundException();
        return new EtudiantsXml(business.getEtudiantsByOption(option));
    }

    private static void requireValidBody(Etudiant etudiant) {
        if (etudiant == null || etudiant.getIdentifiant() == null || etudiant.getIdentifiant().trim().isEmpty()
                || etudiant.getOption() == null) {
            throw new BadRequestException("L'identifiant et l'option sont requis.");
        }
    }
}
