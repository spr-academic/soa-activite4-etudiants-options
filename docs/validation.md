# Validation — Activité 4

**Date : 31 octobre 2026**

**Résultat : compilation réussie, WAR construit, 22 tests réussis, 0 échec, 0 erreur, 0 test ignoré.**

## Environnement

- Java : 21.0.13 (Eclipse Adoptium).
- Maven : 3.9.11.
- Jersey : 2.27 ; Jetty : 9.4.57.v20241219.
- Commande : `mvn clean verify`, avec JDK 21 sélectionné.

## Méthode

Les tests déploient le **WAR construit par Maven** dans un serveur Jetty local, sur un port disponible choisi automatiquement. Ils utilisent de vraies requêtes HTTP et vérifient les réponses. Les ressources REST ne sont pas simulées.

L’application est activée automatiquement par `utilities.RestActivator` et `@ApplicationPath("rest")`. Aucun mappage manuel `/rest` n’est ajouté dans les tests pour contourner cette activation.

## Vérifications couvertes

| Domaine | Vérifications |
|---|---|
| Options | Création, liste, filtre avec accents, lecture, modification et suppression |
| Étudiants | Création, liste, lecture, modification, réaffectation et suppression |
| Statuts | `200`, `204` sans corps, `404` pour les ressources absentes |
| Affectation | Option réellement enregistrée ; rejet d’une option inconnue |
| Persistance en mémoire | Création conservée après plusieurs requêtes et constructions des classes métier |
| Cohérence | Modification d’une option visible dans les étudiants affectés ; rejet des doublons |
| JSON | Paramètres et champs attendus, dont l’objet `option` dans les étudiants |
| XML | Racine `etudiants`, éléments `etudiant`, champs attendus et absence de l’objet `option` |
| Cas limites | Liste XML vide pour une option existante sans étudiants ; option absente : `404` |
| Erreurs de requête | JSON invalide, corps absent/incomplet, identité incohérente : `400` |
| Négociation du format | `406` et `415` pour les formats non acceptés |

Le [rapport XML](rapport-tests.xml) conserve les résultats individuels des 22 tests, extraits du rapport Maven Failsafe. Les propriétés de l’environnement et les journaux techniques ont été retirés de cette copie pour faciliter sa lecture.

## Reproduire

```bash
mvn clean verify
```

Les rapports complets sont régénérés dans `target/failsafe-reports/`. Le serveur temporaire est arrêté automatiquement à la fin des tests. Les données modifiées pendant ces tests n’affectent pas une autre instance de l’application.
