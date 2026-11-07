package br.senai.catdash.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import br.senai.catdash.CatDashGame;
import br.senai.catdash.entities.Cat;
import br.senai.catdash.entities.EnergyDrink;
import br.senai.catdash.entities.FallingItem;
import br.senai.catdash.entities.Fish;
import br.senai.catdash.entities.WaterDrop;

/**
 * Tela principal do gameplay onde acontecem:
 * - O controle do gato com física de inércia.
 * - O spawn procedural e temporizado de itens que caem do topo (TimeUtils.nanoTime).
 * - A detecção de colisão via retângulos (Rectangle.overlaps) e gatilhos de áudio.
 * - O sistema de áudio completo (música em loop, efeitos de miado variados, powerup e splash).
 * - A mecânica de pausa completa (congelamento de lógica, pause/resume de música e aviso na tela).
 * - A progressão dinâmica de dificuldade em 3 níveis com crossfade suave de cenários.
 * - A renderização contínua dos fundos, entidades e HUD.
 */
public class GameScreen implements Screen {
    private final CatDashGame game;
    private final GlyphLayout layout;

    // Entidades
    private final Cat cat;
    private final Array<FallingItem> items;

    // Texturas de Cenário / Fundo
    private final Texture fundoDia;
    private final Texture fundoTarde;
    private final Texture fundoNoite;

    // Variáveis de controle da transição suave de fundo (crossfade)
    private Texture fundoAtual;
    private Texture proximoFundo;
    private float fadeAlpha = 0f;
    private boolean emTransicao = false;
    private int nivelAtual = 1;

    // Sistema de Áudio
    private final Music musicaFundo;
    private final Sound somPowerup;
    private final Sound somSplash;
    private final Sound[] somMiados;

    // Sistema de Pausa
    private boolean isPaused = false;
    private boolean firstFrame = true;

    // Estado da partida
    private int score;
    private long lastItemSpawnTime;
    private boolean isGameOver;

    /**
     * Construtor da GameScreen.
     *
     * @param game Referência à classe principal do jogo.
     */
    public GameScreen(final CatDashGame game) {
        this.game = game;
        this.layout = new GlyphLayout();

        // Instancia o gato centralizado na base da tela tocando naturalmente o piso (y = 32)
        float startX = (CatDashGame.VIRTUAL_WIDTH - Cat.WIDTH) / 2f;
        float startY = 32f;
        this.cat = new Cat(startX, startY, game.catTexture);

        // Lista dinâmica para gerenciamento dos itens em queda
        this.items = new Array<>();

        // 1. Carregamento dos cenários de fundo da pasta assets
        this.fundoDia = new Texture(Gdx.files.internal("fundo_nivel1.png"));
        this.fundoTarde = new Texture(Gdx.files.internal("fundo_nivel2.png"));
        this.fundoNoite = new Texture(Gdx.files.internal("fundo_nivel3.png"));

        // Aplicação de filtro linear para renderização nítida
        this.fundoDia.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        this.fundoTarde.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        this.fundoNoite.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // Configuração inicial de cenário
        this.fundoAtual = fundoDia;
        this.proximoFundo = null;
        this.fadeAlpha = 0f;
        this.emTransicao = false;
        this.nivelAtual = 1;

        // 2. Carregamento do Sistema de Áudio com tratamento robusto
        Music m = null;
        try {
            m = Gdx.audio.newMusic(Gdx.files.internal("musica.mp3"));
            m.setLooping(true);
            m.setVolume(0.5f);
        } catch (Throwable t) {
            Gdx.app.error("Audio", "Aviso: Nao foi possivel carregar musica.mp3: " + t.getMessage());
        }
        this.musicaFundo = m;

        Sound sPowerup = null;
        try {
            sPowerup = Gdx.audio.newSound(Gdx.files.internal("powerup.wav"));
        } catch (Throwable t) {
            Gdx.app.error("Audio", "Aviso: Nao foi possivel carregar powerup.wav: " + t.getMessage());
        }
        this.somPowerup = sPowerup;

        Sound sSplash = null;
        try {
            sSplash = Gdx.audio.newSound(Gdx.files.internal("splash.wav"));
        } catch (Throwable t) {
            Gdx.app.error("Audio", "Aviso: Nao foi possivel carregar splash.wav: " + t.getMessage());
        }
        this.somSplash = sSplash;

        this.somMiados = new Sound[5];
        for (int i = 0; i < 5; i++) {
            try {
                this.somMiados[i] = Gdx.audio.newSound(Gdx.files.internal("miado" + (i + 1) + ".wav"));
            } catch (Throwable t) {
                Gdx.app.error("Audio", "Aviso: Nao foi possivel carregar miado" + (i + 1) + ".wav: " + t.getMessage());
                this.somMiados[i] = null;
            }
        }

        // Inicialização do estado de jogo
        this.score = 0;
        this.isGameOver = false;
        this.isPaused = false;
        this.firstFrame = true;
        this.lastItemSpawnTime = TimeUtils.nanoTime();
    }

