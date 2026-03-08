# Optimistic Locking with JPA & Hibernate

##  Description
Ce projet démontre l'utilisation du **Optimistic Locking** avec **JPA et Hibernate** pour gérer la concurrence lors des mises à jour dans une base de données.

L’Optimistic Locking permet d’éviter que plusieurs utilisateurs modifient la même donnée en même temps sans contrôle.  
Il utilise généralement un champ `@Version` dans les entités pour détecter les conflits de mise à jour. :contentReference[oaicite:0]{index=0}

Lorsqu'une entité est modifiée :
- Hibernate vérifie la version actuelle.
- Si la version a changé dans la base de données, une **OptimisticLockException** est déclenchée.
- Cela évite l'écrasement des données modifiées par un autre utilisateur.

---
## Objectif du projet
  Ce projet a pour objectif de :

  --> Comprendre le mécanisme de gestion de concurrence

  --> Implémenter Optimistic Locking avec Hibernate

  --> Tester les conflits de mise à jour dans une base de données

---

##  Technologies utilisées

- Java
- JPA (Jakarta Persistence API)
- Hibernate
- MySQL
- Maven
- IntelliJ IDEA

---

##  Architecture du projet

Le projet suit une **architecture en couches** pour séparer les responsabilités.

## Video Demo



https://github.com/user-attachments/assets/203f265c-36a3-4768-8c70-2a238a8a8bbb



## Conclusion

Ce projet illustre l'utilisation du Optimistic Locking avec JPA et Hibernate pour garantir l’intégrité des données dans un environnement concurrent.
Grâce au champ @Version, Hibernate peut détecter automatiquement les conflits de mise à jour et empêcher la perte de données lorsque plusieurs transactions tentent de modifier la même entité simultanément.
Cette approche est particulièrement utile dans les applications multi-utilisateurs où plusieurs opérations peuvent être exécutées sur les mêmes données.

