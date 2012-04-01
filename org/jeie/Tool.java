/*
 * Copyright (C) 2012 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of Jeie.
 * Jeie is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.jeie;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import org.jeie.ImageAction.FillAction;
import org.jeie.ImageAction.LineAction;
import org.jeie.ImageAction.PointAction;

public interface Tool
	{
	void mousePress(MouseEvent e, Canvas c, Palette p);

	void mouseRelease(MouseEvent e, Canvas c, Palette p);

	/**
	 * Invoked when the mouse moved, regardless of button states.
	 * <p>
	 * Notice that e.getButtons returns 0, because this event is
	 * independent of mouse buttons - it can occur with no button,
	 * or any number of buttons held down simultaneously. Buttons
	 * can be fetched using <code>e.getModifiersEx & BUTTON1_DOWN_MASK</code>.
	 * <p>
	 * Because this is not linked to a single button, getting the
	 * palette color should either be avoided or iterated per button.
	 * @param drag Whether 1 or more buttons are down during this event.
	 */
	void mouseMove(MouseEvent e, Canvas c, Palette p, boolean drag);

	void finish(Canvas c, Palette p);

	/**
	 * Convenience Tool parent class. This provides 4 convenience features:
	 * <li><code>active</code> variable to keep track of the currently active action.
	 * <li><code>finish</code> is implemented to simply commit <code>active</code> and reset it.
	 * <li><code>isValid</code> static method to determine whether the mouse event is even valid for our image.
	 * <li><code>cancel</code> method is offered to cancel (reject) the active action.
	 */
	public static abstract class GenericTool<K extends ImageAction> implements Tool
		{
		/**
		 * The currently active action. Extending classes should be sure to set this variable and push it to canvas.active.
		 */
		protected K active;

		/**
		 * Implemented to simply commit <code>active</code> and reset it.
		 * Also resets canvas.active and redraws the canvas cache.
		 */
		public void finish(Canvas c, Palette p)
			{
			if (active == null) return;
			c.acts.add(active);
			c.active = active = null;
			c.redrawCache();
			}

		/**
		 * Convenience static method to determine whether a mouse event is valid for the image.
		 * Namely, this will check mouse coordinates, to ensure that they are within image bounds (0,0,w,h).
		 * <p>
		 * If a palette is provided (non-null), this will check that the mouse button corresponds with a palette color.
		 * For instance, if the user presses the Middle mouse button, which has no corresponding color, it will still fire a
		 * mousePress event, but the tool will not be able to draw because there is no color to draw with.
		 * Setting the palette to null will omit this check and only perform a bounds check.
		 * <p>
		 * Notice: When using this function in mouseMove events, always set the palette to null.
		 * This is because mouseMove events are not dependent on a single button, even during a drag.
		 * As such, if you attempt to provide a palette in mouseMove, this function will always return false.
		 * @param e The MouseEvent. This argument is mandatory.
		 * @param c	The Canvas. This argument is mandatory.
		 * @param pal The Palette. Optional. Provide if you want to additionally confirm the mouse button has a palette color.
		 * @return Whether this mouse event is in-bounds of our image and (optionally) has a palette color.
		 */
		public static boolean isValid(MouseEvent e, Canvas c, Palette pal)
			{
			if (pal != null && pal.getSelectedColor(e.getButton()) == null) return false;
			Point p = e.getPoint();
			if (p.x < 0 || p.y < 0) return false;
			Dimension d = c.getImageSize();
			if (p.x >= d.width || p.y >= d.height) return false;
			return true;
			}

		/**
		 * Cancels (rejects/resets) the active action.
		 * Also resets canvas.active and repaints the canvas.
		 */
		public void cancel(Canvas c)
			{
			c.active = active = null;
			c.repaint();
			}
		}

	public static class LineTool extends GenericTool<LineAction>
		{
		long mouseTime;
		int button;

		public void mousePress(MouseEvent e, Canvas canvas, Palette p)
			{
			if (active != null)
				{
				if (e.getButton() == button)
					finish(canvas,p);
				else
					cancel(canvas);
				return;
				}
			if (!isValid(e,canvas,p)) return;
			button = e.getButton();
			mouseTime = e.getWhen();
			canvas.active = active = new LineAction(e.getPoint(),p.getSelectedColor(button));
			canvas.repaint();
			}

		public void mouseRelease(MouseEvent e, Canvas canvas, Palette pal)
			{
			if (e.getWhen() - mouseTime < 200) return;

			if (active != null) active.p2 = e.getPoint();
			finish(canvas,pal);
			}

		public void mouseMove(MouseEvent e, Canvas canvas, Palette p, boolean drag)
			{
			if (active != null)
				{
				Point op = active.p2;
				if (!op.equals(e.getPoint()))
					{
					active.p2 = e.getPoint();
					Rectangle r = new Rectangle(op);
					r.add(active.p1);
					r.add(active.p2);
					canvas.repaint(r);
					}
				}
			}
		}

	public static class PointTool extends GenericTool<PointAction>
		{
		public void mousePress(MouseEvent e, Canvas c, Palette p)
			{
			if (active != null)
				{
				cancel(c);
				return;
				}
			if (!isValid(e,c,p)) return;
			c.active = active = new PointAction(p.getSelectedColor(e.getButton()));
			active.add(e.getPoint());
			c.repaint();
			}

		public void mouseRelease(MouseEvent e, Canvas c, Palette p)
			{
			finish(c,p);
			}

		public void mouseMove(MouseEvent e, Canvas c, Palette p, boolean drag)
			{
			if (active != null && isValid(e,c,null))
				{
				Point pt = e.getPoint();
				Rectangle r = new Rectangle(pt);
				if (!active.pts.isEmpty()) r.add(active.pts.getLast());
				active.add(pt);
				c.repaint(r);
				}
			}
		}

	public static class FillTool extends GenericTool<FillAction>
		{
		public void mousePress(MouseEvent e, Canvas c, Palette p)
			{
			if (active != null)
				{
				cancel(c);
				return;
				}
			if (!isValid(e,c,p)) return;
			c.active = active = new FillAction(c.getRenderImage(),e.getPoint(),
					p.getSelectedColor(e.getButton()),0);
			c.repaint();
			}

		public void mouseRelease(MouseEvent e, Canvas c, Palette p)
			{
			finish(c,p);
			}

		//Unused
		public void mouseMove(MouseEvent e, Canvas c, Palette p, boolean drag)
			{ //Unused
			}
		}
	}
