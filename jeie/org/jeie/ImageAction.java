/*
 * Copyright (C) 2008 IsmAvatar <IsmAvatar@gmail.com>
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
import java.awt.Point;

public interface ImageAction
	{
	public void paint(Graphics g);

	public static class Resize implements ImageAction
		{
		public int w, h;

		public void paint(Graphics g)
			{
			g.setClip(0,0,w,h);
			g.clipRect(0,0,w,h);
			}
		}

	public static class LineAction implements ImageAction
		{
		public Color c;
		public Point p1, p2;

		public LineAction(Point p, Color c)
			{
			this.c = c;
			p1 = p;
			p2 = p;
			}

		public void paint(Graphics g)
			{
			g.setColor(c);
			g.drawLine(p1.x,p1.y,p2.x,p2.y);
			}
		}

	//TODO: Store multiple points per PointAction to prevent cache flooding
	public static class PointAction implements ImageAction
		{
		public Color c;
		public Point p;

		public void paint(Graphics g)
			{
			g.setColor(c);
			g.drawLine(p.x,p.y,p.x,p.y);
			}
		}
	}
