package com.cris.riverrun.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.cris.riverrun.RiverRunGame;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class MenuScreen implements Screen {

    private static final float VIRTUAL_W = 1280f;
    private static final float VIRTUAL_H = 720f;

    private final RiverRunGame game;

    private OrthographicCamera camera;
    private FitViewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont font;

    // assets de UI
    private Texture riverClear, riverDark, riverRed;
    private Texture titulo;
    private Texture selectedBg; // O fundo do rio escolhido

    // Assets de cenário do menu
    private Texture rockTexture;
    private Texture[] krakenFrames;
    private Texture[] alienHeadFrames;

    private float krakenAnimationTime = 0f;
    private float alienHeadAnimationTime = 0f;
    private float animationFrameDuration = 0.15f; // Duração do frame (para ambos)

    private Array<Vector2> rockPositions; // Posições das pedras

    // botões
    private Button btnStart, btnSettings, btnExit;
    private Button btnPhaseClear, btnPhaseDark, btnPhaseRed, btnBack;
    private boolean selectingPhase = false;

    // layout logo
    private float logoX, logoY, logoW, logoH;

    public MenuScreen(RiverRunGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_W, VIRTUAL_H, camera);
        viewport.apply(true);
        camera.position.set(VIRTUAL_W / 2f, VIRTUAL_H / 2f, 0);
        camera.update();

        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(1.1f);

        // imagens
        riverClear = new Texture("ClearRiver.png");
        riverDark  = new Texture("DarkRiver.png");
        riverRed   = new Texture("RedRiver.png");
        titulo     = new Texture("Titulo.png");

        // Escolhe aleatoriamente um fundo de rio
        Texture[] rivers = { riverClear, riverDark, riverRed };
        selectedBg = rivers[MathUtils.random(0, 2)];

        // Carrega assets do cenário
        rockTexture = new Texture("rockClear.png");

        krakenFrames = new Texture[12];
        for (int i = 0; i < 12; i++) {
            krakenFrames[i] = new Texture("kraken" + (i + 1) + ".png");
        }


        alienHeadFrames = new Texture[12];
        for (int i = 0; i < 12; i++) {
            alienHeadFrames[i] = new Texture("alienHead" + (i + 1) + ".png");
        }

        // Gera posições aleatórias para as pedras
        rockPositions = new Array<>();
        for (int i = 0; i < 6; i++) {
            float rockX = MathUtils.random(0, VIRTUAL_W - 60f);
            float rockY = MathUtils.random(0, VIRTUAL_H - 60f);

            // Evita área central
            boolean isNearCenter = (rockX > VIRTUAL_W * 0.25f && rockX < VIRTUAL_W * 0.75f) &&
                (rockY > VIRTUAL_H * 0.2f && rockY < VIRTUAL_H * 0.8f);

            if (!isNearCenter) {
                rockPositions.add(new Vector2(rockX, rockY));
            }
        }

        // layout logo
        float maxW = 700f;
        float maxH = 220f;
        float scale = Math.min(maxW / titulo.getWidth(), maxH / titulo.getHeight());
        logoW = titulo.getWidth() * scale;
        logoH = titulo.getHeight() * scale;
        logoX = (VIRTUAL_W - logoW) / 2f;
        logoY = VIRTUAL_H - logoH - 40f;

        // layout de botões
        float cx = VIRTUAL_W / 2f;
        float bw = 340f, bh = 56f, gap = 12f;
        float baseY = VIRTUAL_H / 2f - 40f;

        btnStart    = new Button(cx - bw / 2f, baseY + (bh + gap) * 1.5f, bw, bh, "JOGAR");
        btnSettings = new Button(cx - bw / 2f, baseY + (bh + gap) * 0.5f, bw, bh, "CONFIGURAÇÕES");
        btnExit     = new Button(cx - bw / 2f, baseY - (bh + gap) * 0.5f, bw, bh, "SAIR");

        // painel de fases (modal)
        float pbw = 420f;
        btnPhaseClear = new Button(cx - pbw / 2f, baseY + (bh + gap) * 1.0f, pbw, bh, "Rio Calmo");
        btnPhaseDark  = new Button(cx - pbw / 2f, baseY + (bh + gap) * 0.0f, pbw, bh, "Rio Bravo");
        btnPhaseRed   = new Button(cx - pbw / 2f, baseY - (bh + gap) * 1.0f, pbw, bh, "Rio da Morte");
        btnBack       = new Button(cx - pbw / 2f, baseY - (bh + gap) * 2.0f, pbw, bh, "Voltar");

        // input
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    if (selectingPhase) {
                        selectingPhase = false; return true;
                    } else {
                        Gdx.app.exit(); return true;
                    }
                }
                return false;
            }

            @Override
            public boolean touchDown (int screenX, int screenY, int pointer, int button) {
                com.badlogic.gdx.math.Vector3 v = new com.badlogic.gdx.math.Vector3(screenX, screenY, 0);
                camera.unproject(v);

                if (!selectingPhase) {
                    if (btnStart.hit(v.x, v.y))      { selectingPhase = true; return true; }
                    if (btnSettings.hit(v.x, v.y))   { /* ... */ return true; }
                    if (btnExit.hit(v.x, v.y))       { Gdx.app.exit(); return true; }
                } else {
                    if (btnPhaseClear.hit(v.x, v.y)) { game.setScreen(new GameScreen(game, "ClearRiver.png")); return true; }
                    if (btnPhaseDark.hit(v.x, v.y))  { game.setScreen(new GameScreen(game, "DarkRiver.png"));  return true; }
                    if (btnPhaseRed.hit(v.x, v.y))   { game.setScreen(new GameScreen(game, "RedRiver.png"));   return true; }
                    if (btnBack.hit(v.x, v.y))       { selectingPhase = false; return true; }
                }
                return false;
            }
        });
    }

    @Override
    public void render(float delta) {
        // Atualiza animações
        krakenAnimationTime += delta;
        alienHeadAnimationTime += delta;

        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);

        // Ordem de desenho (fundo, cenário, logo)
        batch.begin();
        // 1. Fundo (rio aleatório)
        batch.draw(selectedBg, 0, 0, VIRTUAL_W, VIRTUAL_H);

        // 2. Pedras espalhadas
        float rockW = 64f, rockH = 64f;
        for(Vector2 pos : rockPositions) {
            batch.draw(rockTexture, pos.x, pos.y, rockW, rockH);
        }

        // 3. Kraken animado (inferior direito)
        int krakenFrameIdx = (int)(krakenAnimationTime / animationFrameDuration) % krakenFrames.length;
        float krakenW = 128f, krakenH = 128f;
        float krakenX = VIRTUAL_W - krakenW - 40f;
        float krakenY = 40f;
        batch.draw(krakenFrames[krakenFrameIdx], krakenX, krakenY, krakenW, krakenH);

        // 4. Alien Head animado (superior esquerdo)
        int alienFrameIdx = (int)(alienHeadAnimationTime / animationFrameDuration) % alienHeadFrames.length;
        float alienW = 128f, alienH = 128f; // Mesmo tamanho do kraken
        float alienX = 40f; // Margem esquerda
        float alienY = VIRTUAL_H - alienH - 40f; // Margem superior
        batch.draw(alienHeadFrames[alienFrameIdx], alienX, alienY, alienW, alienH);

        // 5. Logo (por cima de tudo)
        batch.draw(titulo, logoX, logoY, logoW, logoH);
        batch.end();
        // --- Fim da alteração de desenho ---

        if (!selectingPhase) {
            drawButton(btnStart);
            drawButton(btnSettings);
            drawButton(btnExit);
        } else {
            // modal escurecendo o fundo
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(0, 0, 0, 0.55f);
            shapes.rect(0, 0, VIRTUAL_W, VIRTUAL_H);
            float cardW = 640f, cardH = 360f;
            float cardX = (VIRTUAL_W - cardW)/2f, cardY = (VIRTUAL_H - cardH)/2f - 20f;
            shapes.setColor(0.10f, 0.13f, 0.17f, 1f);
            shapes.rect(cardX, cardY, cardW, cardH);
            shapes.setColor(0.18f, 0.22f, 0.28f, 1f);
            shapes.rect(cardX-4, cardY-4, cardW+8, 4);
            shapes.rect(cardX-4, cardY+cardH, cardW+8, 4);
            shapes.rect(cardX-4, cardY, 4, cardH);
            shapes.rect(cardX+cardW, cardY, 4, cardH);
            shapes.end();
            batch.begin();
            GlyphLayout gl = new GlyphLayout(font, "Selecione a fase");
            font.draw(batch, gl, (VIRTUAL_W - gl.width)/2f, cardY + cardH - 16f);
            batch.end();
            drawButton(btnPhaseClear, riverClear);
            drawButton(btnPhaseDark,  riverDark);
            drawButton(btnPhaseRed,   riverRed);
            drawButton(btnBack);
        }
    }

    private void drawButton(Button b) { drawButton(b, null); }

    private void drawButton(Button b, Texture preview) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0, 0, 0, 0.35f);
        shapes.rect(b.x + 3, b.y - 3, b.w, b.h);
        shapes.setColor(0.12f, 0.16f, 0.20f, 1f);
        shapes.rect(b.x, b.y, b.w, b.h);
        shapes.setColor(1f, 1f, 1f, 0.06f);
        shapes.rect(b.x, b.y + b.h - 4, b.w, 4);
        shapes.end();
        batch.begin();
        float pad = 12f;
        float textX = b.x + pad;
        if (preview != null) {
            float sW = 72f, sH = 40f;
            batch.draw(preview, b.x + pad, b.y + (b.h - sH) / 2f, sW, sH);
            textX += sW + 10f;
        }
        GlyphLayout gl = new GlyphLayout(font, b.label);
        font.draw(batch, gl, textX, b.y + (b.h + gl.height) / 2f + 1f);
        batch.end();
    }

    @Override public void resize(int w, int h) { viewport.update(w, h, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { dispose(); }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        font.dispose();
        riverClear.dispose();
        riverDark.dispose();
        riverRed.dispose();
        titulo.dispose();

        // Dispose dos assets do cenário
        rockTexture.dispose();
        if (krakenFrames != null) {
            for (Texture frame : krakenFrames) {
                if (frame != null) frame.dispose();
            }
        }
        // Dispose da cabeça do alien
        if (alienHeadFrames != null) {
            for (Texture frame : alienHeadFrames) {
                if (frame != null) frame.dispose();
            }
        }
    }

    private static class Button {
        float x, y, w, h;
        String label;
        Button(float x, float y, float w, float h, String l) { this.x=x; this.y=y; this.w=w; this.h=h; this.label=l; }
        boolean hit(float px, float py) { return px>=x && px<=x+w && py>=y && py<=y+h; }
    }
}
