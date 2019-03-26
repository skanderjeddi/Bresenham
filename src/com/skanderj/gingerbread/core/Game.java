package com.skanderj.gingerbread.core;

import com.skanderj.gingerbread.Process;

public abstract class Game extends Process {
	private double rate;

	public Game(String identifier, double rate) {
		super(identifier);
		this.rate = rate;
	}

	@Override
	protected abstract void create();

	@Override
	protected abstract void destroy();

	@Override
	protected void loop() {
		long startTime = System.nanoTime();
		double nanosecondsPerTick = 1000000000D / this.rate;
		int frames = 0;
		int updates = 0;
		long resetTime = System.currentTimeMillis();
		double delta = 0.0D;
		while (this.isRunning) {
			long endTime = System.nanoTime();
			delta += (endTime - startTime) / nanosecondsPerTick;
			startTime = endTime;
			boolean shouldRender = false;
			while (delta >= 1) {
				updates++;
				this.update(delta);
				delta -= 1;
				shouldRender = true;
			}
			if (shouldRender) {
				frames++;
				this.render();
			}
			if ((System.currentTimeMillis() - resetTime) >= 1000) {
				resetTime += 1000;
				System.out.printf("LSU %d - LSF %d\n", updates, frames);
				frames = 0;
				updates = 0;
			}
		}
	}

	public abstract void update(double delta);

	public abstract void render();

}
