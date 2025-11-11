package com.cris.riverrun.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.cris.riverrun.RiverRunGame;

public class GameScreen implements Screen {

    private static final float VIRTUAL_W = 1280f;
    private static final float VIRTUAL_H = 720f;

    private final RiverRunGame game;
    private final String backgroundFile;

    private OrthographicCamera camera;
    private FitViewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont font;

    // <<< ALTERADO: Gerenciamento de Estado do Jogo
    private enum GameState { PLAYING, DYING, GAME_OVER }
    private GameState currentState = GameState.PLAYING;
    // <<< NOVO: Enum para o tipo de animação de morte
    private enum DeathAnimationType { KRAKEN, ROCK }
    private DeathAnimationType currentDeathAnimation = DeathAnimationType.KRAKEN; // Padrão
    // ---

    private Texture river;
    private Texture[] boatFrames;
    private float frameDuration = 0.1f;
    private float animationTime = 0f;

    private float riverY1, riverY2;
    private Vector2 boatPos;
    private float boatBaseY = 120f;
    private float boatBobTime = 0f;
    private float boatW = 96f;
    private float boatH = 96f;

    private float boatLateralSpeed = 350f;
    private float minScreenX = 50f;
    private float maxScreenX;

    private float cadencePower = 0f;
    private float cadenceAccel = 1.8f;
    private float cadenceDecay = 0.8f;
    private float baseSpeed = 80f;
    private float maxBoost = 140f;
    private float scrollSpeed = 0f;
    private float targetSpeed = 160f;

    private Array<Obstacle> obstacles;
    private Texture rockTexture; // Textura da pedra (agora usada na morte também)
    private Texture[] tentacleFrames;
    private Texture[] krakenFrames;
    private float spawnTimer = 0f;
    private float spawnInterval = 1.5f;
    private boolean isColliding = false;

    // <<< ALTERADO: Variáveis da Animação de Morte
    private float deathAnimationTime = 0f;
    private float krakenDeathFrameDuration = 0.1f;
    private int krakenTotalDeathFrames = 12;
    private float rockDeathZoomDuration = 1.2f; // <<< NOVO: Duração do zoom da pedra (em segundos)
    // ---

    private boolean paused = false;
    private Button btnResume, btnSettings, btnMenu;

    public GameScreen(RiverRunGame game, String backgroundFile) {
        this.game = game;
        this.backgroundFile = backgroundFile;
    }

    @Override
    public void show() {
        // ... (Todo o 'show()' permanece o mesmo, incluindo a correção 'bw/2f') ...
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_W, VIRTUAL_H, camera);
        viewport.apply(true);
        camera.position.set(VIRTUAL_W / 2f, VIRTUAL_H / 2f, 0);
        camera.update();

        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(1.1f);

        river = new Texture(backgroundFile);
        river.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        boatFrames = new Texture[4];
        for (int i = 0; i < 4; i++) {
            boatFrames[i] = new Texture("boat" + (i + 1) + ".png");
        }

        rockTexture = new Texture("rockClear.png"); // Já carregamos a pedra aqui

        tentacleFrames = new Texture[6];
        for (int i = 0; i < 6; i++) {
            tentacleFrames[i] = new Texture("tentacle" + (i + 1) + ".png");
        }

        krakenFrames = new Texture[12];
        for (int i = 0; i < 12; i++) {
            krakenFrames[i] = new Texture("kraken" + (i + 1) + ".png");
        }

        obstacles = new Array<>();
        riverY1 = 0;
        riverY2 = VIRTUAL_H;
        boatPos = new Vector2(VIRTUAL_W / 2f - boatW / 2f, boatBaseY);
        maxScreenX = VIRTUAL_W - boatW - 50f;