    @Override
    public void show() {
        // Inicia a música de fundo com loop contínuo
        if (musicaFundo != null) {
            try {
                musicaFundo.play();
            } catch (Throwable t) {
                Gdx.app.error("Audio", "Erro ao iniciar musicaFundo: " + t.getMessage());
            }
        }

        // Dispara o primeiro spawn logo ao iniciar a partida
        spawnItem();
    }

    /**
     * Spawna um novo item no topo da tela com base no nível de dificuldade atual.
     * Utiliza probabilidades para escolher entre Peixe, Gota d'Água e Energético.
     */
    private void spawnItem() {
        // Posição horizontal aleatória no topo da tela com base no tamanho ampliado dos itens
        float spawnX = MathUtils.random(0f, CatDashGame.VIRTUAL_WIDTH - Fish.SIZE);
        float spawnY = CatDashGame.VIRTUAL_HEIGHT;

        float fallSpeed = getItemFallSpeed(nivelAtual);

        // Sorteio de probabilidade [0.0f a 1.0f]
        float roll = MathUtils.random();

        FallingItem newItem;

        if (nivelAtual == 1) {
            // Nível 1: Peixes (70%), Gotas d'água (20%), Energéticos (10%)
            if (roll < 0.70f) {
                newItem = new Fish(spawnX, spawnY, fallSpeed, game.fishTexture);
            } else if (roll < 0.90f) {
                newItem = new WaterDrop(spawnX, spawnY, fallSpeed, game.waterDropTexture);
            } else {
                newItem = new EnergyDrink(spawnX, spawnY, fallSpeed, game.energyDrinkTexture);
            }
        } else if (nivelAtual == 2) {
            // Nível 2: Chance de gota d'água sobe para 35%, Energéticos 10%, Peixes 55%
            if (roll < 0.55f) {
                newItem = new Fish(spawnX, spawnY, fallSpeed, game.fishTexture);
            } else if (roll < 0.90f) { // 0.90 - 0.55 = 35% de gota d'água
                newItem = new WaterDrop(spawnX, spawnY, fallSpeed, game.waterDropTexture);
            } else {
                newItem = new EnergyDrink(spawnX, spawnY, fallSpeed, game.energyDrinkTexture);
            }
        } else {
            // Nível 3: Itens caem muito rápido, Peixes 50%, Gotas 40%, Energéticos 10%
            if (roll < 0.50f) {
                newItem = new Fish(spawnX, spawnY, fallSpeed, game.fishTexture);
            } else if (roll < 0.90f) { // 40% de gota d'água
                newItem = new WaterDrop(spawnX, spawnY, fallSpeed, game.waterDropTexture);
            } else {
                newItem = new EnergyDrink(spawnX, spawnY, fallSpeed, game.energyDrinkTexture);
            }
        }

        items.add(newItem);
        lastItemSpawnTime = TimeUtils.nanoTime();
    }

