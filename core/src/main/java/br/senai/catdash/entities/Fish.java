package br.senai.catdash.entities;

import com.badlogic.gdx.graphics.Texture;
import br.senai.catdash.screens.GameScreen;

/**
 * Entidade que representa o peixe que cai do topo da tela.
 * Ao ser coletado pelo gato, concede +10 pontos ao jogador.
 */
public class Fish extends FallingItem {
    public static final float SIZE = 48f;
    public static final int SCORE_VALUE = 10;

    /**
     * Construtor do Peixe.
     *
     * @param x       Posição horizontal de spawn.
     * @param y       Posição vertical de spawn (topo da tela).
     * @param speed   Velocidade de queda em pixels/s.
     * @param texture Textura do peixe carregada a partir do arquivo PNG.
     */
    public Fish(float x, float y, float speed, Texture texture) {
        super(x, y, SIZE, SIZE, speed, texture);
    }

    @Override
    public void onCollect(Cat cat, GameScreen gameScreen) {
        // Concede 10 pontos ao jogador
        gameScreen.addScore(SCORE_VALUE);
    }
}

