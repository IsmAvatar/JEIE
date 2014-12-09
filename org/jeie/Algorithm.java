/**
* @file  Algorithm.java
* @brief Effect utility class.
*
* @section License
*
* Copyright (C) 2014 JoshDreamland 
* 
* This file is a part of JEIE.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
**/

package org.jeie;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import org.jeie.Canvas.RenderMode;

public class Algorithm
	{

	/** 
	 * A class to calculate a set of points which match (in color comparison
	 * and spatial contiguity) a given point on a canvas. Useful for flood-fill
	 * operations and magic wand selection.
	 * @author IsmAvatar
	 */
	public static class FloodFill
		{
		//input
		BufferedImage source;
		int targetRGB;
		int threshold;

		//output
		public Set<Point> set = new HashSet<Point>();
		public int minX = Integer.MAX_VALUE, minY = minX, maxX = 0, maxY = 0;

		FloodFill(Canvas c, BufferedImage source, Point p, int threshold)
			{
			this.source = source;
			this.threshold = threshold;
			if (p.x < 0 || p.y < 0 || p.x >= source.getWidth() || p.y >= source.getHeight()) return;
			targetRGB = source.getRGB(p.x,p.y);
			floodFill(p,c);
			}

		protected void floodFill(Point start, Canvas c)
			{
			// Create a queue of points to fill around
			Queue<Point> q = new ArrayDeque<Point>();
			// Bounds checking
			if (start.x < 0 || start.y < 0 || start.x >= source.getWidth()
					|| start.y >= source.getHeight())
				{
				if (c.renderMode != RenderMode.TILED) return;
				System.out.print("what\n");
				start = new Point((start.x + source.getWidth()) % source.getWidth(),
						(start.y + source.getHeight()) % source.getHeight());
				}

			q.add(start);
			while (!q.isEmpty())
				{
				Point n = q.remove();
				if (!needsFill(n)) continue;
				Point w = new Point(n), e = new Point(n);
				do
					w.x--;
				while (needsFill(w));
				do
					e.x++;
				while (needsFill(e));

				w.x++;
				if (w.x < minX) minX = w.x;
				if (e.x - 1 > maxX) maxX = e.x - 1;
				if (n.y < minY) minY = n.y;
				if (n.y > maxY) maxY = n.y;

				if (c.renderMode != RenderMode.TILED)
					{
					for (int x = w.x; x < e.x; x++)
						set.add(new Point(x,n.y));
					if (n.y - 1 >= 0) for (int x = w.x; x < e.x; x++)
						if (needsFillInBound(new Point(x,n.y - 1))) q.add(new Point(x,n.y - 1));
					if (n.y + 1 < source.getHeight()) for (int x = w.x; x < e.x; x++)
						if (needsFillInBound(new Point(x,n.y + 1))) q.add(new Point(x,n.y + 1));
					}
				else
					{
					for (int x = w.x; x < e.x; x++)
						set.add(new Point(x,n.y));

					// Wrap horizontally
					if (w.x == 0)
						{
						Point wrap_p = new Point(maxX = source.getWidth() - 1,w.y);
						if (needsFillInBound(wrap_p)) q.add(wrap_p);
						}
					else if (e.x == source.getWidth())
						{
						Point wrap_p = new Point(minX = 0,e.y);
						if (needsFillInBound(wrap_p)) q.add(wrap_p);
						}

					// Wrap vertically
					if (n.y - 1 >= 0)
						for (int x = w.x; x < e.x; x++)
							{
							if (needsFillInBound(new Point(x,n.y - 1))) q.add(new Point(x,n.y - 1));
							}
					else
						{
						int h1 = source.getHeight() - 1;
						for (int x = w.x; x < e.x; x++)
							if (needsFillInBound(new Point(x,h1))) q.add(new Point(x,h1));
						}

					if (n.y + 1 < source.getHeight())
						for (int x = w.x; x < e.x; x++)
							{
							if (needsFillInBound(new Point(x,n.y + 1))) q.add(new Point(x,n.y + 1));
							}
					else
						for (int x = w.x; x < e.x; x++)
							if (needsFillInBound(new Point(x,0))) q.add(new Point(x,0));
					}
				}
			}

		protected boolean needsFillInBound(Point p)
			{
			// Return false if we've already filled this pixel
			if (set.contains(p)) return false;
			// Return whether this pixel matches the color we're filling
			return source.getRGB(p.x,p.y) == targetRGB; // TODO: Add threshold here
			}

		protected boolean needsFill(Point p)
			{
			if (p.x < 0 || p.y < 0 || p.x >= source.getWidth() || p.y >= source.getHeight())
				return false;
			return needsFillInBound(p);
			}
		}

	public static class EdgeDetect
		{
		public Set<Point> set = new HashSet<Point>();

		public EdgeDetect(FloodFill body, Canvas cv)
			{
			if (cv.renderMode != RenderMode.TILED)
				for (Point p : body.set)
					{
					Point c = new Point(p.x,p.y);
					c.x -= 1;
					if (!body.set.contains(c))
						{
						set.add(p);
						continue;
						}
					c.x += 2;
					if (!body.set.contains(c))
						{
						set.add(p);
						continue;
						}
					c.x -= 1;
					c.y -= 1;
					if (!body.set.contains(c))
						{
						set.add(p);
						continue;
						}
					c.y += 2;
					if (!body.set.contains(c))
						{
						set.add(p);
						continue;
						}
					}
			else
				for (Point p : body.set)
					{
					Dimension is = cv.getImageSize();
					Point c = new Point(p.x,p.y);
					c.x = (c.x + is.width - 1) % is.width;
					if (!body.set.contains(c))
						{
						set.add(p);
						continue;
						}
					c.x = (c.x + is.width + 2) % is.width;
					if (!body.set.contains(c))
						{
						set.add(p);
						continue;
						}
					c.x = p.x;
					c.y = (c.y + is.height - 1) % is.height;
					if (!body.set.contains(c))
						{
						set.add(p);
						continue;
						}
					c.y = (c.y + is.height + 2) % is.height;
					if (!body.set.contains(c))
						{
						set.add(p);
						continue;
						}
					}
			}

		}
	}
