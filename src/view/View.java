package view;

import model.GameState;

public interface View {

    public void renderPlaying(GameState gameState);

    public void renderShop(GameState gameState);

    public void renderGameOver(GameState gameState);

    public void renderVictory(GameState gameState);

    public String getUserInput();

    public void renderInvalidInput();

    public void renderHelp();

}
