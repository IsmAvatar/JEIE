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

	public LineDrawer(JToolBar tool, Canvas c)
		{
		canvas = c;
		bg = new ButtonGroup();

		JToggleButton b = new JToggleButton("P");
		bg.add(b);
		tool.add(b);
		bPoint = b.getModel();

		b = new JToggleButton("L",true);
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

	@Override
	public void mousePressed(MouseEvent e)
		{
		Point p = e.getPoint();
		Point l = new Point(p.x / canvas.zoom,p.y / canvas.zoom);
		ButtonModel sel = bg.getSelection();
		if (sel == bLine)
			{
			ImageAction ia = canvas.active;
			LineAction la = null;
			if (ia instanceof LineAction) la = (LineAction) ia;
			if (la != null) return;
			la = new LineAction(l,Color.BLACK);
			canvas.active = la;
			mouseTime = e.getWhen();
			}
		}

	@Override
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

	@Override
	public void mouseDragged(MouseEvent e)
		{
		mouseMoved(e);
		}

	@Override
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
	@Override
	public void mouseClicked(MouseEvent e)
		{
		}

	@Override
	public void mouseEntered(MouseEvent e)
		{
		}

	@Override
	public void mouseExited(MouseEvent e)
		{
		}
	}
