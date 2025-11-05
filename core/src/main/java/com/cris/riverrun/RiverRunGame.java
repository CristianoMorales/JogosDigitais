package com.cris.riverrun;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.cris.riverrun.screens.MenuScreen;

public class RiverRunGame extends Game {
    public SpriteBatch batch;
    private Music menuMusic;

    @Override
    public void create() {
        batch = new SpriteBatch();
        setScreen(new MenuScreen(this));
    }

    // Toca música de fundo no menu
    public void playMenuMusic(String path) {
        stopMenuMusic(); // garante que não toque duas ao mesmo tempo
        try {
            menuMusic = Gdx.audio.newMusic(Gdx.files.internal(path));
            menuMusic.setLooping(true);
            menuMusic.setVolume(0.5f); // 50% do volume máximo
            menuMusic.play();
        } catch (Exception e) {
            System.out.println("Não foi possível tocar a música: " + path);
        }
    }

    // Para e limpa a música atual
    public void stopMenuMusic() {
        if (menuMusic != null) {
            menuMusic.stop();
            menuMusic.dispose();
            menuMusic = null;
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        stopMenuMusic(); // garante que pare a música ao fechar o jogo
        batch.dispose();
    }
}