    /**
     * Retorna o nível de dificuldade atual.
     *
     * @return Nível de 1 a 3.
     */
    public int getCurrentLevel() {
        return nivelAtual;
    }

    /**
     * Retorna a velocidade de queda dos itens de acordo com o nível atual.
     *
     * @param level Nível atual de dificuldade.
     * @return Velocidade em pixels por segundo.
     */
    private float getItemFallSpeed(int level) {
        switch (level) {
            case 1:
                return 190f; // Queda lenta
            case 2:
                return 320f; // Queda rápida
            case 3:
            default:
                return 470f; // Queda muito rápida
        }
    }

    /**
     * Retorna o intervalo de spawn em nanossegundos para o nível atual:
     * - Nível 1: 1.5s
     * - Nível 2: 1.0s
     * - Nível 3: 0.6s
     *
     * @param level Nível de dificuldade.
     * @return Intervalo em nanossegundos.
     */
    private long getSpawnIntervalNano(int level) {
        switch (level) {
            case 1:
                return 1_500_000_000L; // 1.5s
            case 2:
                return 1_000_000_000L; // 1.0s
            case 3:
            default:
                return 600_000_000L;   // 0.6s
        }
    }

    @Override
    public void render(float delta) {
        // Se a partida foi finalizada neste frame, interrompe a execução
        if (isGameOver) {
            return;
        }

        // Permite voltar ao menu a qualquer momento com a tecla ESC
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (musicaFundo != null) {
                try {
                    musicaFundo.stop();
                } catch (Throwable ignored) {
                }
            }
            game.setScreen(new MainMenuScreen(game));
            dispose();
            return;
        }

