package controller;

import java.util.List;

import domain.Card;
import domain.hand.combinations.PlayedHand;
import domain.hand.scoring.Score;

/**
 * Transporte vers la vue les données nécessaires à l'affichage du récap d'une main jouée
 */
public record PlayResult(PlayedHand hand, Score score, List<Card> playedCards) {
}
