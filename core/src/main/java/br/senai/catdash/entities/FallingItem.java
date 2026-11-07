package br.senai.catdash.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import br.senai.catdash.screens.GameScreen;

/**
 * Classe abstrata base para todos os itens que caem do topo da tela.
 * Gerencia a posição, velocidade de queda, textura e hitbox retangular.
 */
public abstract class FallingItem {
    protected Rectangle bounds;
    protected float speed;
    protected Texture texture;

    /**
     * Construtor da entidade FallingItem.
     *
     * @param x       Posição X inicial.
     * @param y       Posição Y inicial.
     * @param width   Largura do item.
     * @param height  Altura do item.
     * @param speed   Velocidade vertical de queda (pixels por segundo).
     * @param texture Textura utilizada para desenhar o item.
     */
    public FallingItem(float x, float y, float width, float height, float speed, Texture texture) {
        this.bounds = new Rectangle(x, y, width, height);
        this.speed = speed;
        this.texture = texture;
    }

    /**
     * Atualiza a posição do item de acordo com a velocidade de queda e o tempo delta.
     *
     * @param delta Tempo decorrido desde o último frame (em segundos).
     */
    public void update(float delta) {
        bounds.y -= speed * delta;
    }

    /**
     * Renderiza o item na tela através do SpriteBatch fornecido.
     *
     * @param batch SpriteBatch ativo para desenho.
     */
    public void render(SpriteBatch batch) {
        batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    /**
     * Verifica se o item saiu totalmente pela parte inferior da tela.
     *
     * @return true se o item estiver abaixo da tela; false caso contrário.
     */
    public boolean isOffScreen() {
        return bounds.y + bounds.height < 0;
    }

    /**
     * Obtém o retângulo delimitador (hitbox) para detecção de colisão.
     *
     * @return Retângulo de colisão do item.
     */
    public Rectangle getBounds() {
        return bounds;
    }

    /**
     * Método abstrato chamado quando o gato colide com este item.
     * Cada subclasse define o seu efeito específico (polimorfismo estrito).
     *
     * @param cat        Instância do gato do jogador.
     * @param gameScreen Instância da tela principal de jogo.
     */
    public abstract void onCollect(Cat cat, GameScreen gameScreen);
}

