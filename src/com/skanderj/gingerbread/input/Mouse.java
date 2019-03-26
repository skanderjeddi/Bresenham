package com.skanderj.gingerbread.input;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Mouse extends MouseAdapter {
	private static final int BUTTON_COUNT = 3;

	private boolean[] currentButtonsStates;
	private MouseState[] buttonsStates;

	private int x, y, currentX, currentY;

	public Mouse() {
		this.currentButtonsStates = new boolean[Mouse.BUTTON_COUNT];
		this.buttonsStates = new MouseState[Mouse.BUTTON_COUNT];
		for (int index = 0; index < Mouse.BUTTON_COUNT; index++) {
			this.buttonsStates[index] = MouseState.UP;
		}
	}

	public synchronized void update() {
		this.x = this.currentX;
		this.y = this.currentY;
		for (int index = 0; index < Mouse.BUTTON_COUNT; index++) {
			if (this.currentButtonsStates[index] == true) {
				if (this.buttonsStates[index] == MouseState.UP) {
					this.buttonsStates[index] = MouseState.DOWN_ONCE;
				} else {
					this.buttonsStates[index] = MouseState.DOWN;
				}
			} else {
				this.buttonsStates[index] = MouseState.UP;
			}
		}
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public boolean isButtonDownOnce(int button) {
		return this.buttonsStates[(button - 1)] == MouseState.DOWN_ONCE;
	}

	public boolean isButtonDown(int button) {
		return (this.buttonsStates[(button - 1)] == MouseState.DOWN_ONCE) || (this.buttonsStates[(button - 1)] == MouseState.DOWN);
	}

	@Override
	public synchronized void mousePressed(MouseEvent mouseEvent) {
		this.currentButtonsStates[(mouseEvent.getButton() - 1)] = true;
	}

	@Override
	public synchronized void mouseReleased(MouseEvent mouseEvent) {
		this.currentButtonsStates[(mouseEvent.getButton() - 1)] = false;
	}

	@Override
	public synchronized void mouseEntered(MouseEvent mouseEvent) {
		this.mouseMoved(mouseEvent);
	}

	@Override
	public synchronized void mouseExited(MouseEvent mouseEvent) {
		this.mouseMoved(mouseEvent);
	}

	@Override
	public synchronized void mouseDragged(MouseEvent mouseEvent) {
		this.mouseMoved(mouseEvent);
	}

	@Override
	public synchronized void mouseMoved(MouseEvent mouseEvent) {
		this.currentX = mouseEvent.getX();
		this.currentY = mouseEvent.getY();
	}

	private static enum MouseState {
		UP, DOWN, DOWN_ONCE;
	}
}
