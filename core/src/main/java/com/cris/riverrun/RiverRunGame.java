package com.cris.riverrun;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.cris.riverrun.screens.MenuScreen;
import com.cris.riverrun.screens.GameScreen;

public class RiverRunGame extends Game {
    public SpriteBatch batch;
    private Music menuMusic;

    @Override
    public void create() {
        batch = new SpriteBatch();
        setScreen(new MenuScreen(this));
        playMenuMusic("menu_theme.mp3"); // toca música do menu assim que o jogo inicia
    }

    // toca música do menu (chame de novo ao voltar pro menu)
    public void playMenuMusic(String path) {
        stopMenuMusic();
        try {
            menuMusic = Gdx.audio.newMusic(Gdx.files.internal(path));
            menuMusic.setLooping(true);
            menuMusic.setVolume(0.5f);
            menuMusic.play();
        } catch (Exception e) {
            System.out.println("Não foi possível tocar a música: " + path);
        }
    }

    // parar música
    public void stopMenuMusic() {
        if (menuMusic != null) {
            menuMusic.stop();
            menuMusic.dispose();
            menuMusic = null;
        }
    }

    @Override
    public void setScreen(com.badlogic.gdx.Screen screen) {
        super.setScreen(screen);
        // controle automático de música conforme a tela
        if (screen instanceof MenuScreen) {
            playMenuMusic("menu_theme.mp3");
        } else if (screen instanceof GameScreen) {
            stopMenuMusic();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        stopMenuMusic();
        batch.dispose();
    }
}
