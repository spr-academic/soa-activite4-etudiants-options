package integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Tests HTTP sur le WAR réellement construit, sans simuler les ressources REST. */
class RestApiIT {
    private static Server server;
    private static String base;
    private static final ObjectMapper JSON = new ObjectMapper();

    @BeforeAll
    static void deployWar() throws Exception {
        File war = new File(System.getProperty("warFile"));
        assertTrue(war.isFile(), "Le WAR doit être construit avant les tests d'intégration.");
        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setHost("127.0.0.1");
        connector.setPort(0);
        server.addConnector(connector);
        Configuration.ClassList.setServerDefault(server).addBefore(
                "org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
                "org.eclipse.jetty.annotations.AnnotationConfiguration");
        WebAppContext app = new WebAppContext();
        app.setContextPath("/soa-activite4-etudiants-options");
        app.setWar(war.getAbsolutePath());
        app.setTempDirectory(Files.createTempDirectory("soa-activite4-war-").toFile());
        app.setPersistTempDirectory(false);
        app.setThrowUnavailableOnStartupException(true);
        server.setHandler(app);
        server.start();
        base = "http://127.0.0.1:" + connector.getLocalPort() + app.getContextPath() + "/rest";
    }

    @AfterAll
    static void stopServer() throws Exception {
        if (server != null) { server.stop(); server.destroy(); }
    }

    @Test void optionsListIsJsonAndContainsSuppliedSeeds() throws Exception {
        Reply r = call("GET", "/options", null);
        assertEquals(200, r.status);
        assertTrue(r.type.startsWith("application/json"));
        assertTrue(r.json().isArray());
        List<Integer> codes = new ArrayList<>();
        r.json().forEach(o -> codes.add(o.get("codeOption").asInt()));
        for (int i = 1; i <= 5; i++) assertTrue(codes.contains(i));
    }

    @Test void optionsFilterHandlesAccentedDomainAndUnknownDomain() throws Exception {
        Reply r = call("GET", "/options?domaine=" + URLEncoder.encode("Mathématiques", "UTF-8"), null);
        assertEquals(200, r.status);
        assertEquals(1, r.json().size());
        assertEquals(2, r.json().get(0).get("codeOption").asInt());
        assertEquals(0, call("GET", "/options?domaine=Inconnu", null).json().size());
    }

    @Test void getOptionReturnsAllFields() throws Exception {
        Reply r = call("GET", "/options/1", null);
        assertEquals(200, r.status);
        assertEquals("Informatique", r.json().get("libelle").asText());
        assertEquals(7, r.json().size());
    }

    @Test void createOptionSurvivesSubsequentRequestsAndBusinessConstruction() throws Exception {
        assertEquals(200, call("POST", "/options", option(101, "Robotique", 30)).status);
        assertEquals(200, call("GET", "/etudiants", null).status);
        assertEquals("Robotique", call("GET", "/options/101", null).json().get("libelle").asText());
        assertEquals(404, call("POST", "/options", option(101, "Doublon", 30)).status);
        assertEquals("Robotique", call("GET", "/options/101", null).json().get("libelle").asText());
    }

    @Test void updateOptionAlsoUpdatesItsRepresentationInAssignedStudents() throws Exception {
        assertEquals(200, call("POST", "/options", option(102, "Initial", 10)).status);
        assertEquals(200, call("POST", "/etudiants", student("T102", 102, "Nom")).status);
        Reply r = call("PUT", "/options/102", option(102, "Modifiée", 35));
        assertEquals(200, r.status);
        assertEquals(35, call("GET", "/options/102", null).json().get("capacite").asInt());
        assertEquals("Modifiée", call("GET", "/etudiants/T102", null).json().get("option").get("libelle").asText());
    }

    @Test void deleteOptionReturnsEmpty204Then404() throws Exception {
        assertEquals(200, call("POST", "/options", option(103, "Temporaire", 10)).status);
        Reply r = call("DELETE", "/options/103", null);
        assertEquals(204, r.status);
        assertEquals("", r.body);
        assertEquals(404, call("GET", "/options/103", null).status);
        assertEquals(404, call("DELETE", "/options/103", null).status);
    }

