# el_mon_tribala

el_mon_tribala est un jeu de carte fortement inspiré de Balatro, il s'agit d'un jeu de poker dans lequel on doit atteindre un certain score en jouant des mains de poker.

Projet réalisé dans le cadre d'un cours à l'ESIEE Paris (E3 INFO).

## Description

Le joueur enchaîne des manches (blinds) en jouant des mains de poker (paire, brelan, suite, couleur, etc.) pour atteindre un score cible. Entre chaque blind, une boutique permet d'acheter des planètes qui améliorent les combinaisons. Le but est de battre les 8 antes du jeu.

L'architecture suit un découpage MVC :
- `controller/` : logique de jeu (`GameController`)
- `model/` : état du jeu (`GameState`, `GamePhase`)
- `domain/` : règles métier (cartes, deck, mains, scoring, boutique)
- `view/` : interface console (`ConsoleView`)

## Prérequis

- JDK 25
- Un terminal supportant les codes ANSI (couleurs)

## Lancement

- Telecharger la release la plus récente.
- éxecuter la commande suivante:

Pour lancer avec l'interface graphique:
```
java -jar el_mon_tribala.jar zen
```

Pour lancer dans la console:
```
java -jar el_mon_tribala.jar console
```