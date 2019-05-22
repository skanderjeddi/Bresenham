package com.skanderj.bresenham.time;

public final class Watch {
	private long startTime, endTime, elapsedTime;
	private WatchMode mode;

	public Watch(WatchMode mode) {
		this.startTime = 0;
		this.endTime = 0;
		this.elapsedTime = 0;
		this.mode = mode;
	}

	public final void begin() {
		this.reset();
		switch (this.mode) {
		case MILLIS:
			this.startTime = System.currentTimeMillis();
			break;
		case NANOS:
			this.startTime = System.nanoTime();
			break;
		}
	}

	public final void end() {
		switch (this.mode) {
		case MILLIS:
			this.endTime = System.currentTimeMillis();
			break;
		case NANOS:
			this.endTime = System.nanoTime();
			break;
		}
		this.elapsedTime = this.endTime - this.startTime;
	}

	public final long getElapsedTime() {
		return this.elapsedTime;
	}

	private final void reset() {
		this.startTime = 0;
		this.endTime = 0;
		this.elapsedTime = 0;
	}

	public WatchMode getMode() {
		return this.mode;
	}

	public void setMode(WatchMode mode) {
		this.mode = mode;
	}

	public static enum WatchMode {
		MILLIS, NANOS;
	}
}