    @Test void missingOptionGetAndUpdateReturn404() throws Exception {
        assertEquals(404, call("GET", "/options/9999", null).status);
        assertEquals(404, call("PUT", "/options/9999", option(9999, "Absent", 1)).status);
    }

    @Test void optionIdentityMismatchIsRejectedWithoutChangingState() throws Exception {
        assertEquals(400, call("PUT", "/options/1", option(99, "Erreur", 1)).status);
        assertEquals("Informatique", call("GET", "/options/1", null).json().get("libelle").asText());
    }

    @Test void studentsListContainsNestedOptionInJson() throws Exception {
        Reply r = call("GET", "/etudiants", null);
        assertEquals(200, r.status);
        assertTrue(r.type.startsWith("application/json"));
        assertTrue(r.json().size() >= 3);
        for (JsonNode student : r.json()) assertTrue(student.get("option").isObject());
    }

    @Test void getStudentMatchesPdfExample() throws Exception {
        Reply r = call("GET", "/etudiants/I003", null);
        assertEquals(200, r.status);
        assertEquals("Durand", r.json().get("nom").asText());
        assertEquals("Pierre", r.json().get("prenom").asText());
        assertEquals(2, r.json().get("option").get("codeOption").asInt());
    }

    @Test void createStudentUsesExistingCanonicalOptionAndPersists() throws Exception {
        Reply r = call("POST", "/etudiants", student("T201", 4, "Créé"));
        assertEquals(200, r.status);
        Reply fetched = call("GET", "/etudiants/T201", null);
        assertEquals(200, fetched.status);
        assertEquals("Infographie", fetched.json().get("option").get("libelle").asText());
        assertEquals(404, call("POST", "/etudiants", student("T201", 4, "Doublon")).status);
    }

    @Test void createStudentWithUnknownOptionReturns404WithoutInsertion() throws Exception {
        assertEquals(404, call("POST", "/etudiants", student("T202", 9999, "Absent")).status);
        assertEquals(404, call("GET", "/etudiants/T202", null).status);
    }

    @Test void updateStudentChangesFieldsAndAssignment() throws Exception {
        assertEquals(200, call("POST", "/etudiants", student("T203", 4, "Avant")).status);
        assertEquals(200, call("PUT", "/etudiants/T203", student("T203", 5, "Après")).status);
        JsonNode actual = call("GET", "/etudiants/T203", null).json();
        assertEquals("Après", actual.get("nom").asText());
        assertEquals("Chimie", actual.get("option").get("libelle").asText());
    }

    @Test void updateStudentRejectsUnknownOptionWithoutChangingState() throws Exception {
        assertEquals(200, call("POST", "/etudiants", student("T204", 4, "Stable")).status);
        assertEquals(404, call("PUT", "/etudiants/T204", student("T204", 9999, "Erreur")).status);
        assertEquals("Stable", call("GET", "/etudiants/T204", null).json().get("nom").asText());
    }

    @Test void deleteStudentReturnsEmpty204Then404() throws Exception {
        assertEquals(200, call("POST", "/etudiants", student("T205", 4, "Temporaire")).status);
        Reply r = call("DELETE", "/etudiants/T205", null);
        assertEquals(204, r.status);
        assertEquals("", r.body);
        assertEquals(404, call("GET", "/etudiants/T205", null).status);
        assertEquals(404, call("DELETE", "/etudiants/T205", null).status);
    }

    @Test void missingStudentGetAndUpdateReturn404() throws Exception {
        assertEquals(404, call("GET", "/etudiants/INCONNU", null).status);
        assertEquals(404, call("PUT", "/etudiants/INCONNU", student("INCONNU", 1, "Absent")).status);
    }

    @Test void studentIdentityMismatchIsRejected() throws Exception {
        assertEquals(400, call("PUT", "/etudiants/I001", student("AUTRE", 1, "Erreur")).status);
        assertEquals("Doe", call("GET", "/etudiants/I001", null).json().get("nom").asText());
    }

