package com.prism.components.extended;

import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class JKineticScrollPane extends RTextScrollPane {

	private static final double WHEEL_SENSITIVITY = 0.8;
	private static final double FRICTION = 0.90;
	private static final int TIMER_DELAY = 15;

	private final JViewport vp;
	private final Timer animator;

	private double velX = 0, velY = 0;
	private long lastTime;

	private Point dragStart;

	public JKineticScrollPane(Component view) {
		super(view);
		vp = getViewport();

		setWheelScrollingEnabled(false);
		addMouseWheelListener(this::onWheel);

		animator = new Timer(TIMER_DELAY, e -> tick());
		animator.setInitialDelay(0);

		vp.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				stop();
				dragStart = e.getPoint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (dragStart == null) return;
				int dx = e.getX() - dragStart.x;
				int dy = e.getY() - dragStart.y;
				dragStart = null;

				velX = -dx / 5.0;
				velY = -dy / 5.0;
				lastTime = System.currentTimeMillis();
				animator.start();
			}
		});

		vp.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if (dragStart == null) return;
				int dx = e.getX() - dragStart.x;
				int dy = e.getY() - dragStart.y;
				dragStart = e.getPoint();

				Point p = vp.getViewPosition();
				p.translate(-dx, -dy);
				clampAndSet(p);
			}
		});
	}

	private void onWheel(MouseWheelEvent e) {
		int rot = e.getWheelRotation();
		velY += rot * WHEEL_SENSITIVITY;
		if (!animator.isRunning()) {
			lastTime = System.currentTimeMillis();
			animator.start();
		}
		e.consume();
	}

	private void tick() {
		long now = System.currentTimeMillis();
		double dt = now - lastTime;
		lastTime = now;

		Point p = vp.getViewPosition();
		p.translate((int) (velX * dt), (int) (velY * dt));
		clampAndSet(p);

		velX *= FRICTION;
		velY *= FRICTION;

		if (Math.abs(velX) < 0.01 && Math.abs(velY) < 0.01) {
			animator.stop();
		}
	}

	private void clampAndSet(Point p) {
		Dimension viewSize = vp.getViewSize();
		Dimension extentSize = vp.getExtentSize();
		p.x = Math.max(0, Math.min(p.x, viewSize.width - extentSize.width));
		p.y = Math.max(0, Math.min(p.y, viewSize.height - extentSize.height));
		vp.setViewPosition(p);
	}

	private void stop() {
		animator.stop();
		velX = velY = 0;
	}
}