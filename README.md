# SOA — Activité 4 : Atelier REST Étudiants et options

**ESPRIT · Année universitaire 2026–2027**

**Étudiant : Sayed Touati**

**Atelier REST n°1 — Mise en place d’un service web RESTful**

Cette application expose la gestion des étudiants et des options sous forme d’une API REST. Elle utilise **JAX-RS (`javax.ws.rs`)**, **Jersey 2.27** et un stockage en mémoire, et se construit en archive **WAR**, conformément à l’énoncé.

**Validation : 22 tests HTTP réussis sur le WAR, aucun échec.** Voir le [compte rendu](docs/validation.md).

## Démarrer le projet

Prérequis : **JDK 21** et **Maven 3.9+**. Le code applicatif est compilé pour Java 8 ; la validation du dépôt est effectuée avec JDK 21. Vérifier le JDK utilisé avec `mvn -version`. Les dépendances sont téléchargées par Maven lors du premier lancement.

```bash
mvn clean verify
mvn jetty:run-war
```

La première commande compile le projet, construit le WAR et exécute les tests HTTP d’intégration. La seconde lance l’application sur le port 8080. Arrêt : `Ctrl+C`.

**URL de base :** `http://localhost:8080/soa-activite4-etudiants-options/rest`

Dans IntelliJ, ouvrir `pom.xml` comme projet Maven, sélectionner JDK 21 pour le projet et Maven, puis utiliser les mêmes objectifs Maven. L’import graphique dans IntelliJ n’est pas une étape exécutée lors de la validation automatisée.

Le fichier `target/soa-activite4-etudiants-options.war` peut aussi être déployé dans un conteneur Servlet compatible avec **`javax.servlet`**, par exemple Tomcat 9. Tomcat 10+ utilise l’espace de noms `jakarta.servlet` et ne correspond pas directement à cette version du cours.

## Organisation

| Emplacement | Rôle |
|---|---|
| [entities/](src/main/java/entities) | Entités `Option` et `Etudiant` avec leurs attributs, constructeurs et accesseurs |
| [metiers/](src/main/java/metiers) | Classes métier fournies, adaptées pour conserver les données entre requêtes |
| [resources/](src/main/java/resources) | Ressources REST `OptionResource` et `EtudiantResource` |
| [RestActivator.java](src/main/java/utilities/RestActivator.java) | Activation JAX-RS avec `@ApplicationPath("rest")` |
| [EtudiantsXml.java](src/main/java/dto/EtudiantsXml.java) | Représentation XML de la liste des étudiants d’une option |
| [requests/](requests/atelier.http) | Exemples de toutes les opérations de l’atelier |
| [RestApiIT.java](src/test/java/integration/RestApiIT.java) | Tests HTTP sur le WAR déployé dans Jetty |
| [Validation](docs/validation.md) | Résultats de la compilation et des tests |

## Contrat REST réalisé

Tous les chemins ci-dessous sont relatifs à l’URL de base. Les créations et modifications reçoivent du **JSON**.

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

## Exemples d’utilisation

Lire les options :

```bash
curl -i http://localhost:8080/soa-activite4-etudiants-options/rest/options
```

Créer une nouvelle option depuis le fichier fourni :

```bash
curl -i -X POST \
  -H 'Content-Type: application/json' \
  --data-binary @requests/option-create.json \
  http://localhost:8080/soa-activite4-etudiants-options/rest/options
```

Récupérer les étudiants de l’option 1 en XML :

```bash
curl -i -H 'Accept: application/xml' \
  'http://localhost:8080/soa-activite4-etudiants-options/rest/etudiants/option?codeOption=1'
```

La structure attendue est `<etudiants><etudiant>…</etudiant></etudiants>`. Chaque étudiant contient `anneeEtude`, `email`, `identifiant`, `nom` et `prenom`. Le champ `option` est absent de cette vue XML, conformément à l’exemple du sujet, mais reste présent dans les réponses JSON.

## Données initiales et décisions d’implémentation

- Les classes métier fournies initialisent **cinq options** (codes 1 à 5) et **trois étudiants** (`I001`, `I002`, `I003`). Ces données sont conservées, même si certains exemples du PDF montrent des listes plus courtes.
- Le code 3 existe déjà dans les classes fournies. Les exemples de création du dépôt utilisent donc le **code 6**, afin de créer une nouvelle option sans doublon.
- Les listes sont initialisées une seule fois au chargement des classes. Leur réinitialisation dans chaque constructeur, présente dans les fichiers de départ, aurait annulé les créations et modifications au fil des requêtes.
- L’affectation d’un étudiant utilise l’option réellement enregistrée, identifiée par son code. Une option inconnue renvoie `404`, à la création comme à la modification.
- Modifier une option conserve la référence utilisée par les étudiants affectés. La suppression reprend le comportement métier fourni, sans suppression en cascade des étudiants ; ceux-ci conservent leur référence à l’objet supprimé. Aucune politique supplémentaire de cascade n’est spécifiée dans le sujet.
- Le stockage est en mémoire : redémarrer l’application rétablit les données initiales. Aucune base de données n’est demandée.

## Éléments de départ

Le dossier reçu contenait l’énoncé PDF, `dependencies.docx`, `OptionBusiness.java` et `EtudiantBusiness.java`. Le projet Maven et les entités annoncés dans le PDF n’étaient pas présents : le `pom.xml` et les deux entités ont donc été reconstitués à partir du diagramme et des exemples.

Les modules Jersey sont alignés sur **2.27**, version majoritaire du document fourni ; le module JSON y était indiqué en 2.25.1. JAXB est ajouté explicitement pour son fonctionnement avec JDK 21. L’activation utilise bien `utilities.RestActivator extends javax.ws.rs.core.Application` et `@ApplicationPath("rest")`.

Les documents pédagogiques originaux sont conservés localement et exclus de Git. Le dépôt contient le code nécessaire à la compilation, les exemples et les tests.
