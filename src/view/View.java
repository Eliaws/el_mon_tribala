package view;

import controller.PlayResult;
import model.GameState;

/**
 * Contrat d'une vue. Une vue sait :
 * <ul>
 *   <li>prendre la main pour la durée de la session via {@link #run()}
 *       (boucle d'événements interne pour une CLI, show de la fenêtre
 *       principale pour une GUI) ;</li>
 *   <li>afficher l'état courant du jeu via les méthodes {@code render*}.</li>
 * </ul>
 *
 * <p>La lecture d'entrée utilisateur n'est pas dans le contrat : elle est
 * spécifique au modèle d'interaction (poll pour la CLI, push par listeners
 * pour la GUI). Chaque implémentation la gère en interne.
 */
public sealed interface View permits ConsoleView {

	void run();

	void render(GameState state);

	void renderHelp();

	void renderInvalidInput(GameState state, String message);

	void renderPlayResult(GameState state, PlayResult result);
}
