package com.skanderj.gingerbread;

public abstract class Process {
	public static final int EXIT_SUCCESS = 0, EXIT_FAILURE = -1;

	private Thread thread;
	protected boolean isRunning;

	public Process(String identifier) {
		this.thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Process.this.run();
			}
		}, String.format("Process[%s]", identifier));
		this.isRunning = false;
	}

	public final void start() {
		if (this.isRunning) {
			return;
		} else {
			this.isRunning = true;
			this.thread.start();
		}
	}

	protected abstract void create();

	public final void stop() {
		if (this.isRunning) {
			this.isRunning = false;
			this.thread.interrupt();
		} else {
			return;
		}
	}

	protected abstract void destroy();

	private void run() {
		this.create();
		while (this.isRunning) {
			this.loop();
		}
		this.destroy();
	}

	protected abstract void loop();
}
