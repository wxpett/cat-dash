package br.senai.catdash.entities;

import com.badlogic.gdx.graphics.Texture;
import br.senai.catdash.screens.GameScreen;

/**
 * Entidade que representa o energético que cai do topo da tela.
 * Ao ser coletado, ativa o aumento temporário de velocidade limite do gato por 5 segundos.
 */
public class EnergyDrink extends FallingItem {
    public static final float SIZE = 48f;
    public static final float BOOST_DURATION = 5.0f; // Duração de 5 segundos

    /**
     * Construtor do Energético.
     *
     * @param x       Posição horizontal de spawn.
     * @param y       Posição vertical de spawn (topo da tela).
     * @param speed   Velocidade de queda em pixels/s.
     * @param texture Textura da lata de energético carregada a partir do arquivo PNG.
     */
    public EnergyDrink(float x, float y, float speed, Texture texture) {
        super(x, y, SIZE, SIZE, speed, texture);
    }

    @Override
    public void onCollect(Cat cat, GameScreen gameScreen) {
        // Ativa o aumento temporário de velocidade limite do gato por 5 segundos
        cat.activateSpeedBoost(BOOST_DURATION);
    }
}

