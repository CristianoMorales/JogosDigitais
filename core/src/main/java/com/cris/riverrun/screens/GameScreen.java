package com.cris.riverrun.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class GameScreen implements Screen {

    // Mundo fixo (independente da resolução do monitor)
    private static final float VIRTUAL_W = 360f;
    private static final float VIRTUAL_H = 640f;

    private OrthographicCamera camera;
    private FitViewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer shapes;

    private Texture river, boat;

    // Fundo (duas peças do tamanho do mundo)
    private float riverY1, riverY2;

    // Barco
    private Vector2 boatPos;
    private float boatBaseY = 120f;
    private float boatBobTime = 0f;
    // Tamanho final do barco no mundo (controla “barco gigante”)
    private float boatW = 48f;   // ajuste fino aqui
    private float boatH = 96f;   // ajuste fino aqui

    // Gameplay (sem IoT ainda)
    private float cadencePower = 0f;
    private float cadenceAccel = 1.8f;
    private float cadenceDecay = 0.8f;
    private float baseSpeed = 80f;
    private float maxBoost = 140f;
    private float scrollSpeed = 0f;
    private float targetSpeed = 160f;

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_W, VIRTUAL_H, camera);
        viewport.apply(true);
        camera.position.set(VIRTUAL_W / 2f, VIRTUAL_H / 2f, 0);
        camera.update();

        batch = new SpriteBatch();
        shapes = new ShapeRenderer();

        river = new Texture("river.png");
        river.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        boat = new Texture("boat.png");
        boat.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        riverY1 = 0;
        riverY2 = VIRTUAL_H;

        boatPos = new Vector2(VIRTUAL_W / 2f - boatW / 2f, boatBaseY);
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);

        batch.begin();
        // cada peça do rio preenche o mundo inteiro
        batch.draw(river, 0, riverY1, VIRTUAL_W, VIRTUAL_H);
        batch.draw(river, 0, riverY2, VIRTUAL_W, VIRTUAL_H);
        // barco com tamanho controlado
        batch.draw(boat, boatPos.x, boatPos.y, boatW, boatH);
        batch.end();

        drawHUD();
    }

    private void update(float delta) {
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.UP))
            cadencePower += cadenceAccel * delta;
        else
            cadencePower -= cadenceDecay * delta;

        cadencePower = MathUtils.clamp(cadencePower, 0f, 1f);
        scrollSpeed = baseSpeed + maxBoost * cadencePower;

        riverY1 -= scrollSpeed * delta;
        riverY2 -= scrollSpeed * delta;

        if (riverY1 <= -VIRTUAL_H) riverY1 = riverY2 + VIRTUAL_H;
        if (riverY2 <= -VIRTUAL_H) riverY2 = riverY1 + VIRTUAL_H;

        boatBobTime += delta * 2f;
        boatPos.y = boatBaseY + MathUtils.sin(boatBobTime) * 4f;

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE))
            Gdx.app.exit();
    }

    private void drawHUD() {
        float barW = 160f, barH = 10f, pad = 8f;
        float speedRatio = MathUtils.clamp((scrollSpeed - baseSpeed) / maxBoost, 0f, 1f);
        float targetRatio = MathUtils.clamp((targetSpeed - baseSpeed) / maxBoost, 0f, 1f);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.1f, 0.1f, 0.1f, 1f);
        shapes.rect(pad, VIRTUAL_H - pad - barH, barW, barH);
        shapes.setColor(0.2f, 0.7f, 1f, 1f);
        shapes.rect(pad, VIRTUAL_H - pad - barH, barW * speedRatio, barH);
        shapes.setColor(1f, 1f, 1f, 1f);
        shapes.rect(pad + barW * targetRatio - 1f, VIRTUAL_H - pad - barH, 2f, barH);
        shapes.end();
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { dispose(); }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        river.dispose();
        boat.dispose();
    }
}
