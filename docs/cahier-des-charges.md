# Cahier des charges — Assistant RH (Phase 1 : MVP)

> Ce document traduit le [MVP](mvp.md) en exigences vérifiables.
> Le modèle de données est défini dans [domain.md](domain.md). L'architecture technique sera décrite dans `architecture.md`.

---

## 1. Contexte et objectif

Les PME de 30 à 250 salariés sans service RH dédié gèrent leurs recrutements avec une boîte mail et un fichier Excel. L'objectif de la Phase 1 est de livrer une application web qui permet de **centraliser les candidatures et suivre chaque recrutement**, sans intelligence artificielle.

---

## 2. Acteurs et rôles

| Acteur | Description | Droits (MVP) |
|--------|-------------|--------------|
| **Administrateur** | Personne qui gère les recrutements de l'entreprise | Tout : offres, candidats, candidatures, utilisateurs de l'entreprise |
| **Membre** *(Should Have)* | Manager ou collègue associé aux recrutements | Consulter offres et candidats, commenter, participer aux entretiens |
| **Candidat** | Personne qui postule | **Aucun accès** à l'application pour le MVP |

**Règle d'isolation :** dès que la connexion (S1) est en place, chaque entreprise (tenant) ne voit que ses propres données. Aucune donnée ne doit être accessible d'une entreprise à l'autre.

**Sans connexion (Must Have seuls)**, l'application fonctionne pour une seule entreprise, sans gestion d'utilisateurs.

---

## 3. Exigences fonctionnelles

Codes de priorité : **M** = Must Have, **S** = Should Have, **C** = Could Have (voir [mvp.md](mvp.md#6-périmètre-fonctionnel)).

### 3.1 Authentification — S1

> Should Have : implémentée après le socle Must Have. Le modèle de données doit néanmoins prévoir le rattachement des données à une entreprise, pour l'ajouter sans refonte.

| ID | Exigence |
|----|----------|
| AUTH-01 | L'utilisateur se connecte avec son email et son mot de passe. |
| AUTH-02 | Les mots de passe sont stockés hachés (algorithme adapté, ex. bcrypt). Jamais en clair. |
| AUTH-03 | Toutes les routes de l'API, hors connexion, exigent un utilisateur authentifié. |
| AUTH-04 | L'utilisateur peut se déconnecter. |
| AUTH-05 | Pour le MVP, les comptes sont créés par l'équipe produit (pas d'inscription publique). |

### 3.2 Offres d'emploi — M1

| ID | Exigence |
|----|----------|
| OFF-01 | Créer une offre avec : intitulé (obligatoire), description, lieu, type de contrat (CDI, CDD, stage, freelance, autre). Seul l'intitulé est obligatoire. |
| OFF-02 | Une offre a un statut : **Brouillon**, **Ouverte** ou **Clôturée**. |
| OFF-03 | Modifier une offre tant qu'elle n'est pas clôturée. |
| OFF-04 | Clôturer une offre ; ses candidatures restent consultables et leur statut reste modifiable. Une offre clôturée peut être rouverte. Une offre ne revient jamais au statut Brouillon. |
| OFF-05 | Lister les offres, filtrables par statut. |
| OFF-06 | Une offre qui a des candidatures ne peut pas être supprimée, seulement clôturée. |

### 3.3 Candidats — M2

| ID | Exigence |
|----|----------|
| CAN-01 | Créer un candidat avec : prénom et nom (obligatoires), email (obligatoire, format valide), téléphone. |
| CAN-02 | L'email d'un candidat est unique au sein d'une entreprise (détection de doublon). |
| CAN-03 | Consulter, modifier et supprimer un candidat. |
| CAN-04 | La suppression d'un candidat supprime ses candidatures (droit à l'effacement). Une confirmation est demandée. |
| CAN-05 | Lister les candidats, du plus récent au plus ancien, avec pagination. |

### 3.4 Candidatures — M3, M4

| ID | Exigence |
|----|----------|
| APP-01 | Associer un candidat existant à une offre au statut Ouverte. Cela crée une candidature. |
| APP-02 | Un candidat ne peut avoir qu'une seule candidature par offre, quel que soit son statut. Un candidat refusé qui se représente sur le même poste voit sa candidature existante rouverte. |
| APP-03 | Un candidat peut postuler à plusieurs offres. |
| APP-04 | Une candidature a un statut parmi : **Nouveau**, **Présélectionné**, **Entretien**, **Offre**, **Embauché**, **Refusé**. |
| APP-05 | Une nouvelle candidature est créée au statut **Nouveau**. |
| APP-06 | Le statut peut être changé librement vers n'importe quel autre (pas de workflow imposé au MVP). **Embauché** et **Refusé** sont des statuts finaux, mais réversibles en cas d'erreur. |
| APP-07 | La date du dernier changement de statut est conservée. L'historique complet des changements n'est pas conservé au MVP. |
| APP-08 | La date de candidature peut être saisie à la création (date du jour par défaut) et corrigée ensuite, pour les candidatures reçues avant leur saisie dans l'outil. Elle ne peut pas être dans le futur. |

### 3.5 Tableau de bord et fiches détail — M5, M6

| ID | Exigence |
|----|----------|
| DSH-01 | Le tableau de bord liste les offres ouvertes avec, pour chacune, le nombre de candidatures par statut. |
| DSH-02 | La fiche d'une offre affiche ses informations et la liste de ses candidats avec leur statut. |
| DSH-03 | La fiche d'un candidat affiche ses informations et la liste de ses candidatures (offre, statut, date). |
| DSH-04 | Depuis la fiche d'une offre, on peut changer le statut d'une candidature en une action. |

