/*
 * Copyright (C) 2012 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2013 jimn346 <jds9496@gmail.com>
 * 
 * This file is part of Jeie.
 * 
 * Jeie is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Jeie is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License (COPYING) for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.jeie;

import static org.jeie.OptionComponent.emptyPanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jeie.Canvas.RenderMode;
import org.jeie.ImageAction.FillAction;
import org.jeie.ImageAction.GradientAction;
import org.jeie.ImageAction.LineAction;
import org.jeie.ImageAction.PointAction;
import org.jeie.ImageAction.RectangleAction;
import org.jeie.ImageAction.OvalAction;
import org.jeie.ImageAction.TextAction;
import org.jeie.OptionComponent.FillOptions;
import org.jeie.OptionComponent.FillOptions.FillType;
import org.jeie.OptionComponent.GradientOptions;
import org.jeie.OptionComponent.SizeOptions;
import org.jeie.OptionComponent.TextOptions;

public interface Tool
	{
	void mousePress(MouseEvent e, Canvas c, Palette p);

	void mouseRelease(MouseEvent e, Canvas c, Palette p);

	/**
	 * @return Returns an options widget for controlling tool settings.
	 */
	public JComponent getOptionsComponent();

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
	 * @param e Class describing this mouse event.
	 * @param c The Canvas for which the event is fired.
	 * @param p The current color Palette.
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
	 * @param <K> The class of undo action generated by this tool (a child of ImageAction).
	 */
	public static abstract class GenericTool<K extends ImageAction> implements Tool
		{
		/**
		 * The currently active action. Extending classes should be sure to set this variable and push it to canvas.active.
		 */
		protected K active;

		public abstract JComponent getOptionsComponent();

		/**
		 * Implemented to simply commit <code>active</code> and reset it.
		 * Also resets canvas.active and redraws the canvas cache.
		 * @param c The Canvas on which we are operating.
		 * @param p The current Palette.
		 */
		public void finish(Canvas c, Palette p)
			{
			if (active == null) return;
			c.acts.add(active);
			c.active = active = null;
			c.redoActs.clear();
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
		 * @param c The canvas whose tool action is being cancelled.
		 */
		public void cancel(Canvas c)
			{
			c.active = active = null;
			c.repaint();
			}
		}

	public static class LineTool extends GenericTool<LineAction> implements ChangeListener
		{
		long mouseTime;
		int button;
		int diameter;

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
			if (!isValid(e,canvas,p) && canvas.renderMode != RenderMode.TILED) return;
			button = e.getButton();
			mouseTime = e.getWhen();
			canvas.active = active = new LineAction(canvas, e.getPoint(),p.getSelectedColor(button),diameter);
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
			if (active != null && !active.p2.equals(e.getPoint()))
				{
				Rectangle r = new Rectangle(active.p2); //previous value
				active.p2 = e.getPoint();
				r.add(active.p1);
				r.add(active.p2);
				r.x -= active.diameter << 1;
				r.y -= active.diameter << 1;
				r.width += active.diameter << 2;
				r.height += active.diameter << 2;
				canvas.repaint(r);
				}
			}

		private static final SizeOptions so = new SizeOptions();

		public LineTool()
			{
			so.addChangeListener(this);
			}

		@Override
		public JComponent getOptionsComponent()
			{
			return so;
			}

		public void stateChanged(ChangeEvent e)
			{
			diameter = so.getValue();
			}
		}

	public static class GradientTool extends GenericTool<GradientAction> implements ListSelectionListener
		{
		long mouseTime;
		int button;
		GradientOptions.GradientType type;

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
			button = e.getButton();
			if (!isValid(e,canvas,p) && canvas.renderMode != RenderMode.TILED) return;
			mouseTime = e.getWhen();
			Color c1, c2;
			if (button == MouseEvent.BUTTON1)
				{
				c1 = p.getLeft();
				c2 = p.getRight();
				}
			else
				{
				c1 = p.getRight();
				c2 = p.getLeft();
				}
			canvas.active = active = new GradientAction(canvas,e.getPoint(),c1,c2,type);
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
			if (active != null && !active.p2.equals(e.getPoint()))
				{
				active.p2 = e.getPoint();
				canvas.repaint();
				}
			}

		private static final GradientOptions go = new GradientOptions();

		public GradientTool()
			{
			go.addListSelectionListener(this);
			type = go.getFillType();
			}

		@Override
		public JComponent getOptionsComponent()
			{
			return go;
			}

		public void valueChanged(ListSelectionEvent e)
			{
			type = go.getFillType();
			}
		}

	public static class PointTool extends GenericTool<PointAction>
		{
		@Override
		public JComponent getOptionsComponent()
			{
			return emptyPanel;
			}

		public void mousePress(MouseEvent e, Canvas c, Palette p)
			{
			if (active != null)
				{
				cancel(c);
				return;
				}
			if (!isValid(e,c,p) && c.renderMode != RenderMode.TILED) return;
			c.active = active = new PointAction(c, p.getSelectedColor(e.getButton()));
			active.add(e.getPoint());
			c.repaint();
			}

		public void mouseRelease(MouseEvent e, Canvas c, Palette p)
			{
			finish(c,p);
			}

		public void mouseMove(MouseEvent e, Canvas c, Palette p, boolean drag)
			{
			if (active != null)
				{
				Point pt = e.getPoint();
				if (!active.pts.isEmpty() && active.pts.getLast().equals(pt)) return;
				Rectangle r = new Rectangle(pt);
				if (!active.pts.isEmpty()) r.add(active.pts.getLast());
				active.add(pt);
				c.repaint(r);
				}
			}
		}

	public static class RectangleTool extends GenericTool<RectangleAction> implements
			ListSelectionListener
		{
		long mouseTime;
		int button;
		FillType type = FillType.OUTLINE;

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
			Color c1 = p.getLeft();
			Color c2 = p.getRight();
			if (button != MouseEvent.BUTTON1)
				{ //lol, shut up
				c1 = c2;
				c2 = p.getLeft();
				}
			switch (type)
				{
				case OUTLINE:
					canvas.active = active = new RectangleAction(canvas, e.getPoint(),c1,null);
					break;
				case BOTH:
					canvas.active = active = new RectangleAction(canvas, e.getPoint(),c1,c2);
					break;
				case FILL:
					canvas.active = active = new RectangleAction(canvas, e.getPoint(),c1,c1);
					break;
				}
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
			if (active != null && !active.p2.equals(e.getPoint()))
				{
				Rectangle r = new Rectangle(active.p2); //previous value
				active.p2 = e.getPoint();
				r.add(active.p1);
				r.add(active.p2);
				canvas.repaint(r);
				}
			}

		private static final FillOptions fills = new FillOptions();

		@Override
		public JComponent getOptionsComponent()
			{
			return fills;
			}

		public RectangleTool()
			{
			fills.addListSelectionListener(this);
			}

		public void valueChanged(ListSelectionEvent e)
			{
			type = fills.getFillType();
			}
		}
	

	public static class OvalTool extends GenericTool<OvalAction> implements
			ListSelectionListener
		{
		long mouseTime;
		int button;
		FillType type = FillType.OUTLINE;

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
			Color c1 = p.getLeft();
			Color c2 = p.getRight();
			if (button != MouseEvent.BUTTON1)
				{ //lol, shut up
				c1 = c2;
				c2 = p.getLeft();
				}
			switch (type)
				{
				case OUTLINE:
					canvas.active = active = new OvalAction(canvas, e.getPoint(),c1,null);
					break;
				case BOTH:
					canvas.active = active = new OvalAction(canvas, e.getPoint(),c1,c2);
					break;
				case FILL:
					canvas.active = active = new OvalAction(canvas, e.getPoint(),c1,c1);
					break;
				}
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
			if (active != null && !active.p2.equals(e.getPoint()))
				{
				Rectangle r = new Rectangle(active.p2); //previous value
				active.p2 = e.getPoint();
				r.add(active.p1);
				r.add(active.p2);
				canvas.repaint(r);
				}
			}

		private static final FillOptions fills = new FillOptions();

		@Override
		public JComponent getOptionsComponent()
			{
			return fills;
			}

		public OvalTool()
			{
			fills.addListSelectionListener(this);
			}

		public void valueChanged(ListSelectionEvent e)
			{
			type = fills.getFillType();
			}
		}

	public static class FillTool extends GenericTool<FillAction> implements ListSelectionListener
		{
		FillType type;

		public void mousePress(MouseEvent e, Canvas c, Palette p)
			{
			if (active != null)
				{
				cancel(c);
				return;
				}
			
			Point fp;
			if (!isValid(e,c,p)) {
			  if (c.renderMode != RenderMode.TILED) return;
			  fp = e.getPoint();
			  fp.x = (fp.x + c.imageWidth()) % c.imageWidth();
			  fp.y = (fp.y + c.imageHeight()) % c.imageHeight();
			  System.out.println(fp.x + ", " + fp.y);
			  System.out.println(c.imageWidth() + ", " + c.imageHeight());
			}
			else fp = e.getPoint();

			Color c1 = p.getLeft();
			Color c2 = p.getRight();
			if (e.getButton() != MouseEvent.BUTTON1)
				{
				c1 = c2;
				c2 = p.getLeft();
				}
			switch (type)
				{
				case OUTLINE:
					c.active = active = new FillAction(c, c.getRenderImage(),fp,c1,null,0,type);
					break;
				case BOTH:
					c.active = active = new FillAction(c, c.getRenderImage(),fp,c1,c2,0,type);
					break;
				case FILL:
					c.active = active = new FillAction(c, c.getRenderImage(),fp,c1,c1,0,type);
					break;
				}

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

		private static final FillOptions fills = new FillOptions();

		@Override
		public JComponent getOptionsComponent()
			{
			return fills;
			}

		public FillTool()
			{
			fills.addListSelectionListener(this);
			fills.select(FillType.FILL);
			}

		public void valueChanged(ListSelectionEvent e)
			{
			type = fills.getFillType();
			}
		}

	public static class ColorPickerTool extends GenericTool<ImageAction>
		{
			//Necessary for dragging as dragging gives no button by default.
			private int button;

		public void mousePress(MouseEvent e, Canvas c, Palette p)
			{
			button = e.getButton();
			
			Point cp;
			if (!isValid(e,c,p)) {
			  if (c.renderMode != RenderMode.TILED) return;
			  cp = e.getPoint();
			  cp.x = (cp.x + c.imageWidth()) % c.imageWidth();
			  cp.y = (cp.y + c.imageHeight()) % c.imageHeight();
			  System.out.println(cp.x + ", " + cp.y);
			  System.out.println(c.imageWidth() + ", " + c.imageHeight());
			}
			else cp = e.getPoint();
			if (e.getButton() == MouseEvent.BUTTON1)
				p.setLeft(c.getColorAt(cp));
			else if (e.getButton() == MouseEvent.BUTTON3)
				p.setRight(c.getColorAt(cp));
			
			}

		public void mouseRelease(MouseEvent e, Canvas c, Palette p)
			{ //Unused
			}

		public void mouseMove(MouseEvent e, Canvas c, Palette p, boolean drag)
			{ if (drag)
					mousePress(new MouseEvent((Component) e.getSource(),e.getID(),e.getWhen(),e.getModifiers(),e.getX(),e.getY(),
							e.getClickCount(),e.isPopupTrigger(),button), c, p);
			}

		public JComponent getOptionsComponent()
			{
			return emptyPanel;
			}
		}

	public static class TextTool extends GenericTool<ImageAction>
		{
		public void mousePress(MouseEvent e, Canvas c, Palette p)
			{
			Color col;
			if (e.getButton() == MouseEvent.BUTTON1)
				col = p.getLeft();
			else if (e.getButton() == MouseEvent.BUTTON3)
				col = p.getRight();
			else
				return;
			
			Point cp;
			if (!isValid(e,c,p)) {
			  if (c.renderMode != RenderMode.TILED) return;
			  cp = e.getPoint();
			  cp.x = (cp.x + c.imageWidth()) % c.imageWidth();
			  cp.y = (cp.y + c.imageHeight()) % c.imageHeight();
			}
			else cp = e.getPoint();
			
			String text = JOptionPane.showInputDialog("Text");
			
			if (text == null)
				return;

			c.active = active = new TextAction(c, cp, col, opts.font, text, opts.halign, opts.valign);
			finish(c,p);
			}

		public void mouseRelease(MouseEvent e, Canvas c, Palette p)
			{ //Unused
			}

		public void mouseMove(MouseEvent e, Canvas c, Palette p, boolean drag)
			{ //Unused
			}

		private static final TextOptions opts = new TextOptions();

		public JComponent getOptionsComponent()
			{
			return opts;
			}
		}
	}
