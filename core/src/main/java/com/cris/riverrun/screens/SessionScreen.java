package com.cris.riverrun.screens;

import com.cris.riverrun.RiverRunGame;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class SessionScreen implements Screen {
    private final RiverRunGame game;
    private Stage stage;
    private BitmapFont font;

    private static final float VIRTUAL_W = 1280, VIRTUAL_H = 720;

    private int reps = 0;          // SPACE conta 1 rep
    private float elapsed = 0f;    // cronômetro
    private boolean spaceLatch = false;

    private Label repsLabel, timeLabel;

    public SessionScreen(RiverRunGame game) { this.game = game; }

    @Override public void show() {
        stage = new Stage(new FitViewport(VIRTUAL_W, VIRTUAL_H), game.batch);
        Gdx.input.setInputProcessor(stage);
        font = new BitmapFont();

        Label.LabelStyle ls = new Label.LabelStyle(font, com.badlogic.gdx.graphics.Color.WHITE);
        Table root = new Table(); root.setFillParent(true); stage.addActor(root);

        Label title = new Label("Sessao de Reabilitacao (Protótipo)", ls);
        repsLabel = new Label("Reps: 0", ls);
        timeLabel = new Label("Tempo: 0.0s", ls);
        Label hint = new Label("SPACE conta; ESC volta ao menu", ls);

        root.add(title).padBottom(30f).row();
        root.add(repsLabel).padBottom(10f).row();
        root.add(timeLabel).padBottom(30f).row();
        root.add(hint);
    }

    @Override public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MenuScreen(game));
            return;
        }
        elapsed += delta;

        boolean spaceDown = Gdx.input.isKeyPressed(Input.Keys.SPACE);
        if (spaceDown && !spaceLatch) { reps++; spaceLatch = true; }
        if (!spaceDown) spaceLatch = false;

        repsLabel.setText("Reps: " + reps);
        timeLabel.setText(String.format("Tempo: %.1fs", elapsed));

        Gdx.gl.glClearColor(0.10f, 0.10f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta); stage.draw();
    }

    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { stage.dispose(); font.dispose(); }
}
