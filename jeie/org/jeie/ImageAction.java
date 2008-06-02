/*
 * Copyright (C) 2008 IsmAvatar <cmagicj@nni.com>
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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/*
 * Note to subclassers:
 * Your responsibility aside from painting your action is
 * handling the mouse events for your action. For your
 * convenience, the relevant mouse events are already
 * implemented, you just need to override them where needed.
 * Don't bother calling the super method for them,
 * because they are all empty.
 */

public abstract class ImageAction extends MouseAdapter implements MouseMotionListener
	{
	protected Jeie j;

	/** Provided so subclasses don't bitch */
	protected ImageAction()
		{
		}

	public ImageAction(Jeie j)
		{
		this.j = j;
		}

	public abstract void paint(Graphics g);

	public static class Resize extends ImageAction
		{
		public int w, h;

		@Override
		public void paint(Graphics g)
			{
			g.setClip(0,0,w,h);
			g.clipRect(0,0,w,h);
			}
		}

	public static class Line extends ImageAction
		{
		public Color c;
		public int x, y, w, h;

		@Override
		public void paint(Graphics g)
			{
			g.setColor(c);
			g.drawLine(x,y,x + w,y + h);
			}
		}

	public static class Point extends ImageAction
		{
		public Color c;
		public int x, y;

		@Override
		public void paint(Graphics g)
			{
			g.setColor(c);
			g.drawLine(x,y,x,y);
			}
		}

	//Java 1.5 only: MouseAdapter does not implement these
	public void mouseDragged(MouseEvent e)
		{
		}

	public void mouseMoved(MouseEvent e)
		{
		}
	}