    @Test void studentsByOptionReturnExactXmlShapeAndCorrectStudents() throws Exception {
        Reply r = call("GET", "/etudiants/option?codeOption=1", null, "application/xml", "application/json");
        assertEquals(200, r.status);
        assertTrue(r.type.startsWith("application/xml"));
        Document xml = parseXml(r.body);
        assertEquals("etudiants", xml.getDocumentElement().getTagName());
        assertEquals(2, xml.getElementsByTagName("etudiant").getLength());
        assertEquals(0, xml.getElementsByTagName("option").getLength());
        assertEquals("I001", xml.getElementsByTagName("identifiant").item(0).getTextContent());
        assertEquals("I002", xml.getElementsByTagName("identifiant").item(1).getTextContent());
        assertEquals("anneeEtude", xml.getElementsByTagName("etudiant").item(0).getFirstChild().getNodeName());
    }

    @Test void studentsByUnknownOptionReturn404AndMissingParameterReturns400() throws Exception {
        assertEquals(404, call("GET", "/etudiants/option?codeOption=9999", null, "application/xml", "application/json").status);
        assertEquals(400, call("GET", "/etudiants/option", null, "application/xml", "application/json").status);
    }

    @Test void existingOptionWithoutStudentsReturnsEmptyXmlList() throws Exception {
        assertEquals(200, call("POST", "/options", option(301, "Vide", 10)).status);
        Reply r = call("GET", "/etudiants/option?codeOption=301", null, "application/xml", "application/json");
        assertEquals(200, r.status);
        assertEquals(0, parseXml(r.body).getElementsByTagName("etudiant").getLength());
    }

    @Test void malformedBodiesAreRejected() throws Exception {
        assertEquals(400, call("POST", "/options", "{").status);
        assertEquals(400, call("POST", "/options", "null").status);
        assertEquals(400, call("POST", "/options", "{}").status);
        assertEquals(400, call("POST", "/etudiants", "{}").status);
        assertEquals(400, call("POST", "/etudiants", "null").status);
    }

    @Test void mediaTypesAreEnforced() throws Exception {
        assertEquals(406, call("GET", "/etudiants/option?codeOption=1", null, "application/json", "application/json").status);
        assertEquals(415, call("POST", "/options", "<option/>", "application/json", "application/xml").status);
    }

    private static String option(int code, String label, int capacity) {
        return "{\"codeOption\":" + code + ",\"libelle\":\"" + label + "\",\"domaine\":\"Test\","
                + "\"responsable\":\"Responsable test\",\"credits\":30,\"semestre\":1,\"capacite\":" + capacity + "}";
    }

    private static String student(String id, int option, String name) {
        return "{\"identifiant\":\"" + id + "\",\"nom\":\"" + name + "\",\"prenom\":\"Test\","
                + "\"option\":{\"codeOption\":" + option + ",\"libelle\":\"Libellé ignoré\"},"
                + "\"anneeEtude\":2023,\"email\":\"test@example.com\"}";
    }

    private static Reply call(String method, String path, String body) throws Exception {
        return call(method, path, body, "application/json", "application/json");
    }

    private static Reply call(String method, String path, String body, String accept, String contentType) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(base + path).openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("Accept", accept);
        if (body != null) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", contentType + "; charset=UTF-8");
            try (OutputStream out = connection.getOutputStream()) { out.write(body.getBytes(StandardCharsets.UTF_8)); }
        }
        int status = connection.getResponseCode();
        String type = connection.getContentType();
        InputStream input = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        if (input != null) {
            try (InputStream stream = input) {
                byte[] buffer = new byte[2048];
                int count;
                while ((count = stream.read(buffer)) != -1) bytes.write(buffer, 0, count);
            }
        }
        connection.disconnect();
        return new Reply(status, type == null ? "" : type, new String(bytes.toByteArray(), StandardCharsets.UTF_8));
    }

    private static Document parseXml(String text) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
    }

    private static class Reply {
        final int status;
        final String type;
        final String body;
        Reply(int status, String type, String body) { this.status = status; this.type = type; this.body = body; }
        JsonNode json() throws Exception { return JSON.readTree(body); }
    }
}
