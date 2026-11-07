package br.senai.catdash;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import br.senai.catdash.screens.MainMenuScreen;

/**
 * Classe principal do jogo "Cat Dash".
 * Estende {@link Game} e atua como o ponto central da aplicação, gerenciando o ciclo de vida,
 * a câmera ortográfica fixa em 800x480, o {@link SpriteBatch}, a fonte {@link BitmapFont} com anti-aliasing linear,
 * as texturas PNG carregadas da pasta assets, o sistema de persistência de Recorde Máximo (Preferences)
 * e a transição entre telas (Screens).
 */
public class CatDashGame extends Game {
    // Resolução virtual fixa do jogo
    public static final float VIRTUAL_WIDTH = 800f;
    public static final float VIRTUAL_HEIGHT = 480f;

    // Recursos gráficos compartilhados
    public SpriteBatch batch;
    public OrthographicCamera camera;
    public BitmapFont font;

    // Texturas carregadas dos arquivos PNG na pasta assets
    public Texture catTexture;
    public Texture fishTexture;
    public Texture energyDrinkTexture;
    public Texture waterDropTexture;

    // Sistema de persistência de dados (Preferences)
    private Preferences preferences;

    @Override
    public void create() {
        // Inicialização do pipeline de renderização 2D
        batch = new SpriteBatch();

        // Configuração da câmera ortográfica fixa em 800x480
        camera = new OrthographicCamera();
        camera.setToOrtho(false, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);

        // Fonte padrão para renderização de textos e HUD com filtro Linear (anti-aliasing)
        font = new BitmapFont();
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // Carregamento das texturas diretamente dos arquivos PNG na pasta assets
        catTexture = new Texture(Gdx.files.internal("cat.png"));
        fishTexture = new Texture(Gdx.files.internal("fish.png"));
        energyDrinkTexture = new Texture(Gdx.files.internal("energy.png"));
        waterDropTexture = new Texture(Gdx.files.internal("water.png"));

        // Aplicação de filtro bilinear para manter as texturas suaves em qualquer escala
        catTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        fishTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        energyDrinkTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        waterDropTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // Inicialização do sistema de persistência de preferências
        preferences = Gdx.app.getPreferences("CatDashPreferences");

        // Define a tela inicial do jogo
        setScreen(new MainMenuScreen(this));
    }

    /**
     * Retorna a instância de Preferences do jogo (com verificação de inicialização).
     */
    private Preferences getPrefs() {
        if (preferences == null) {
            preferences = Gdx.app.getPreferences("CatDashPreferences");
        }
        return preferences;
    }

    /**
     * Retorna o recorde máximo salvo nas preferências do jogo (chave "recorde").
     *
     * @return Pontuação recorde atual salva (ou 0 caso não exista registro anterior).
     */
    public int getHighScore() {
        return getPrefs().getInteger("recorde", 0);
    }

    /**
     * Salva a nova pontuação nas preferências caso seja estritamente maior que o recorde atual,
     * persistindo os dados em disco imediatamente através do método flush().
     *
     * @param score Pontuação obtida pelo jogador na partida.
     */
    public void setHighScore(int score) {
        if (score > getHighScore()) {
            getPrefs().putInteger("recorde", score);
            getPrefs().flush();
        }
    }

    @Override
    public void render() {
        // Delega o loop de atualização e desenho para a Screen atualmente ativa
        super.render();
    }

    @Override
    public void dispose() {
        // Liberação de memória de todos os recursos carregados (boas práticas libGDX)
        super.dispose();

        if (batch != null) {
            batch.dispose();
        }
        if (font != null) {
            font.dispose();
        }
        if (catTexture != null) {
            catTexture.dispose();
        }
        if (fishTexture != null) {
            fishTexture.dispose();
        }
        if (energyDrinkTexture != null) {
            energyDrinkTexture.dispose();
        }
        if (waterDropTexture != null) {
            waterDropTexture.dispose();
        }
    }
}
