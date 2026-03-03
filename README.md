### Gestion de stock REST API documentation
v1
OAS 3.1
/v3/api-docs/REST API V1
Cette API permet de gérer les utilisateurs d'un système de gestion. Elle offre diverses fonctionnalités pour administrer les utilisateurs, leur rôle et leur sécurité.

#### Fonctionnalités :
- **Créer des profils pour chaque entreprise**.

- **Une entreprise a un ou plusieurs utilisateurs**

- **Paramétrer les catégories d’articles (produits)**

- **Une entreprise a un ou plusieurs articles (produits)**

- **Une entreprise a un ou plusieurs clients ou utilisateur**

- **Passer des commande clients**
Une commande client a un seul client
Une commande client a un ou plusieurs articles (produits)
Une commande client effectue une sortie de stock pour les articles en question

- **Passer des commandes fournisseurs**
Une commande fournisseur a un seul fournisseur
Une commande fournisseur a un ou plusieurs articles (produits)
Une commande fournisseur effectue une entrée de stock pour les articles en question

- **Effectuer les ventes au magasin**
Une vente a un ou plusieurs articles (produits)
Une vente effectue une sortie de stock pour les articles en question

- **Consulter l’état de stock de chaque entreprise**
Voir la quantité de stock de l’article en temps réel
Effectuer les corrections de stock (mettre à jour le stock)

#### Utilisateurs par défaut :

Une fois l'application lancée, une entreprise peut se faire enregistrer et cela va créer automatiquement un utilisateur avec le role ADMIN qui
pourra se faire authentifier pour avoir accès aus autres fonctionnalités
de l'application.

#### Technologies utilisées :

JAVA17 pour le langage de développement du backend
Spring Boot pour le développement du backend
JPA & Hibernate pour l'interaction avec la base de données
Swagger/OpenAPI pour la documentation de l'API
Spring Security pour la gestion des rôles et de la sécurité
Lombok pour réduire le code boilerplate
Cette API est conçue pour être utilisée par des développeurs souhaitant gérer les utilisateurs dans leurs applications, tout en garantissant une intégration facile grâce à des mécanismes de sécurité avancés.
