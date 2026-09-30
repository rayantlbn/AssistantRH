# MVP — Assistant RH

> Document de référence produit. Il fixe **pour qui**, **quel problème** et **quel périmètre** pour la première version commercialisable.

---

## 1. Résumé en une phrase

**Assistant RH est un outil de suivi des recrutements simple, pensé pour les PME sans service RH dédié, qui remplace le couple « boîte mail + Excel » par un espace unique où l'on centralise les candidatures et suit chaque recrutement jusqu'à l'embauche.**

---

## 2. Cible

### 2.1 Comparaison des segments

Notes de 1 (défavorable) à 5 (favorable).

| Critère                    | PME | Cabinet de recrutement | Recruteur indépendant | Startup |
|----------------------------|:---:|:----------------------:|:---------------------:|:-------:|
| Facilité d'acquisition     |  3  |           2            |           4           |    3    |
| Valeur métier du MVP       |  5  |           2            |           3           |    3    |
| Simplicité technique       |  5  |           2            |           4           |    4    |
| Potentiel commercial       |  4  |           4            |           2           |    3    |
| **Total**                  | **17** |       **10**         |        **13**         | **13**  |

**Lecture :**

- **Cabinet de recrutement** : bon potentiel de revenu, mais les cabinets utilisent déjà des ATS complets (multi-clients, sourcing, facturation). Un MVP simple ne les convaincra pas, et leurs exigences imposeraient un produit lourd dès le départ.
- **Recruteur indépendant** : facile à atteindre et le besoin est simple, mais le panier est faible (1 utilisateur) et la sensibilité au prix est forte.
- **Startup** : souvent outillée tôt (outils gratuits, ATS légers existants) et recrute par vagues : le besoin est réel mais irrégulier.
- **PME** : le problème est concret, fréquent et mal outillé. Un ATS simple couvre l'essentiel du besoin, ce qui colle exactement au périmètre d'un MVP.

### 2.2 Cible retenue

**PME de 30 à 250 salariés, sans service RH dédié**, qui recrute plusieurs fois par an.

- **Utilisateur principal** : la personne qui gère les recrutements « en plus » de son poste (office manager, assistant(e) de direction, responsable administratif, parfois le dirigeant).
- **Utilisateurs secondaires** : les managers qui participent aux entretiens et donnent leur avis.
- **Décideur d'achat** : le dirigeant ou le DAF.
- **Marché de départ** : Maroc (francophone), extensible ensuite.

### 2.3 Hors cible (pour le MVP)

- Entreprises disposant déjà d'un ATS.
- Grands groupes (> 250 salariés) : besoins de workflows, SSO, intégrations SIRH.
- Cabinets de recrutement (multi-clients).

---

## 3. Problème

Dans une PME cible, un recrutement se passe typiquement ainsi :

1. L'offre est publiée (site, LinkedIn, jobboards, bouche-à-oreille).
2. Les CV arrivent **par email**, parfois dans plusieurs boîtes.
3. Quelqu'un recopie les candidats dans un **fichier Excel** (quand il le fait).
4. Les statuts (« à appeler », « entretien », « refusé ») sont mis à jour à la main, souvent en retard.
5. Les avis des managers circulent par email ou oralement et **ne sont pas conservés**.
6. Pour retrouver un CV ou un échange, il faut **fouiller la boîte mail**.

**Conséquences :**

- Temps perdu en saisie et en recherche d'information.
- Candidats oubliés ou recontactés trop tard, qui acceptent une autre offre.
- Pas de vision d'ensemble : « où en est-on sur le poste X ? » exige une réunion.
- Image peu professionnelle auprès des candidats (absence de réponse, relances manquées).
- Les ATS du marché sont perçus comme **trop complexes et trop chers** pour quelques recrutements par an.

---

## 4. Proposition de valeur

> **Un seul endroit pour toutes vos candidatures, et une vision claire de chaque recrutement, sans formation ni complexité.**

| Pour l'utilisateur                                      | Pour le dirigeant                               |
|---------------------------------------------------------|-------------------------------------------------|
| Tous les candidats et CV centralisés                    | Visibilité sur l'avancement de chaque poste     |
| Statut de chaque candidature visible en un coup d'œil   | Recrutements plus rapides (moins de candidats perdus) |
| Plus de ressaisie Excel                                 | Historique conservé (candidats réutilisables)   |
| Prise en main en quelques minutes                        | Image plus professionnelle de l'entreprise      |

**Différenciation vis-à-vis d'un ATS classique :** moins de fonctionnalités, mais les bonnes. La simplicité est l'argument principal, pas un défaut.

---

## 5. Pourquoi un client paierait

> Aucun chiffrage à ce stade : les gains seront mesurés à partir de données réelles (entretiens avec des PME, retours des pilotes).

### 5.1 Argument principal : le gain de temps

Sans outil, la personne qui recrute perd du temps à chaque étape :

