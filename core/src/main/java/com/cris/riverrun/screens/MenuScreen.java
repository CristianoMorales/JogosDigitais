package com.cris.riverrun.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.cris.riverrun.RiverRunGame;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo;

public class MenuScreen implements Screen {

    private final RiverRunGame game;
    private Stage stage;
    private SpriteBatch batch;

    private Texture backgroundTexture;   // usa river.png
    private Texture wavesTexture;        // ondas geradas
    private Texture pixel;               // overlay 1x1
    private float waveOffset;

    private Label titleLabel;
    private BitmapFont titleFont;
    private BitmapFont buttonFont;

    private Drawable btnUp, btnOver, btnDown;

    private final float VIRTUAL_W = 1280f;
    private final float VIRTUAL_H = 720f;

    public MenuScreen(RiverRunGame game) {
        this.game = game;
        this.batch = new SpriteBatch();
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(VIRTUAL_W, VIRTUAL_H), batch);
        Gdx.input.setInputProcessor(stage);

        // fundo
        backgroundTexture = new Texture("river.png");

        // overlay 1x1
        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(1,1,1,1);
        p.fill();
        pixel = new Texture(p);
        p.dispose();

        // ondas
        wavesTexture = makeWavesTexture(512, 200);

        // fontes
        titleFont = new BitmapFont();
        titleFont.getData().setScale(2.6f);
        buttonFont = new BitmapFont();

        // botões sem skin
        btnUp   = makeRectDrawable(new Color(0,0,0,0.45f));
        btnOver = makeRectDrawable(new Color(0.10f,0.20f,0.35f,0.60f));
        btnDown = makeRectDrawable(new Color(0,0,0,0.75f));

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = buttonFont;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.overFontColor = Color.SKY;
        buttonStyle.downFontColor = Color.LIGHT_GRAY;
        buttonStyle.up = btnUp;
        buttonStyle.over = btnOver;
        buttonStyle.down = btnDown;

        // título
        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font = titleFont;
        titleStyle.fontColor = Color.CYAN;
        titleLabel = new Label("RIVER RUN", titleStyle);
        titleLabel.setAlignment(Align.center);

        // botões
        TextButton startButton   = new TextButton("START",   buttonStyle);
        TextButton optionsButton = new TextButton("OPTIONS", buttonStyle);
        TextButton exitButton    = new TextButton("EXIT",    buttonStyle);

        addButtonHoverAnimations(startButton);
        addButtonHoverAnimations(optionsButton);
        addButtonHoverAnimations(exitButton);

        // layout
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.add(titleLabel).padBottom(60).row();
        table.add(startButton).width(280).height(64).pad(10).row();
        table.add(optionsButton).width(280).height(64).pad(10).row();
        table.add(exitButton).width(280).height(64).pad(10);
        stage.addActor(table);

        // ações
        startButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                // Se o seu GameScreen aceitar RiverRunGame, troque para: new GameScreen(game)
                game.setScreen(new GameScreen());
            }
        });
        optionsButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                // game.setScreen(new OptionsScreen(game));
            }
        });
        exitButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        // música opcional
        tryPlayMusic("music/river_theme.mp3");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        waveOffset += delta * 30f;

        batch.begin();
        // fundo
        batch.draw(backgroundTexture, 0, 0, VIRTUAL_W, VIRTUAL_H);

        // overlay escuro
        batch.setColor(0,0,0,0.35f);
        batch.draw(pixel, 0, 0, VIRTUAL_W, VIRTUAL_H);
        batch.setColor(1,1,1,1);

        // ondas na base
        float w = wavesTexture.getWidth();
        float x = -(waveOffset % w);
        batch.draw(wavesTexture, x, 0);
        batch.draw(wavesTexture, x + w, 0);
        batch.end();

        // leve “shake” no título
        float shake = (float) Math.sin(System.currentTimeMillis() * 0.005) * 2f;
        titleLabel.setRotation(shake);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        batch.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (wavesTexture != null) wavesTexture.dispose();
        if (pixel != null) pixel.dispose();
        disposeDrawable(btnUp);
        disposeDrawable(btnOver);
        disposeDrawable(btnDown);
        if (titleFont != null) titleFont.dispose();
        if (buttonFont != null && buttonFont != titleFont) buttonFont.dispose();
    }

    // ---------- Helpers ----------

    private void addButtonHoverAnimations(final TextButton btn) {
        btn.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                btn.setTransform(true);
                btn.addAction(scaleTo(1.03f, 1.03f, 0.08f));
            }
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                btn.addAction(scaleTo(1f, 1f, 0.08f));
            }
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                btn.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                    scaleTo(0.98f, 0.98f, 0.05f),
                    scaleTo(1.03f, 1.03f, 0.05f)
                ));
                return super.touchDown(event, x, y, pointer, button);
            }
        });
    }

    private Drawable makeRectDrawable(Color color) {
        Pixmap pm = new Pixmap(10, 10, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        return new TextureRegionDrawable(new TextureRegion(tex));
    }

    private void disposeDrawable(Drawable d) {
        if (d instanceof TextureRegionDrawable) {
            TextureRegion r = ((TextureRegionDrawable) d).getRegion();
            if (r != null && r.getTexture() != null) r.getTexture().dispose();
        }
    }

    private Texture makeWavesTexture(int w, int h) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(0f, 0.3f, 0.6f, 0.18f);
        pm.fill();
        pm.setColor(1f, 1f, 1f, 0.20f);
        for (int y = 20; y < h; y += 20) {
            for (int x = 0; x < w; x++) {
                int yOff = (int)(4 * Math.sin((x + y) * 0.05));
                int yy = Math.min(h - 1, Math.max(0, y + yOff));
                pm.drawPixel(x, yy);
            }
        }
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    private void tryPlayMusic(String path) {
        try {
            if (Gdx.files.internal(path).exists()) {
                game.playMenuMusic(path);
            }
        } catch (Exception ignored) {}
    }
}