        // Ignora ESPAÇO no exato primeiro frame para não herdar o clique do menu
        if (firstFrame) {
            firstFrame = false;
        } else {
            // 1. Controle do Sistema de Pausa (tecla ESPAÇO)
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                isPaused = !isPaused;
                if (isPaused) {
                    if (musicaFundo != null && musicaFundo.isPlaying()) {
                        try {
                            musicaFundo.pause();
                        } catch (Throwable ignored) {
                        }
                    }
                } else {
                    if (musicaFundo != null) {
                        try {
                            musicaFundo.play();
                        } catch (Throwable ignored) {
                        }
                    }
                }
            }
        }

        // 2. Lógica de Atualização (executada somente se o jogo NÃO estiver pausado)
        if (!isPaused) {
            // 2.1. Verificação de progressão de nível e transição de cenário
            if (score >= 100 && nivelAtual == 1) {
                proximoFundo = fundoTarde;
                emTransicao = true;
                nivelAtual = 2;
            } else if (score >= 300 && nivelAtual == 2) {
                proximoFundo = fundoNoite;
                emTransicao = true;
                nivelAtual = 3;
            }

            // 2.2. Atualização da Física do Gato
            cat.update(delta);

            // 2.3. Controle de Spawn Temporizado via TimeUtils.nanoTime()
            long spawnInterval = getSpawnIntervalNano(nivelAtual);
            if (TimeUtils.nanoTime() - lastItemSpawnTime > spawnInterval) {
                spawnItem();
            }

            // 2.4. Atualização dos Itens e Verificação de Colisões
            // Itera de trás para frente para permitir remoção segura de elementos
            for (int i = items.size - 1; i >= 0; i--) {
                FallingItem item = items.get(i);
                item.update(delta);

                // Remove itens que saíram pela base da tela
                if (item.isOffScreen()) {
                    items.removeIndex(i);
                    continue;
                }

                // Verifica colisão entre as hitboxes retangulares do gato e do item
                if (item.getBounds().overlaps(cat.getBounds())) {
                    // Gatilhos dos efeitos sonoros específicos
                    if (item instanceof Fish) {
                        // Escolhe um miado aleatório entre 0 e 4
                        int meowIndex = MathUtils.random(0, 4);
                        if (somMiados[meowIndex] != null) {
                            try {
                                somMiados[meowIndex].play();
                            } catch (Throwable ignored) {
                            }
                        }
                    } else if (item instanceof EnergyDrink) {
                        // Toca o som de powerup
                        if (somPowerup != null) {
                            try {
                                somPowerup.play();
                            } catch (Throwable ignored) {
                            }
                        }
                    } else if (item instanceof WaterDrop) {
                        // Toca o som de splash e para a música
                        if (somSplash != null) {
                            try {
                                somSplash.play();
                            } catch (Throwable ignored) {
                            }
                        }
                        if (musicaFundo != null) {
                            try {
                                musicaFundo.stop();
                            } catch (Throwable ignored) {
                            }
                        }
                    }

                    items.removeIndex(i);
                    item.onCollect(cat, this);

                    // Se colidiu com gota d'água, encerra o ciclo imediatamente
                    if (isGameOver) {
                        return;
                    }
                }
            }

            // 2.5. Atualização da Transição Gradual de Cenário (Crossfade)
            if (emTransicao && proximoFundo != null) {
                fadeAlpha += delta * 0.5f; // Transição suave de ~2 segundos
                fadeAlpha = Math.min(fadeAlpha, 1f);

                // Ao completar a transição (alpha >= 1), atualiza o fundo atual
                if (fadeAlpha >= 1f) {
                    fundoAtual = proximoFundo;
                    proximoFundo = null;
                    emTransicao = false;
                    fadeAlpha = 0f;
                }
            }
        }

        // 3. Renderização (executada de forma contínua, inclusive durante a Pausa)
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        game.camera.update();
        game.batch.setProjectionMatrix(game.camera.combined);

        game.batch.begin();

        // 3.1. Renderização dos Fundos Dinâmicos com Crossfade
        game.batch.draw(fundoAtual, 0, 0, 800, 480);

        if (emTransicao && proximoFundo != null) {
            game.batch.setColor(1, 1, 1, fadeAlpha);
            game.batch.draw(proximoFundo, 0, 0, 800, 480);
            game.batch.setColor(1, 1, 1, 1); // Reseta a cor imediatamente
        }

        // 3.2. Renderização dos Itens em Queda
        for (FallingItem item : items) {
            item.render(game.batch);
        }

        // 3.3. Renderização do Gato
        cat.render(game.batch);

        // 3.4. Renderização do HUD (Interface do Usuário)
        renderHUD(nivelAtual);

        // 3.5. Renderização do Aviso de Jogo Pausado
        if (isPaused) {
            renderPauseOverlay();
        }

        game.batch.end();
    }

    /**
     * Renderiza o aviso centralizado de "JOGO PAUSADO" quando a pausa estiver ativa.
     */
    private void renderPauseOverlay() {
        // Título de Pausa destacado
        game.font.getData().setScale(3.2f);
        String pauseText = "JOGO PAUSADO";
        layout.setText(game.font, pauseText);
        float px = (CatDashGame.VIRTUAL_WIDTH - layout.width) / 2f;
        float py = (CatDashGame.VIRTUAL_HEIGHT / 2f) + 40f;

        // Sombra escura
        game.font.setColor(0f, 0f, 0f, 0.95f);
        game.font.draw(game.batch, pauseText, px + 3f, py - 3f);
        // Texto principal em Dourado
        game.font.setColor(Color.GOLD);
        game.font.draw(game.batch, pauseText, px, py);

        // Subtexto com instrução para retomar o jogo
        game.font.getData().setScale(1.3f);
        String resumeText = "Pressione ESPACO para continuar";
        layout.setText(game.font, resumeText);
        float rx = (CatDashGame.VIRTUAL_WIDTH - layout.width) / 2f;
        float ry = py - 55f;

        game.font.setColor(0f, 0f, 0f, 0.95f);
        game.font.draw(game.batch, resumeText, rx + 2f, ry - 2f);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, resumeText, rx, ry);

        // Restaura a escala padrão
        game.font.getData().setScale(1.0f);
        game.font.setColor(Color.WHITE);
    }

    /**
     * Renderiza as informações em tempo real no topo da tela com sombra para alto contraste.
     *
     * @param currentLevel Nível de dificuldade atual.
     */
    private void renderHUD(int currentLevel) {
        game.font.getData().setScale(1.2f);

        // Pontuação
        drawHUDTextWithShadow("Pontos: " + score, 20f, CatDashGame.VIRTUAL_HEIGHT - 20f, Color.WHITE);

        // Nível de Dificuldade
        Color levelColor;
        if (currentLevel == 1) {
            levelColor = Color.GREEN;
        } else if (currentLevel == 2) {
            levelColor = Color.YELLOW;
        } else {
            levelColor = Color.RED;
        }
        drawHUDTextWithShadow("Nivel: " + currentLevel, 200f, CatDashGame.VIRTUAL_HEIGHT - 20f, levelColor);

        // Indicador de Energético / Super Velocidade Ativa
        if (cat.isBoosted()) {
            String boostText = String.format("SUPER VELOCIDADE! (%.1fs)", cat.getBoostTimer());
            drawHUDTextWithShadow(boostText, 450f, CatDashGame.VIRTUAL_HEIGHT - 20f, Color.YELLOW);
        }

        // Restaura a escala padrão
        game.font.getData().setScale(1.0f);
        game.font.setColor(Color.WHITE);
    }

    /**
     * Desenha texto do HUD com sombra escura para garantir legibilidade contra o cenário.
     */
    private void drawHUDTextWithShadow(String text, float x, float y, Color textColor) {
        game.font.setColor(0f, 0f, 0f, 0.9f);
        game.font.draw(game.batch, text, x + 2f, y - 2f);
        game.font.setColor(textColor);
        game.font.draw(game.batch, text, x, y);
    }

    /**
     * Adiciona pontos à pontuação atual do jogador.
     *
     * @param points Quantidade de pontos a acrescentar (ex: +10 ao pegar peixe).
     */
    public void addScore(int points) {
        this.score += points;
    }

    /**
     * Dispara o Game Over imediato (chamado ao colidir com uma gota d'água).
     */
    public void triggerGameOver() {
        if (!isGameOver) {
            isGameOver = true;
            if (musicaFundo != null) {
                try {
                    musicaFundo.stop();
                } catch (Throwable ignored) {
                }
            }

            // Verificação de Recorde Máximo: se a pontuação for maior que o recorde atual, salva nas preferências
            if (score > game.getHighScore()) {
                game.setHighScore(score);
            }

            game.setScreen(new GameOverScreen(game, score));
            dispose();
        }
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
        // Pausa a música caso a janela do jogo perca o foco
        if (musicaFundo != null && musicaFundo.isPlaying()) {
            try {
                musicaFundo.pause();
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public void resume() {
        // Retoma a música se o jogo não estiver pausado manualmente pelo jogador
        if (musicaFundo != null && !isPaused) {
            try {
                musicaFundo.play();
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public void hide() {
        if (musicaFundo != null) {
            try {
                musicaFundo.stop();
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public void dispose() {
        // Limpa a lista de itens ativos
        items.clear();

        // 1. Libera a memória das texturas de fundo da tela
        if (fundoDia != null) {
            fundoDia.dispose();
        }
        if (fundoTarde != null) {
            fundoTarde.dispose();
        }
        if (fundoNoite != null) {
            fundoNoite.dispose();
        }

        // 2. Libera a memória dos recursos de áudio
        if (musicaFundo != null) {
            try {
                musicaFundo.dispose();
            } catch (Throwable ignored) {
            }
        }
        if (somPowerup != null) {
            try {
                somPowerup.dispose();
            } catch (Throwable ignored) {
            }
        }
        if (somSplash != null) {
            try {
                somSplash.dispose();
            } catch (Throwable ignored) {
            }
        }
        if (somMiados != null) {
            for (Sound som : somMiados) {
                if (som != null) {
                    try {
                        som.dispose();
                    } catch (Throwable ignored) {
                    }
                }
            }
        }
    }
}
