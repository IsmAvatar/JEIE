/*
 * Copyright (C) 2008, 2009, 2012 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of Jeie.
 * Jeie is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.jeie;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.jeie.ImageAction.LineAction;

public class LineDrawer implements MouseListener,MouseMotionListener
	{
	ButtonModel bPoint, bLine;
	ButtonGroup bg;

	Canvas canvas;
	Palette pal;

	public LineDrawer(JToolBar tool, Canvas c, Palette p)
		{
		canvas = c;
		pal = p;
		bg = new ButtonGroup();

		JToggleButton b = new JToggleButton("Pt");
		bg.add(b);
		tool.add(b);
		bPoint = b.getModel();

		b = new JToggleButton("Ln",true);
		bg.add(b);
		tool.add(b);
		bLine = b.getModel();

		c.addMouseListener(this);
		c.addMouseMotionListener(this);
		}

	///////////
	// Mouse //
	///////////

	long mouseTime;

	public void mousePressed(MouseEvent e)
		{
		Point p = e.getPoint();
		Point l = new Point(p.x / canvas.zoom,p.y / canvas.zoom);
		ButtonModel sel = bg.getSelection();
		if (sel == bLine)
			{
			if (canvas.active != null && canvas.active instanceof LineAction) return;
			Color c = pal.getSelectedColor(e.getButton());
			if (c == null) return;
			canvas.active = new LineAction(l,c);
			mouseTime = e.getWhen();
			}
		}

	public void mouseReleased(MouseEvent e)
		{
		if (e.getWhen() - mouseTime < 200) return;
		Point p = e.getPoint();
		Point l = new Point(p.x / canvas.zoom,p.y / canvas.zoom);
		ButtonModel sel = bg.getSelection();
		if (sel == bLine)
			{
			ImageAction ia = canvas.active;
			LineAction la = null;
			if (ia instanceof LineAction) la = (LineAction) ia;
			if (la != null)
				{
				la.p2 = l;
				canvas.acts.add(la);
				canvas.active = null;
				canvas.redrawCache();
				}
			}
		}

	public void mouseDragged(MouseEvent e)
		{
		mouseMoved(e);
		}

	public void mouseMoved(MouseEvent e)
		{
		ImageAction ia = canvas.active;
		if (ia instanceof LineAction)
			{
			Point p = e.getPoint();
			((LineAction) ia).p2 = new Point(p.x / canvas.zoom,p.y / canvas.zoom);
			}
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