### 3.6 Should Have

| ID | Exigence |
|----|----------|
| ITW-01 | Planifier un entretien lié à une candidature : date et heure, type (téléphone, visio, sur site), participants (noms en texte libre). La planification ne modifie pas le statut de la candidature. Aucun nouvel entretien sur une candidature Embauché ou Refusé. |
| ITW-02 | Une fois l'entretien passé, saisir un avis (favorable, réservé, défavorable) et un compte rendu libre. L'avis est obligatoire dès qu'un compte rendu est saisi. Un avis par participant est reporté à l'arrivée de la connexion. |
| COM-01 | Ajouter un commentaire interne horodaté sur une candidature. Les commentaires ne sont jamais visibles par le candidat. L'auteur du commentaire sera enregistré à l'arrivée de la connexion. |
| SRC-01 | Rechercher un candidat par nom, prénom ou email. |
| SRC-02 | Filtrer les candidatures par offre et par statut. |
| USR-01 | Un administrateur peut créer des comptes membres pour son entreprise. |

### 3.7 Could Have

| ID | Exigence |
|----|----------|
| DOC-01 | Joindre un CV (PDF, 5 Mo max) à un candidat et le télécharger. Aucun traitement automatique du contenu. |
| EXP-01 | Exporter en CSV les candidatures d'une offre. |
| NOT-01 | Envoyer un email à l'administrateur la veille d'un entretien. |

---

## 4. Exigences non fonctionnelles

### 4.1 Sécurité

| ID | Exigence |
|----|----------|
| SEC-01 | Communications en HTTPS en production. |
| SEC-02 | Dès la mise en place de la connexion : isolation stricte des données par entreprise, vérifiée à chaque requête côté serveur. |
| SEC-03 | Validation de toutes les entrées côté serveur (formats, tailles, champs obligatoires). |
| SEC-04 | Aucun secret (mot de passe BDD, clés) dans le code source : variables d'environnement. |
| SEC-05 | Messages d'erreur sans fuite d'information technique (pas de stack trace renvoyée au client). |

### 4.2 Protection des données personnelles

L'application traite des données personnelles de candidats (nom, email, téléphone, CV).

| ID | Exigence |
|----|----------|
| RGPD-01 | Conformité à la **loi marocaine 09-08** (déclaration auprès de la CNDP à prévoir avant la commercialisation) et au **RGPD** en cas de clients ou candidats européens. |
| RGPD-02 | Ne collecter que les données nécessaires au recrutement (minimisation). |
| RGPD-03 | Suppression définitive d'un candidat sur demande (voir CAN-04). |
| RGPD-04 | **Développement et démonstrations uniquement avec des données fictives ou synthétiques.** Aucune donnée réelle dans les environnements de test. |
| RGPD-05 | Durée de conservation des candidatures non retenues à définir, avec suppression ou anonymisation au-delà *(post-MVP)*. |

### 4.3 Performance et disponibilité

| ID | Exigence |
|----|----------|
| PERF-01 | Temps de réponse de l'API < 500 ms pour les listes paginées (volume cible : 5 000 candidats par entreprise). |
| PERF-02 | Pagination obligatoire sur toutes les listes. |
| DISP-01 | Pas de haute disponibilité exigée pour le MVP ; sauvegarde quotidienne de la base de données. |

### 4.4 Ergonomie

| ID | Exigence |
|----|----------|
| UX-01 | Interface en français uniquement. Les libellés sont externalisés (pas de texte en dur) pour permettre l'ajout de l'arabe plus tard. |
| UX-02 | Prise en main sans formation : un nouvel utilisateur crée une offre et y ajoute un candidat sans aide. |
| UX-03 | Utilisable sur ordinateur (Chrome, Firefox, Edge, Safari récents). Le mobile n'est pas une cible du MVP mais l'affichage doit rester lisible. |

### 4.5 Qualité et maintenabilité

| ID | Exigence |
|----|----------|
| QUA-01 | API REST documentée automatiquement (OpenAPI / Swagger). |
| QUA-02 | Tests automatisés sur les règles métier (services) et les endpoints principaux. |
| QUA-03 | Le projet démarre en local avec Docker (PostgreSQL) et une seule commande pour le backend. |
| QUA-04 | Migrations de schéma de base de données versionnées avant la mise en production (remplacement de `ddl-auto=update`). |

---

## 5. Contraintes techniques

| Élément | Choix |
|---------|-------|
| Backend | Java 21, Spring Boot 4.1, Maven |
| Base de données | PostgreSQL 17 |
| Frontend | Application web séparée (Next.js ou Vite, choix au Jour 6) |
| Conteneurisation | Docker, `infra/docker-compose.yml` |
| Dépôt | Monorepo Git, branches `main` (stable) et `develop` (intégration) |
| IA | **Aucune dans la Phase 1.** L'architecture doit simplement permettre d'ajouter des modules plus tard. |

---

## 6. Critères d'acceptation de la Phase 1

La Phase 1 est terminée quand :

1. Le scénario de démonstration du [MVP](mvp.md#7-scénario-de-démonstration-fin-de-phase-1) se déroule de bout en bout sans erreur.
2. Toutes les exigences **Must Have** (OFF, CAN, APP, DSH) sont implémentées et testées.
3. Les endpoints sont testables depuis Swagger.
4. Si la connexion (S1) est livrée : un utilisateur d'une entreprise A ne peut accéder à aucune donnée d'une entreprise B (test dédié).
5. L'application démarre en local en suivant uniquement le README.
