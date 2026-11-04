# Notes techniques

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
