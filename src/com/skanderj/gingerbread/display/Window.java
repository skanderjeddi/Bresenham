package com.skanderj.gingerbread.display;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;

import com.skanderj.gingerbread.core.Game;
import com.skanderj.gingerbread.input.Keyboard;
import com.skanderj.gingerbread.input.Mouse;

public class Window {
	private Frame frame;
	private Canvas canvas;

	private int buffers;

	private boolean isCloseRequested;

	public Window(Game game, String title, int width, int height, int buffers) {
		this.frame = new Frame();
		this.canvas = new Canvas();
		this.canvas.setMinimumSize(new Dimension(width, height));
		this.canvas.setMaximumSize(new Dimension(width, height));
		this.canvas.setPreferredSize(new Dimension(width, height));
		this.frame.setTitle(title);
		this.frame.add(this.canvas);
		this.frame.setResizable(false);
		this.frame.pack();
		this.frame.setLocationRelativeTo(null);
		this.frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				Window.this.isCloseRequested = true;
			}
		});
		this.buffers = buffers;
		this.isCloseRequested = false;
	}

	public void show() {
		this.frame.setVisible(true);
	}

	public void hide() {
		this.frame.setVisible(false);
	}

	public BufferStrategy getBufferStrategy() {
		BufferStrategy bufferStrategy = this.canvas.getBufferStrategy();
		if (bufferStrategy == null) {
			this.canvas.createBufferStrategy(this.buffers);
			return this.getBufferStrategy();
		}
		return bufferStrategy;
	}

	public void registerKeyboard(Keyboard keyboard) {
		this.canvas.addKeyListener(keyboard);
		this.canvas.requestFocus();
	}

	public void registerMouse(Mouse mouse) {
		this.canvas.addMouseListener(mouse);
		this.canvas.addMouseMotionListener(mouse);
		this.canvas.addMouseWheelListener(mouse);
		this.canvas.requestFocus();
	}

	public boolean isCloseRequested() {
		return this.isCloseRequested;
	}
}
