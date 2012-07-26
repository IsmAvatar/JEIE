package org.jeie;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

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

		FloodFill(BufferedImage source, Point p, int threshold)
			{
			this.source = source;
			this.threshold = threshold;
			if (p.x < 0 || p.y < 0 || p.x >= source.getWidth() || p.y >= source.getHeight()) return;
			targetRGB = source.getRGB(p.x,p.y);
			floodFill(p);
			}

		protected void floodFill(Point start)
			{
			Queue<Point> q = new ArrayDeque<Point>();
			if (!needsFill(start)) return;
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

				for (int x = w.x; x < e.x; x++)
					{
					set.add(new Point(x,n.y));
					if (needsFill(new Point(x,n.y - 1))) q.add(new Point(x,n.y - 1));
					if (needsFill(new Point(x,n.y + 1))) q.add(new Point(x,n.y + 1));
					}
				}
			}

		protected boolean needsFill(Point p)
			{
			if (p.x < 0 || p.y < 0 || p.x >= source.getWidth() || p.y >= source.getHeight())
				return false;
			if (set.contains(p)) return false;
			return source.getRGB(p.x,p.y) == targetRGB;
			}
		}

	public static class EdgeDetect
		{
		public Set<Point> set = new HashSet<Point>();

		public EdgeDetect(FloodFill body)
			{
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
			}

		}
	}
