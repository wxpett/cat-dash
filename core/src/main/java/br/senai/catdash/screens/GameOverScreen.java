package br.senai.catdash.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import br.senai.catdash.CatDashGame;

/**
 * Tela de fim de jogo (Game Over).
 * Exibida quando o gato colide com uma gota d'água.
 * Apresenta o cenário noturno com chuva (fundo_nivel3.png) como fundo estático,
 * o título "FIM DE JOGO" em destaque, a pontuação final da partida,
 * o Recorde Máximo persistente (resgatado via game.getHighScore())
 * e a instrução arcade piscante "Pressione ESPAÇO para tentar de novo".
 */
public class GameOverScreen implements Screen {
    private final CatDashGame game;
    private final int finalScore;
    private final GlyphLayout layout;
    private final Texture fundoNoite;

    // Variável para acumulação de tempo e controle do efeito pulsante de fade
    private float timeAccumulator = 0f;

    /**
     * Construtor da tela de Game Over.
     *
     * @param game       Referência à classe principal do jogo.
     * @param finalScore Pontuação final acumulada pelo jogador na partida.
     */
    public GameOverScreen(final CatDashGame game, int finalScore) {
        this.game = game;
        this.finalScore = finalScore;
        this.layout = new GlyphLayout();

        // Carrega o fundo estático da noite (clima chuvoso/derrota)
        this.fundoNoite = new Texture(Gdx.files.internal("fundo_nivel3.png"));
        this.fundoNoite.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        // Acumula tempo para o efeito piscante da instrução de reinício
        timeAccumulator += delta;

        // Limpa a tela
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        // Atualiza a câmera e projeta no SpriteBatch
        game.camera.update();
        game.batch.setProjectionMatrix(game.camera.combined);

        game.batch.begin();

        // 1. Desenha o fundo estático da noite
        game.batch.draw(fundoNoite, 0, 0, CatDashGame.VIRTUAL_WIDTH, CatDashGame.VIRTUAL_HEIGHT);

        // 2. Título de Fim de Jogo ("FIM DE JOGO" - texto grande)
        game.font.getData().setScale(3.5f);
        String gameOverText = "FIM DE JOGO";
        layout.setText(game.font, gameOverText);
        float titleX = (CatDashGame.VIRTUAL_WIDTH - layout.width) / 2f;
        float titleY = 390f;
        drawTextWithShadow(gameOverText, titleX, titleY, Color.RED);

        // 3. Pontuação do Jogador ("Sua Pontuacao: [pontos]" - texto médio)
        game.font.getData().setScale(1.8f);
        String scoreText = "Sua Pontuacao: " + finalScore;
        layout.setText(game.font, scoreText);
        float scoreX = (CatDashGame.VIRTUAL_WIDTH - layout.width) / 2f;
        float scoreY = 295f;
        drawTextWithShadow(scoreText, scoreX, scoreY, Color.WHITE);

        // 4. Recorde Atual ("Recorde Atual: [high score]" - texto médio, via game.getHighScore())
        game.font.getData().setScale(1.8f);
        int currentHighScore = game.getHighScore();
        String recordText = "Recorde Atual: " + currentHighScore;
        layout.setText(game.font, recordText);
        float recordX = (CatDashGame.VIRTUAL_WIDTH - layout.width) / 2f;
        float recordY = 235f;
        drawTextWithShadow(recordText, recordX, recordY, Color.GOLD);

        // 4.1. Destaque especial caso o jogador tenha acabado de bater ou igualar o recorde
        if (finalScore >= currentHighScore && finalScore > 0) {
            game.font.getData().setScale(1.2f);
            String newRecordNotice = "PARABENS! NOVO RECORDE ALCANCADO!";
            layout.setText(game.font, newRecordNotice);
            float noticeX = (CatDashGame.VIRTUAL_WIDTH - layout.width) / 2f;
            float noticeY = 180f;
            drawTextWithShadow(newRecordNotice, noticeX, noticeY, Color.YELLOW);
        }

        // 5. Efeito arcade piscante ("Pressione ESPAÇO para tentar de novo" - texto pequeno piscando)
        float blinkAlpha = 0.35f + 0.65f * (MathUtils.sin(timeAccumulator * 4.5f) * 0.5f + 0.5f);
        game.font.getData().setScale(1.2f);
        String restartText = "Pressione ESPACO para tentar de novo";
        layout.setText(game.font, restartText);
        float restartX = (CatDashGame.VIRTUAL_WIDTH - layout.width) / 2f;
        float restartY = 105f;

        // Sombra suave deslocada
        game.font.setColor(0f, 0f, 0f, blinkAlpha * 0.9f);
        game.font.draw(game.batch, restartText, restartX + 2f, restartY - 2f);

        // Texto principal com transparência oscilante
        game.font.setColor(1f, 1f, 1f, blinkAlpha);
        game.font.draw(game.batch, restartText, restartX, restartY);

        // 6. Opção de voltar ao menu principal
        game.font.getData().setScale(0.95f);
        String menuText = "Pressione ESC para voltar ao Menu Principal";
        layout.setText(game.font, menuText);
        float menuX = (CatDashGame.VIRTUAL_WIDTH - layout.width) / 2f;
        float menuY = 55f;
        drawTextWithShadow(menuText, menuX, menuY, Color.LIGHT_GRAY);

        // Restaura as configurações padrão da fonte e batch
        game.font.getData().setScale(1.0f);
        game.font.setColor(Color.WHITE);
        game.batch.setColor(1f, 1f, 1f, 1f);

        game.batch.end();

        // Transição: Reinicia a partida ao pressionar ESPAÇO
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.setScreen(new GameScreen(game));
            dispose();
            return;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            // Retorna ao Menu Principal ao pressionar ESC
            game.setScreen(new MainMenuScreen(game));
            dispose();
            return;
        }
    }

    /**
     * Desenha texto com sombra escura projetada para garantir contraste contra o cenário de fundo.
     */
    private void drawTextWithShadow(String text, float x, float y, Color textColor) {
        game.font.setColor(0f, 0f, 0f, 0.95f);
        game.font.draw(game.batch, text, x + 2f, y - 2f);
        game.font.setColor(textColor);
        game.font.draw(game.batch, text, x, y);
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        // Libera a memória da textura de fundo desta tela
        if (fundoNoite != null) {
            fundoNoite.dispose();
        }
    }
}