- recopier chaque candidature de la boîte mail vers un fichier Excel ;
- rechercher un CV ou un échange précis dans les emails ;
- tenir le fichier à jour (statuts, doublons, erreurs de saisie) ;
- organiser des points « où en est-on ? » faute d'outil partagé ;
- rattraper les candidatures oubliées.

Assistant RH supprime ces tâches : les candidatures sont saisies une fois, au même endroit, et le statut de chaque candidat est visible par tous.

### 5.2 Conséquence directe : des recrutements plus rapides

Le temps gagné et le suivi clair se traduisent naturellement par un **délai de recrutement plus court** :

- les candidats sont relancés à temps et partent moins souvent chez un concurrent ;
- les décisions se prennent plus vite, car chacun voit où en est chaque candidature ;
- le poste reste vacant moins longtemps.

### 5.3 Arguments complémentaires

- Centralisation : fin des CV perdus dans les boîtes mail.
- Simplicité : aucune formation nécessaire.
- Continuité : si la personne qui recrute s'absente ou part, l'historique reste.
- Image : des candidats suivis et recontactés, une entreprise plus professionnelle.

---

## 6. Périmètre fonctionnel

### 6.1 Must Have — indispensable pour la V1

| # | Fonctionnalité | Description |
|---|----------------|-------------|
| M1 | Gestion des offres | Créer, modifier, clôturer une offre d'emploi |
| M2 | Gestion des candidats | Créer, consulter, modifier, supprimer un candidat |
| M3 | Candidatures | Associer un candidat à une ou plusieurs offres |
| M4 | Suivi du recrutement | Statut de chaque candidature (ex. : Nouveau → Présélectionné → Entretien → Offre → Embauché / Refusé) |
| M5 | Tableau de bord | Liste des offres ouvertes avec le nombre de candidatures par statut |
| M6 | Fiches détail | Page détail d'un candidat (infos + candidatures) et d'une offre (liste des candidats) |

### 6.2 Should Have — forte valeur, dès que le socle est stable

| # | Fonctionnalité | Description |
|---|----------------|-------------|
| S1 | Connexion | Authentification email / mot de passe, une entreprise = un espace isolé |
| S2 | Entretiens | Planifier un entretien (date, participants) et saisir un compte rendu |
| S3 | Commentaires internes | Notes et avis des managers sur une candidature |
| S4 | Recherche et filtres | Rechercher un candidat par nom, email ; filtrer par offre et statut |
| S5 | Plusieurs utilisateurs | Inviter des collègues (rôles simples : administrateur / membre). Nécessite S1 |

### 6.3 Could Have — si le temps le permet

| # | Fonctionnalité | Description |
|---|----------------|-------------|
| C1 | Upload de CV | Joindre un CV PDF à un candidat (stockage simple, **sans parsing**) |
| C2 | Export | Export CSV des candidats d'une offre |
| C3 | Notifications | Email lors d'un changement de statut ou d'un entretien à venir |

### 6.4 Hors périmètre du MVP

- Toute fonctionnalité d'IA : OCR, parsing de CV, matching, assistant conversationnel (voir [roadmap.md](roadmap.md), phases 3 à 5).
- Page carrière publique et formulaire de candidature en ligne.
- Multidiffusion des offres sur les jobboards.
- Intégration boîte mail (import automatique des CV).
- Facturation et abonnement en ligne.
- Application mobile.

---

## 7. Scénario de démonstration (fin de Phase 1)

1. **Tableau de bord** : arrivée sur l'application (précédée de la **connexion** si S1 est livrée).
2. **Création d'une offre** : « Comptable confirmé(e) ».
3. **Ajout d'un candidat** : saisie des informations de contact.
4. **Association** du candidat à l'offre.
5. **Suivi** : passage de la candidature de « Nouveau » à « Entretien », visible sur le tableau de bord.

Les données de démonstration sont **exclusivement fictives**.

---

## 8. Indicateurs de succès du MVP

Les valeurs cibles seront fixées une fois les premiers pilotes lancés.

- Temps nécessaire à un nouvel utilisateur pour créer une offre et y ajouter un premier candidat, sans aide.
- Nombre de PME pilotes qui utilisent l'outil sur un vrai recrutement.
- Rétention : part des pilotes toujours actifs après un mois.
- Nombre de pilotes prêts à payer à l'issue du test.

---

## 9. Décisions

| Sujet | Décision |
|-------|----------|
| Connexion | **Should Have.** La démo peut démarrer directement sur le tableau de bord. |
| Argument commercial | **Gain de temps** en argument principal ; le délai de recrutement réduit en découle. |
| Chiffrage des gains | **Aucun pour l'instant.** À établir à partir de données réelles. |
| Modèle de prix | **Reporté**, à définir plus tard. |
| Langue de l'interface | **Français uniquement** pour le MVP. L'arabe pourra être ajouté ensuite : prévoir des libellés externalisés (pas de texte en dur dans l'interface). |
