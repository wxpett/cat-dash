package br.senai.catdash.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import br.senai.catdash.CatDashGame;

/**
 * Tela inicial do jogo (Menu Principal).
 * Apresenta a arte de capa temática (menu.png), título ampliado com sombra roxa/escura,
 * chamada arcade piscante via função seno, faixa escura semi-transparente para as instruções,
 * legenda de itens em grade 2x2 e créditos do desenvolvedor.
 */
public class MainMenuScreen implements Screen {
    private final CatDashGame game;
    private final GlyphLayout layout;
    private final Texture menuTexture;
    private final Texture overlayTexture;

    // Variável para acumulação de tempo e controle do efeito arcade de fade
    private float timeAccumulator = 0f;

    /**
     * Construtor da tela de menu principal.
     *
     * @param game Referência à classe principal do jogo.
     */
    public MainMenuScreen(final CatDashGame game) {
        this.game = game;
        this.layout = new GlyphLayout();

        // 1. Carrega a arte de capa temática do menu
        this.menuTexture = new Texture(Gdx.files.internal("menu.png"));
        this.menuTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // 2. Cria textura 1x1 preta com 50% de transparência via Pixmap para a faixa inferior
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 0.5f); // Preto com Alpha = 0.5f
        pixmap.fill();
        this.overlayTexture = new Texture(pixmap);
        // Libera imediatamente a memória RAM do Pixmap
        pixmap.dispose();
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        // Acumula tempo para o efeito pulsante de fade
        timeAccumulator += delta;

        // Limpa a tela
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        // Sincroniza a câmera com o SpriteBatch
        game.camera.update();
        game.batch.setProjectionMatrix(game.camera.combined);

        game.batch.begin();

        // 1. Desenha a imagem de fundo temática (menu.png)
        game.batch.draw(menuTexture, 0, 0, CatDashGame.VIRTUAL_WIDTH, CatDashGame.VIRTUAL_HEIGHT);

        // 2. Título do Jogo ("CAT DASH")
        // Escala 4x, sombra escura deslocada (+4, -4) e cor Dourada quente
        game.font.getData().setScale(4.0f);
        String title = "CAT DASH";
        layout.setText(game.font, title);
        float titleX = (CatDashGame.VIRTUAL_WIDTH - layout.width) / 2f;
        float titleY = 445f;

        // Sombra escura (roxa escura/preta) com deslocamento +4 no X e -4 no Y
        game.font.setColor(new Color(0.10f, 0.04f, 0.16f, 0.95f));
        game.font.draw(game.batch, title, titleX + 4f, titleY - 4f);

        // Texto principal em Dourado
        game.font.setColor(Color.GOLD);
        game.font.draw(game.batch, title, titleX, titleY);

        // 3. Efeito Arcade ("Pressione ESPAÇO para jogar")
        // Escala 1.5x com fade in / fade out suave utilizando função seno
        float blinkAlpha = 0.35f + 0.65f * (MathUtils.sin(timeAccumulator * 4.5f) * 0.5f + 0.5f);
        game.font.getData().setScale(1.5f);
        String startInstruction = "Pressione ESPACO para jogar";
        layout.setText(game.font, startInstruction);
        float startX = (CatDashGame.VIRTUAL_WIDTH - layout.width) / 2f;
        float startY = 365f;

        // Sombra suave deslocada
        game.font.setColor(0f, 0f, 0f, blinkAlpha * 0.9f);
        game.font.draw(game.batch, startInstruction, startX + 2f, startY - 2f);

        // Texto pulsante em branco
        game.font.setColor(1f, 1f, 1f, blinkAlpha);
        game.font.draw(game.batch, startInstruction, startX, startY);

        // 4. Faixa Escura para a Legenda e Instruções (Overlay semi-transparente)
        // Cobre a base da tela (altura de 195px) garantindo leitura limpa sobre a arte
        game.batch.draw(overlayTexture, 0, 0, CatDashGame.VIRTUAL_WIDTH, 195f);

        // 4.1. Instruções de Controle e Inércia
        game.font.getData().setScale(1.0f);
        String controls = "Controles: [<-] [->] ou [A] / [D] para mover";
        layout.setText(game.font, controls);
        drawTextWithShadow(controls, (CatDashGame.VIRTUAL_WIDTH - layout.width) / 2f, 175f, Color.WHITE);

        game.font.getData().setScale(0.95f);
        String inertiaNotice = "Cuidado: O chao e escorregadio! Domine a inercia do gato.";
        layout.setText(game.font, inertiaNotice);
        drawTextWithShadow(inertiaNotice, (CatDashGame.VIRTUAL_WIDTH - layout.width) / 2f, 152f, new Color(1f, 0.95f, 0.7f, 1f));

        // 4.2. Legenda dos Itens em Grade 2x2
        float iconSize = 38f;
        float topRowY = 96f;
        float bottomRowY = 46f;
        float col1X = 110f;
        float col2X = 440f;

        game.font.getData().setScale(1.0f);

        // Linha de Cima: Gato e Peixe
        // Gato
        game.batch.draw(game.catTexture, col1X, topRowY - 6f, iconSize, iconSize);
        drawTextWithShadow("Gato (Jogador)", col1X + 48f, topRowY + 20f, Color.WHITE);

        // Peixe
        game.batch.draw(game.fishTexture, col2X, topRowY - 6f, iconSize, iconSize);
        drawTextWithShadow("Peixe (+10 pts)", col2X + 48f, topRowY + 20f, Color.WHITE);

        // Linha de Baixo: Energético e Água
        // Energético
        game.batch.draw(game.energyDrinkTexture, col1X, bottomRowY - 6f, iconSize, iconSize);
        drawTextWithShadow("Energetico (Velocidade)", col1X + 48f, bottomRowY + 20f, Color.WHITE);

        // Água
        game.batch.draw(game.waterDropTexture, col2X, bottomRowY - 6f, iconSize, iconSize);
        drawTextWithShadow("Agua (Game Over)", col2X + 48f, bottomRowY + 20f, Color.WHITE);

        // 5. Créditos do Desenvolvedor no canto inferior direito
        game.font.getData().setScale(0.9f);
        String credits = "Desenvolvido por Kauan da Rosa";
        layout.setText(game.font, credits);
        float creditsX = CatDashGame.VIRTUAL_WIDTH - layout.width - 20f;
        float creditsY = 20f;
        drawTextWithShadow(credits, creditsX, creditsY, Color.WHITE);

        // Restaura as cores e escalas padrão para não afetar outras telas
        game.font.getData().setScale(1.0f);
        game.font.setColor(Color.WHITE);
        game.batch.setColor(1f, 1f, 1f, 1f);

        game.batch.end();

        // Transição para GameScreen quando ESPAÇO for pressionado
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.setScreen(new GameScreen(game));
            dispose();
            return;
        }
    }

    /**
     * Desenha texto com sombra preta projetada para garantir contraste sobre qualquer fundo.
     */
    private void drawTextWithShadow(String text, float x, float y, Color textColor) {
        // Sombra preta deslocada
        game.font.setColor(0f, 0f, 0f, 0.95f);
        game.font.draw(game.batch, text, x + 2f, y - 2f);
        // Texto principal
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
        // Libera a memória das texturas criadas para esta tela
        if (menuTexture != null) {
            menuTexture.dispose();
        }
        if (overlayTexture != null) {
            overlayTexture.dispose();
        }
    }
}
