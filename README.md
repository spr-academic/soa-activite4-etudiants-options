# SOA — Activité 4 : API REST Étudiants et options

**Sayed Touati — ESPRIT — Année universitaire 2026–2027**

Atelier REST n°1 : exposer les classes métier de gestion des options et des étudiants avec JAX-RS. Les données sont stockées en mémoire.

## Exécution

Prérequis : **JDK 21** et **Maven 3.9+**. Vérifier le JDK avec `mvn -version`.

```bash
mvn clean verify
mvn jetty:run-war
```

La première commande compile, construit le WAR et exécute les **22 tests HTTP**. La seconde démarre l’application sur le port 8080. Arrêt : `Ctrl+C`.

URL de base : `http://localhost:8080/soa-activite4-etudiants-options/rest`

Dans IntelliJ, ouvrir `pom.xml` comme projet Maven avec JDK 21. Le WAR peut aussi être déployé sur Tomcat 9 (`javax.servlet`).

## Opérations demandées

Les chemins sont relatifs à l’URL de base. Les créations et modifications reçoivent du JSON.

| Partie | Méthode et URI | Résultat | Statuts de l’énoncé |
|---|---|---|---|
| A.1 | `POST /options` | Création d’une option | `200`, `404` |
| A.2 | `GET /options` | Toutes les options en JSON | `200` |
| A.3 | `GET /options?domaine=Mathématiques` | Options du domaine en JSON | `200` |
| A.4 | `DELETE /options/{code}` | Suppression, sans corps de réponse | `204`, `404` |
| A.5 | `PUT /options/{code}` | Modification de l’option | `200`, `404` |
| A.6 | `GET /options/{code}` | Option en JSON | `200`, `404` |
| B.1 | `POST /etudiants` | Création et affectation à une option existante | `200`, `404` |
| B.2 | `GET /etudiants` | Tous les étudiants en JSON | `200` |
| B.3 | `GET /etudiants/{identifiant}` | Étudiant en JSON, option incluse | `200`, `404` |
| B.4 | `DELETE /etudiants/{identifiant}` | Suppression, sans corps de réponse | `204`, `404` |
| B.5 | `PUT /etudiants/{identifiant}` | Modification et éventuelle réaffectation | `200`, `404` |
| B.6 | `GET /etudiants/option?codeOption=1` | Étudiants de l’option en XML | `200`, `404` si l’option n’existe pas |

Les créations réussies renvoient **200**, comme demandé dans le sujet. Une création avec un identifiant déjà utilisé renvoie **404** parmi les statuts prévus ; elle ne crée pas de doublon. Un corps invalide ou une identité différente entre l’URI et le corps renvoie **400**. Un format non pris en charge est rejeté par JAX-RS (`406` ou `415`).

## Démonstration et résultats

Les exemples sont dans [requests/atelier.http](requests/atelier.http), avec les corps JSON dans le même dossier. Ils créent l’option **6** et l’étudiant **I0010**, puis les suppriment. Exécuter les requêtes dans l’ordre sur un service fraîchement démarré.

- [Validation : 22 tests réussis, aucun échec](docs/validation.md).
- [Guide de remise et de démonstration](docs/remise.md).
- [Notes techniques : données initiales, versions et adaptations](docs/notes-techniques.md).

Le code se trouve dans `src/main/java/` : `entities`, `metiers`, `resources`, `utilities` et `dto`. Les tests sont dans `src/test/java/`. Un redémarrage rétablit les données initiales.
