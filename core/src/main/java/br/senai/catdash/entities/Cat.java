package br.senai.catdash.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

/**
 * Entidade que representa o gato controlado pelo jogador.
 * Gerencia sua própria posição, hitbox retangular, textura e simulação
 * de física de inércia sobre um chão escorregadio (com aceleração, velocidade limite e atrito).
 */
public class Cat {
    // Dimensões do gato (ajustadas para proporção com o cenário)
    public static final float WIDTH = 80f;
    public static final float HEIGHT = 80f;

    // Constantes de física de movimento
    private static final float ACCELERATION = 1400f;     // Taxa de aceleração (pixels/s²)
    private static final float FRICTION = 950f;          // Taxa de desaceleração gradual/atrito (pixels/s²)
    private static final float BASE_MAX_SPEED = 380f;    // Velocidade limite normal (pixels/s)
    private static final float BOOSTED_MAX_SPEED = 680f; // Velocidade limite com energético (pixels/s)

    // Limites horizontais da tela de 800px
    private static final float WORLD_WIDTH = 800f;

    private final Rectangle bounds;
    private final Texture texture;

    private float velocityX = 0f;
    private float boostTimer = 0f;

    /**
     * Construtor da entidade Cat.
     *
     * @param startX  Posição X inicial.
     * @param startY  Posição Y inicial.
     * @param texture Textura do gato carregada a partir do arquivo PNG.
     */
    public Cat(float startX, float startY, Texture texture) {
        this.bounds = new Rectangle(startX, startY, WIDTH, HEIGHT);
        this.texture = texture;
    }

    /**
     * Atualiza o estado físico do gato:
     * - Contabiliza o tempo restante do boost de velocidade.
     * - Captura inputs das teclas de seta e A/D.
     * - Aplica aceleração ou desaceleração gradual (inércia do piso escorregadio).
     * - Limita a velocidade máxima e impede que o gato saia das bordas da tela.
     *
     * @param delta Tempo decorrido desde o último frame (em segundos).
     */
    public void update(float delta) {
        // Atualiza temporizador do energético
        if (boostTimer > 0) {
            boostTimer -= delta;
            if (boostTimer < 0) {
                boostTimer = 0;
            }
        }

        // Leitura de comandos do jogador (Setas ou A / D)
        boolean movingLeft = Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A);
        boolean movingRight = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);

        // Aplicação de aceleração ou inércia/atrito
        if (movingLeft && !movingRight) {
            // Acelera para a esquerda
            velocityX -= ACCELERATION * delta;
        } else if (movingRight && !movingLeft) {
            // Acelera para a direita
            velocityX += ACCELERATION * delta;
        } else {
            // Nenhuma tecla (ou ambas) pressionadas: chão escorregadio causa desaceleração gradual
            if (velocityX > 0) {
                velocityX -= FRICTION * delta;
                if (velocityX < 0) velocityX = 0;
            } else if (velocityX < 0) {
                velocityX += FRICTION * delta;
                if (velocityX > 0) velocityX = 0;
            }
        }

        // Determina velocidade limite baseada no efeito do energético
        float maxSpeed = isBoosted() ? BOOSTED_MAX_SPEED : BASE_MAX_SPEED;

        // Limita a velocidade atual entre [-maxSpeed, +maxSpeed]
        velocityX = MathUtils.clamp(velocityX, -maxSpeed, maxSpeed);

        // Atualiza a posição horizontal com base na velocidade
        bounds.x += velocityX * delta;

        // Mantém o gato dentro dos limites da tela (0 a WORLD_WIDTH - WIDTH)
        if (bounds.x < 0) {
            bounds.x = 0;
            velocityX = 0; // Para ao bater na parede
        } else if (bounds.x > WORLD_WIDTH - bounds.width) {
            bounds.x = WORLD_WIDTH - bounds.width;
            velocityX = 0; // Para ao bater na parede
        }
    }

    /**
     * Renderiza o gato na tela.
     *
     * @param batch SpriteBatch ativo.
     */
    public void render(SpriteBatch batch) {
        batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    /**
     * Ativa o boost de velocidade máxima pelo período especificado.
     *
     * @param duration Duração em segundos (ex: 5 segundos).
     */
    public void activateSpeedBoost(float duration) {
        this.boostTimer = duration;
    }

    /**
     * Verifica se o gato está sob efeito do energético.
     *
     * @return true se o boost estiver ativo; false caso contrário.
     */
    public boolean isBoosted() {
        return boostTimer > 0;
    }

    /**
     * Obtém o tempo restante do boost de velocidade.
     *
     * @return Segundos restantes do boost.
     */
    public float getBoostTimer() {
        return boostTimer;
    }

    /**
     * Retorna a hitbox do gato para detecção de colisão.
     *
     * @return Retângulo de colisão do gato.
     */
    public Rectangle getBounds() {
        return bounds;
    }

    /**
     * Retorna a velocidade horizontal atual do gato.
     *
     * @return Velocidade horizontal em pixels por segundo.
     */
    public float getVelocityX() {
        return velocityX;
    }
}