        // Botões do PAUSE
        float bw = 380f, bh = 58f, gap = 12f;
        float cx = VIRTUAL_W / 2f;
        float baseY = VIRTUAL_H / 2f + bh;
        btnResume   = new Button(cx - bw/2f, baseY,               bw, bh, "Retomar");
        btnSettings = new Button(cx - bw/2f, baseY - (bh+gap),    bw, bh, "Configurações");
        btnMenu     = new Button(cx - bw/2f, baseY - 2*(bh+gap),  bw, bh, "Voltar ao Menu");

        Gdx.input.setInputProcessor(new InputAdapter() {

            @Override
            public boolean keyDown(int keycode) {
                // 1. Lógica de PAUSA
                if (keycode == Input.Keys.ESCAPE && currentState != GameState.GAME_OVER) {
                    paused = !paused;
                    return true;
                }
                // 2. Lógica de GAME OVER
                if (currentState == GameState.GAME_OVER) {
                    if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                        game.setScreen(new MenuScreen(game));
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean touchDown (int screenX, int screenY, int pointer, int button) {
                if (!paused) return false;
                com.badlogic.gdx.math.Vector3 v = new com.badlogic.gdx.math.Vector3(screenX, screenY, 0);
                camera.unproject(v);

                if (btnResume.hit(v.x, v.y))   { paused = false; return true; }
                if (btnSettings.hit(v.x, v.y)) { /* ... */ return true; }
                if (btnMenu.hit(v.x, v.y))     { game.setScreen(new MenuScreen(game)); return true; }

                return false;
            }
        });
    }


    private void update(float delta) {
        if (paused) return;

        switch (currentState) {
            case PLAYING:
                updatePlaying(delta);
                break;
            case DYING:
                updateDying(delta);
                break;
            case GAME_OVER:
                updateGameOver(delta);
                break;
        }
    }


    private void updatePlaying(float delta) {
        // ... (Movimento do barco e rolagem permanecem os mesmos) ...
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) cadencePower += cadenceAccel * delta;
        else cadencePower -= cadenceDecay * delta;
        cadencePower = MathUtils.clamp(cadencePower, 0f, 1f);
        scrollSpeed = baseSpeed + maxBoost * cadencePower;
        riverY1 -= scrollSpeed * delta;
        riverY2 -= scrollSpeed * delta;
        if (riverY1 <= -VIRTUAL_H) riverY1 = riverY2 + VIRTUAL_H;
        if (riverY2 <= -VIRTUAL_H) riverY2 = riverY1 + VIRTUAL_H;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) boatPos.x -= boatLateralSpeed * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) boatPos.x += boatLateralSpeed * delta;
        boatPos.x = MathUtils.clamp(boatPos.x, minScreenX, maxScreenX);
        boatBobTime += delta * 2f;
        boatPos.y = boatBaseY + MathUtils.sin(boatBobTime) * 4f;
        animationTime += delta;
        spawnTimer -= delta;
        if (spawnTimer <= 0) {
            spawnObstacle();
            spawnInterval = MathUtils.random(1.0f, 2.0f);
            spawnTimer = spawnInterval;
        }

        // --- 3. Movimento e Colisão de Obstáculos ---
        isColliding = false;
        for (int i = obstacles.size - 1; i >= 0; i--) {
            Obstacle o = obstacles.get(i);
            o.update(delta);
            o.pos.y -= scrollSpeed * delta;

            // <<< ALTERADO: Lógica de Colisão agora define o TIPO de morte >>>
            if (checkCollision(o)) {
                isColliding = true;
                if (currentState == GameState.PLAYING) {
                    currentState = GameState.DYING;
                    deathAnimationTime = 0f; // Reseta o timer

                    // Define qual animação de morte usar
                    if (backgroundFile.equals("ClearRiver.png")) {
                        currentDeathAnimation = DeathAnimationType.ROCK;
                    } else { // DarkRiver ou (futuramente) RedRiver
                        currentDeathAnimation = DeathAnimationType.KRAKEN;
                    }

                    return; // Para de processar o updatePlaying
                }
            }
            if (o.pos.y + o.height < 0) {
                obstacles.removeIndex(i);
            }
        }
    }


    // <<< ALTERADO: Verifica a duração da animação correta >>>
    private void updateDying(float delta) {
        deathAnimationTime += delta;

        if (currentDeathAnimation == DeathAnimationType.KRAKEN) {
            float totalAnimationDuration = krakenTotalDeathFrames * krakenDeathFrameDuration;
            if (deathAnimationTime >= totalAnimationDuration) {
                currentState = GameState.GAME_OVER;
            }
        } else if (currentDeathAnimation == DeathAnimationType.ROCK) {
            if (deathAnimationTime >= rockDeathZoomDuration) {
                currentState = GameState.GAME_OVER;
            }
        }
    }


    private void updateGameOver(float delta) {
        // Lógica de input movida para o InputProcessor (keyDown)
    }


    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(river, 0, riverY1, VIRTUAL_W, VIRTUAL_H);
        batch.draw(river, 0, riverY2, VIRTUAL_W, VIRTUAL_H);

        // <<< ALTERADO: O 'render' agora desenha a animação de morte correta >>>
        switch (currentState) {
            case PLAYING:
                for (Obstacle o : obstacles) {
                    batch.draw(o.getTexture(), o.pos.x, o.pos.y, o.width, o.height);
                }
                int frameIndex = (int)(animationTime / frameDuration) % boatFrames.length;
                if (isColliding) batch.setColor(1f, 0.4f, 0.4f, 1f);
                batch.draw(boatFrames[frameIndex], boatPos.x, boatPos.y, boatW, boatH);
                batch.setColor(1f, 1f, 1f, 1f);
                break;

            case DYING:
                // Escolhe qual animação desenhar
                if (currentDeathAnimation == DeathAnimationType.KRAKEN) {
                    drawGiantKrakenAnimation();
                } else if (currentDeathAnimation == DeathAnimationType.ROCK) {
                    drawRockZoomAnimation(); // <<< NOVO
                }
                break;

            case GAME_OVER:
                // Desenha o frame final da animação correta
                if (currentDeathAnimation == DeathAnimationType.KRAKEN) {
                    drawGiantKrakenFrame(krakenTotalDeathFrames - 1);
                } else if (currentDeathAnimation == DeathAnimationType.ROCK) {
                    drawRockZoomFrame(rockDeathZoomDuration); // <<< NOVO (mostra zoom máximo)
                }
                drawGameOverUI();
                break;
        }

        batch.end();

        if (currentState == GameState.PLAYING) {
            drawHUD();
        }

        if (paused) {
            shapes.setProjectionMatrix(camera.combined);
            batch.setProjectionMatrix(camera.combined);
            drawPauseUI();
        }
    }

    // --- Métodos de desenho da Morte do Kraken (permanecem) ---
    private void drawGiantKrakenAnimation() {
        if (krakenFrames == null || krakenFrames.length == 0) return;
        int frameIndex = (int)(deathAnimationTime / krakenDeathFrameDuration) % krakenTotalDeathFrames;
        drawGiantKrakenFrame(frameIndex);
    }

    private void drawGiantKrakenFrame(int frameIndex) {
        if (frameIndex < 0 || frameIndex >= krakenFrames.length) {
            frameIndex = krakenFrames.length - 1;
        }
        Texture frame = krakenFrames[frameIndex];
        float krakenW = 512f;
        float krakenH = 512f;
        float krakenX = (VIRTUAL_W - krakenW) / 2f;
        float krakenY = (VIRTUAL_H - krakenH) / 2f;
        batch.draw(frame, krakenX, krakenY, krakenW, krakenH);
    }

    // <<< NOVO: Métodos de desenho da Morte da Pedra (Zoom) >>>
    private void drawRockZoomAnimation() {
        // Passa o tempo atual para a função de desenho
        drawRockZoomFrame(deathAnimationTime);
    }

    private void drawRockZoomFrame(float time) {
        if (rockTexture == null) return;

        // Calcula o progresso da animação (de 0.0 a 1.0)
        float progress = MathUtils.clamp(time / rockDeathZoomDuration, 0f, 1f);

        // Define o tamanho inicial e final do "zoom"
        float startSize = 50f;  // Tamanho de um obstáculo normal
        float maxSize = 600f;   // Tamanho "grande" na tela

        // Interpola o tamanho atual baseado no progresso
        float currentSize = MathUtils.lerp(startSize, maxSize, progress);

        // Calcula a posição para desenhar centralizado
        float rockX = (VIRTUAL_W - currentSize) / 2f;
        float rockY = (VIRTUAL_H - currentSize) / 2f;

        batch.draw(rockTexture, rockX, rockY, currentSize, currentSize);
    }
    // --- Fim dos novos métodos ---

    private void drawGameOverUI() {
        GlyphLayout gl = new GlyphLayout(font, "GAME OVER");
        font.draw(batch, gl, (VIRTUAL_W - gl.width)/2f, VIRTUAL_H / 2f + gl.height + 20f);
        GlyphLayout gl2 = new GlyphLayout(font, "Pressione ENTER para voltar ao menu");
        font.draw(batch, gl2, (VIRTUAL_W - gl2.width)/2f, VIRTUAL_H / 2f - 20f);
    }

    private void spawnObstacle() {
        // ... (Este método permanece o mesmo) ...
        float x, y, w, h;
        y = VIRTUAL_H;

        if (backgroundFile.equals("ClearRiver.png")) {
            w = MathUtils.random(40f, 60f);
            h = MathUtils.random(40f, 60f);
            x = MathUtils.random(minScreenX, maxScreenX);
            obstacles.add(new Obstacle(rockTexture, x, y, w, h));

        } else if (backgroundFile.equals("DarkRiver.png")) {
            if (MathUtils.randomBoolean()) {
                w = 96f;
                h = 96f;
                x = MathUtils.random(minScreenX, maxScreenX);
                obstacles.add(new Obstacle(krakenFrames, x, y, w, h));
            } else {
                w = 96f;
                h = 96f;
                x = MathUtils.random(minScreenX, maxScreenX);
                obstacles.add(new Obstacle(tentacleFrames, x, y, w, h));
            }
        }
    }

    private boolean checkCollision(Obstacle o) {
        // ... (Este método permanece o mesmo) ...
        return boatPos.x < o.pos.x + o.width &&
            boatPos.x + boatW > o.pos.x &&
            boatPos.y < o.pos.y + o.height &&
            boatPos.y + boatH > o.pos.y;
    }

    private void drawHUD() {
        // ... (Este método permanece o mesmo) ...
        float barW = 160f, barH = 10f, pad = 8f;
        float speedRatio  = MathUtils.clamp((scrollSpeed - baseSpeed) / maxBoost, 0f, 1f);
        float targetRatio = MathUtils.clamp((targetSpeed - baseSpeed) / maxBoost, 0f, 1f);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.1f, 0.1f, 0.1f, 1f);
        shapes.rect(pad, VIRTUAL_H - pad - barH, barW, barH);
        shapes.setColor(0.2f, 0.7f, 1f, 1f);
        shapes.rect(pad, VIRTUAL_H - pad - barH, barW * speedRatio, barH);
        shapes.setColor(1f, 1f, 1f, 1f);
        shapes.rect(pad + barW * targetRatio - 1f, VIRTUAL_H - pad - barH, 2f, barH);
        shapes.end();

        if (isColliding && currentState == GameState.PLAYING) {
            batch.begin();
            GlyphLayout gl = new GlyphLayout(font, "COLISÃO!");
            font.draw(batch, gl, VIRTUAL_W / 2f - gl.width / 2f, VIRTUAL_H - 50f);
            batch.end();
        }
    }

    private void drawPauseUI() {
        // ... (Este método permanece o mesmo) ...
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0, 0, 0, 0.55f);
        shapes.rect(0, 0, VIRTUAL_W, VIRTUAL_H);
        float cardW = 640f, cardH = 360f;
        float cardX = (VIRTUAL_W - cardW)/2f, cardY = (VIRTUAL_H - cardH)/2f;
        shapes.setColor(0.10f, 0.13f, 0.17f, 1f);
        shapes.rect(cardX, cardY, cardW, cardH);
        shapes.setColor(0.18f, 0.22f, 0.28f, 1f);
        shapes.rect(cardX-4, cardY-4, cardW+8, 4);
        shapes.rect(cardX-4, cardY+cardH, cardW+8, 4);
        shapes.rect(cardX-4, cardY, 4, cardH);
        shapes.rect(cardX+cardW, cardY, 4, cardH);
        shapes.end();
        batch.begin();
        GlyphLayout gl = new GlyphLayout(font, "Pausado");
        font.draw(batch, gl, (VIRTUAL_W - gl.width)/2f, cardY + cardH - 18f);
        batch.end();
        drawButton(btnResume);
        drawButton(btnSettings);
        drawButton(btnMenu);
    }

    private void drawButton(Button b) {
        // ... (Este método permanece o mesmo) ...
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0, 0, 0, 0.35f);
        shapes.rect(b.x + 3, b.y - 3, b.w, b.h);
        shapes.setColor(0.12f, 0.16f, 0.20f, 1f);
        shapes.rect(b.x, b.y, b.w, b.h);
        shapes.setColor(1f, 1f, 1f, 0.06f);
        shapes.rect(b.x, b.y + b.h - 4, b.w, 4);
        shapes.end();
        batch.begin();
        GlyphLayout gl = new GlyphLayout(font, b.label);
        font.draw(batch, gl, b.x + (b.w - gl.width)/2f, b.y + (b.h + gl.height)/2f);
        batch.end();
    }

    @Override public void resize(int w, int h) { viewport.update(w, h, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { dispose(); }

    @Override
    public void dispose() {
        // ... (Este método permanece o mesmo) ...
        batch.dispose();
        shapes.dispose();
        river.dispose();
        if (boatFrames != null) {
            for (Texture frame : boatFrames) {
                if (frame != null) frame.dispose();
            }
        }
        rockTexture.dispose();
        if (tentacleFrames != null) {
            for (Texture frame : tentacleFrames) {
                if (frame != null) frame.dispose();
            }
        }
        if (krakenFrames != null) {
            for (Texture frame : krakenFrames) {
                if (frame != null) frame.dispose();
            }
        }
        font.dispose();
    }

    private static class Button {
        // ... (Esta classe permanece a mesma) ...
        float x, y, w, h;
        String label;
        Button(float x, float y, float w, float h, String l) { this.x=x; this.y=y; this.w=w; this.h=h; this.label=l; }
        boolean hit(float px, float py) { return px>=x && px<=x+w && py>=y && py<=y+h; }
    }

    private static class Obstacle {
        // ... (Esta classe permanece a mesma) ...
        Vector2 pos;
        float width, height;
        Texture texture;
        Texture[] frames;
        float animationTime = 0f;
        float frameDuration = 0.15f;

        Obstacle(Texture texture, float x, float y, float w, float h) {
            this.texture = texture;
            this.frames = null;
            this.pos = new Vector2(x, y);
            this.width = w;
            this.height = h;
        }

        Obstacle(Texture[] frames, float x, float y, float w, float h) {
            this.texture = null;
            this.frames = frames;
            this.pos = new Vector2(x, y);
            this.width = w;
            this.height = h;
            this.animationTime = MathUtils.random(0f, frames.length * frameDuration);
        }

        public void update(float delta) {
            if (frames != null) {
                animationTime += delta;
            }
        }

        public Texture getTexture() {
            if (frames != null) {
                int frameIndex = (int)(animationTime / frameDuration) % frames.length;
                return frames[frameIndex];
            } else {
                return texture;
            }
        }
    }
}
