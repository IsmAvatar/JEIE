/*
 * Copyright (C) 2008, 2009, 2012 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of Jeie.
 * Jeie is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.jeie;

import java.awt.Color;
import java.awt.event.MouseEvent;

import org.jeie.ImageAction.LineAction;
import org.jeie.Jeie.Tool;

public class LineDrawer implements Tool
	{
	long mouseTime;
	LineAction active;
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
		Color col = p.getSelectedColor(e.getButton());
		if (col == null) return;
		button = e.getButton();
		mouseTime = e.getWhen();
		canvas.active = active = new LineAction(e.getPoint(),col);
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
			active.p2 = e.getPoint();
			canvas.repaint();
			}
		}

	public void finish(Canvas canvas, Palette p)
		{
		if (active != null)
			{
			canvas.acts.add(active);
			canvas.active = active = null;
			canvas.redrawCache();
			}
		}

	public void cancel(Canvas canvas)
		{
		canvas.active = active = null;
		canvas.repaint();
		}

	//Unused
	public void mouseClicked(MouseEvent e)
		{ //Unused
		}

	public void mouseEntered(MouseEvent e)
		{ //Unused
		}

	public void mouseExited(MouseEvent e)
		{ //Unused
		}
	}
