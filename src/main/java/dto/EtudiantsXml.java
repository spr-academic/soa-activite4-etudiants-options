package dto;

import entities.Etudiant;
import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/** Vue XML de la liste : l'option n'est pas répétée, conformément à l'énoncé. */
@XmlRootElement(name = "etudiants")
@XmlAccessorType(XmlAccessType.FIELD)
public class EtudiantsXml {
    @XmlElement(name = "etudiant")
    private List<EtudiantXml> etudiants = new ArrayList<>();

    public EtudiantsXml() { }

    public EtudiantsXml(List<Etudiant> source) {
        for (Etudiant etudiant : source) etudiants.add(new EtudiantXml(etudiant));
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = {"anneeEtude", "email", "identifiant", "nom", "prenom"})
    public static class EtudiantXml {
        private int anneeEtude;
        private String email;
        private String identifiant;
        private String nom;
        private String prenom;

        public EtudiantXml() { }

        public EtudiantXml(Etudiant source) {
            anneeEtude = source.getAnneeEtude();
            email = source.getEmail();
            identifiant = source.getIdentifiant();
            nom = source.getNom();
            prenom = source.getPrenom();
        }
    }
}
