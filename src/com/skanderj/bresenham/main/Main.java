package com.skanderj.bresenham.main;

import com.skanderj.bresenham.Bresenham;

public final class Main {
	private Main() {
		return;
	}

	public static void main(String[] args) {
		Bresenham instance = Bresenham.getInstance();
		instance.start();
	}
}
