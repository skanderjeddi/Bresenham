package com.skanderj.gingerbread.input;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Keyboard extends KeyAdapter {
	private static final int KEY_COUNT = 256;

	private boolean[] currentKeysStates;
	private KeyState[] keysStates;

	public Keyboard() {
		this.currentKeysStates = new boolean[Keyboard.KEY_COUNT];
		this.keysStates = new KeyState[Keyboard.KEY_COUNT];
		for (int index = 0; index < Keyboard.KEY_COUNT; index++) {
			this.keysStates[index] = KeyState.UP;
		}
	}

	public synchronized void update() {
		for (int index = 0; index < Keyboard.KEY_COUNT; index++) {
			if (this.currentKeysStates[index] == true) {
				if (this.keysStates[index] == KeyState.UP) {
					this.keysStates[index] = KeyState.DOWN_ONCE;
				} else {
					this.keysStates[index] = KeyState.DOWN;
				}
			} else {
				this.keysStates[index] = KeyState.UP;
			}
		}
	}

	public boolean isKeyDown(int keyCode) {
		return (this.keysStates[keyCode] == KeyState.DOWN_ONCE) || (this.keysStates[keyCode] == KeyState.DOWN);
	}

	public boolean isKeyDownOnce(int keyCode) {
		return this.keysStates[keyCode] == KeyState.DOWN_ONCE;
	}

	@Override
	public synchronized void keyPressed(KeyEvent keyEvent) {
		int keyCode = keyEvent.getKeyCode();
		if ((keyCode >= 0) && (keyCode < Keyboard.KEY_COUNT)) {
			this.currentKeysStates[keyCode] = true;
		}
	}

	@Override
	public synchronized void keyReleased(KeyEvent keyEvent) {
		int keyCode = keyEvent.getKeyCode();
		if ((keyCode >= 0) && (keyCode < Keyboard.KEY_COUNT)) {
			this.currentKeysStates[keyCode] = false;
		}
	}

	private static enum KeyState {
		UP, DOWN, DOWN_ONCE;
	}
}
