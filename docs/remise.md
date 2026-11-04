# Remise - SOA, Activité 4

**Sayed Touati - ESPRIT - Année universitaire 2026-2027**

**Sujet : Atelier REST n°1, gestion des étudiants et des options**

Dépôt : https://github.com/spr-academic/soa-activite4-etudiants-options

## Contenu

- `src/main/java/` : entités, classes métier, ressources JAX-RS et représentation XML.
- `pom.xml` : configuration Maven et construction du WAR.
- `requests/atelier.http` et fichiers JSON : exemples pour les douze opérations du sujet.
- `src/test/java/integration/RestApiIT.java` : tests HTTP d’intégration.
- `docs/validation.md` et `docs/rapport-tests.xml` : compte rendu et résultats des tests.

## Exécution

1. Ouvrir `pom.xml` comme projet Maven dans IntelliJ et sélectionner JDK 21 pour le projet et Maven.
2. Exécuter `mvn clean verify` depuis la racine du projet.
3. Vérifier le résultat attendu : 22 tests, aucun échec ni erreur.
4. Exécuter `mvn jetty:run-war` pour démarrer le service sur le port 8080.
5. Ouvrir `requests/atelier.http` dans un client HTTP compatible et exécuter les requêtes dans leur ordre d’apparition.

URL de base : `http://localhost:8080/soa-activite4-etudiants-options/rest`.

L’application peut également être déployée avec le WAR construit dans `target/`, sur un conteneur compatible `javax.servlet`, tel que Tomcat 9.

## Démonstration

| Étape | Requêtes | Résultat attendu |
|---|---|---|
| Lire les données initiales | `GET /options`, `GET /etudiants` | `200`, tableaux JSON |
| Filtrer un domaine | `GET /options?domaine=Math%C3%A9matiques` | Option de code 2 |
| Gérer une option | Créer le code 6, modifier, lire puis supprimer | `200`, puis `204` à la suppression |
| Gérer un étudiant | Créer `I0010`, modifier puis supprimer | `200`, puis `204` à la suppression |
| Lire les inscriptions | `GET /etudiants/option?codeOption=1`, avec `Accept: application/xml` | `200`, liste XML sans objet `option` |
| Contrôler une absence | `GET /options/9999` | `404` |

Les données restent disponibles entre les requêtes. Un redémarrage rétablit les cinq options et trois étudiants initiaux. Pour refaire la démonstration, redémarrer le service ou terminer les suppressions prévues.

## Correspondance avec le sujet

Les six opérations sur les options sont définies dans `OptionResource.java` ; les six opérations sur les étudiants dans `EtudiantResource.java`. L’activation utilise `utilities.RestActivator` et `@ApplicationPath("rest")`. Les créations réussies renvoient `200`, conformément aux statuts du sujet.

La dernière validation complète a été réalisée le **4 novembre 2026** : **22 tests réussis, 0 échec, 0 erreur, 0 test ignoré**.
