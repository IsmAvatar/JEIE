/*
 * Copyright (C) 2008, 2012 IsmAvatar <IsmAvatar@gmail.com>
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

import org.jeie.Algorithm.EdgeDetect;
import org.jeie.Algorithm.FloodFill;
import org.jeie.OptionComponent.FillOptions.FillType;

public interface ImageAction
	{
	public void paint(Graphics g);

	/**
	 * A Heavy-weight Image Action is an Image Action that depends on
	 * the raster produced immediately prior to the action. As a result,
	 * if any prior actions are altered, heavy-weight actions will need
	 * to recalculate based on the new prior raster.
	 * <p>
	 * Generally, a user should be warned about this fact, as it may yield
	 * undesirable results, and should potentially be given the option to
	 * destroy or even manually update later heavy-weight image actions.
	 */
	public static interface HeavyImageAction extends ImageAction
		{
		public void recalculate(BufferedImage source);
		}

	public static class Resize implements ImageAction
		{
		public int w, h;

		public void paint(Graphics g)
			{
			g.setClip(0,0,w,h);
			g.clipRect(0,0,w,h);
			}
		}

	public static class RectangleAction implements ImageAction
		{
		public Color out, in;
		public Point p1, p2;

		public RectangleAction(Point p, Color out, Color in)
			{
			this.out = out;
			this.in = in;
			p1 = p;
			p2 = p;
			}

		public void paint(Graphics g)
			{
			Rectangle r = new Rectangle(p1);
			r.add(p2);
			if (out != null)
				{
				g.setColor(out);
				g.drawRect(r.x,r.y,r.width,r.height);
				}
			if (in != null)
				{
				g.setColor(in);
				g.fillRect(r.x + 1,r.y + 1,r.width - 1,r.height - 1);
				}
			}
		}

	public static class LineAction implements ImageAction
		{
		public Color c;
		public Point p1, p2;
		public int diameter;

		public LineAction(Point p, Color c, int diam)
			{
			this.c = c;
			p1 = p;
			p2 = p;
			diameter = diam;
			}

		public void paint(Graphics g)
			{
			g.setColor(c);
			Graphics2D g2d = (Graphics2D) g;
			Stroke s = g2d.getStroke();
			g2d.setStroke(new BasicStroke(diameter));//diameter>2?diameter:diameter/2));
			g2d.drawLine(p1.x,p1.y,p2.x,p2.y);
			g2d.setStroke(s);
			}
		}

	public static class PointAction implements ImageAction
		{
		static final int MAX_FREE_POINTS = 64;

		public Color c;
		LinkedList<Point> pts = new LinkedList<Point>();
		BufferedImage cache;
		int cacheX, cacheY;
		int minX = Integer.MAX_VALUE, minY = minX, maxX = 0, maxY = 0;

		public PointAction(Color c)
			{
			this.c = c;
			}

		protected void toCache()
			{
			int newMinX = minX;
			int newMinY = minY;
			int w = maxX - minX + 1;
			int h = maxY - minY + 1;
			if (cache != null)
				{
				newMinX = Math.min(minX,cacheX);
				newMinY = Math.min(minY,cacheY);
				int newMaxX = Math.max(maxX,cache.getWidth() + cacheX);
				int newMaxY = Math.max(maxY,cache.getHeight() + cacheY);

				w = newMaxX - newMinX + 1;
				h = newMaxY - newMinY + 1;
				}

			BufferedImage newCache = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
			Graphics g = newCache.createGraphics();
			if (cache != null) g.drawImage(cache,cacheX - newMinX,cacheY - newMinY,null);
			g.setColor(c);
			Point prevPoint = pts.getFirst();
			for (Point p : pts)
				{
				g.drawLine(prevPoint.x - newMinX,prevPoint.y - newMinY,p.x - newMinX,p.y - newMinY);
				prevPoint = p;
				}

			//reset our vars
			cache = newCache;
			cacheX = newMinX;
			cacheY = newMinY;
			Point p = pts.getLast();
			pts.clear();
			pts.add(p);
			minX = p.x;
			minY = p.y;
			maxX = p.x;
			maxY = p.y;
			}

		public void add(Point p)
			{
			if (pts.add(p))
				{
				if (p.x < minX) minX = p.x;
				if (p.y < minY) minY = p.y;
				if (p.x > maxX) maxX = p.x;
				if (p.y > maxY) maxY = p.y;
				if (pts.size() > MAX_FREE_POINTS) toCache();
				}
			}

		public void paint(Graphics g)
			{
			if (cache != null) g.drawImage(cache,cacheX,cacheY,null);
			g.setColor(c);
			Point prevPoint = pts.getFirst();
			for (Point p : pts)
				{
				g.drawLine(prevPoint.x,prevPoint.y,p.x,p.y);
				prevPoint = p;
				}
			}
		}

	public static class FillAction implements HeavyImageAction
		{
		Point origin;
		int threshold;
		Color c1, c2;
		BufferedImage source, myCache;
		FloodFill floodFill;
		EdgeDetect floodEdge = null;

		public FillAction(BufferedImage source, Point origin, Color c1, Color c2, int threshold,
				FillType fillType)
			{
			this.source = source;
			this.origin = origin;
			this.c1 = c1;
			this.c2 = c2;
			this.threshold = threshold;
			}

		protected BufferedImage getCache()
			{
			if (floodFill != null) return myCache;
			floodFill = new FloodFill(source,origin,threshold);
			floodEdge = null;

			int w = floodFill.maxX - floodFill.minX + 1;
			int h = floodFill.maxY - floodFill.minY + 1;

			int rgb = c1.getRGB();
			myCache = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
			if (c1.equals(c2))
				{
				for (Point p : floodFill.set)
					myCache.setRGB(p.x - floodFill.minX,p.y - floodFill.minY,rgb);
				return myCache;
				}

			floodEdge = new EdgeDetect(floodFill);
			if (c2 != null)
				{
				int rgb2 = c2.getRGB();
				for (Point p : floodFill.set)
					myCache.setRGB(p.x - floodFill.minX,p.y - floodFill.minY,rgb2);
				}
			for (Point p : floodEdge.set)
				myCache.setRGB(p.x - floodFill.minX,p.y - floodFill.minY,rgb);
			return myCache;
			}

		public void recalculate(BufferedImage source)
			{
			floodFill = null;
			this.source = source;
			}

		public void paint(Graphics g)
			{
			g.drawImage(getCache(),floodFill.minX,floodFill.minY,null);
			}
		}
	}
