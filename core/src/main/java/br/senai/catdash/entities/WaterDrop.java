package br.senai.catdash.entities;

import com.badlogic.gdx.graphics.Texture;
import br.senai.catdash.screens.GameScreen;

/**
 * Entidade que representa a gota d'água perigosa que cai do topo da tela.
 * Ao atingir o gato, encerra o jogo imediatamente (Game Over).
 */
public class WaterDrop extends FallingItem {
    public static final float SIZE = 48f;

    /**
     * Construtor da Gota d'Água.
     *
     * @param x       Posição horizontal de spawn.
     * @param y       Posição vertical de spawn (topo da tela).
     * @param speed   Velocidade de queda em pixels/s.
     * @param texture Textura da gota d'água carregada a partir do arquivo PNG.
     */
    public WaterDrop(float x, float y, float speed, Texture texture) {
        super(x, y, SIZE, SIZE, speed, texture);
    }

    @Override
    public void onCollect(Cat cat, GameScreen gameScreen) {
        // Encerra a partida imediatamente com Game Over
        gameScreen.triggerGameOver();
    }
}

