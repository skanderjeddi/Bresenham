package com.skanderj.gingerbread.sound;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class WaveSound extends Sound {
	private int frameOnPause;

	public WaveSound(String path) throws LineUnavailableException, UnsupportedAudioFileException, IOException {
		super(path);
	}

	public WaveSound(File file) throws LineUnavailableException, UnsupportedAudioFileException, IOException {
		super(file);
		this.frameOnPause = 0;
	}

	@Override
	public void start() {
		this.clip.start();
	}

	@Override
	public void stop() {
		this.clip.stop();
	}

	@Override
	public void reset() {
		this.stop();
		this.goToPosition(0);
	}

	@Override
	public void pause() {
		this.stop();
		this.frameOnPause = this.clip.getFramePosition();
	}

	@Override
	public void resume() {
		this.goToPosition(this.frameOnPause);
		this.clip.start();
	}

	@Override
	public void goToPosition(int framePosition) {
		this.clip.setFramePosition(framePosition);
	}
}