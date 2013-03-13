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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

import org.jeie.Algorithm.EdgeDetect;
import org.jeie.Algorithm.FloodFill;
import org.jeie.Canvas.RenderMode;
import org.jeie.OptionComponent.FillOptions.FillType;

public abstract class ImageAction
	{
	public boolean copiesRaster = false;
	
	public abstract void paint(Graphics g);

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
	public static abstract class HeavyImageAction extends ImageAction
		{
		public abstract void recalculate(BufferedImage source);
		}

	public static class Resize extends ImageAction
		{
		public int w, h;

		public void paint(Graphics g)
			{
			g.setClip(0,0,w,h);
			g.clipRect(0,0,w,h);
			}
		}
	
	public static class TextAction extends ImageAction
	{
	public Color color;
	public Font font;
	public Point p;
	public Canvas canvas;
	public String text;
	public OptionComponent.TextOptions.Alignment halign, valign;
	
	public TextAction(Canvas canvas, Point p, Color col, Font f, String t, OptionComponent.TextOptions.Alignment h, OptionComponent.TextOptions.Alignment v)
		{
		this.canvas = canvas;
		this.p = p;
		color = col;
		font = f;
		text = t;
		halign = h;
		valign = v;
		}
	
	public void paint(Graphics g)
		{
		int x = (int) p.getX();
		int y = (int) p.getY();
		FontMetrics metrics = g.getFontMetrics(font);
		
		int w = metrics.stringWidth(text);
		int h = metrics.getHeight();
		
		if (halign == OptionComponent.TextOptions.Alignment.CENTER)
			x -= w / 2;
		else if (halign == OptionComponent.TextOptions.Alignment.RIGHT)
			x -= w;
		
		if (valign == OptionComponent.TextOptions.Alignment.MIDDLE)
			y += h / 2;
		else if (valign == OptionComponent.TextOptions.Alignment.TOP)
			y += h;
		
		g.setFont(font);
		g.setColor(color);
		
		if (canvas.renderMode != RenderMode.TILED)
			g.drawString(text,x,y);
		else
			{
			Dimension img = canvas.getImageSize();
			for (int dx = 0; x + w - dx >= 0; dx += img.width)
				for (int dy = 0; y + h - dy >= 0; dy += img.height)
					g.drawString(text,x - dx,y - dy);
			}
		}
	}

	public static class RectangleAction extends ImageAction
		{
		public Color out, in;
		public Point p1, p2;
		public Canvas canvas;

		public RectangleAction(Canvas canvas, Point p, Color out, Color in)
			{
			this.canvas = canvas;
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
				if (canvas.renderMode != RenderMode.TILED)
					g.drawRect(r.x,r.y,r.width,r.height);
				else
					{
					Dimension img = canvas.getImageSize();
					for (int dx = 0; r.x + r.width - dx >= 0; dx += img.width)
						for (int dy = 0; r.y + r.height - dy >= 0; dy += img.height)
							g.drawRect(r.x - dx,r.y - dy,r.width,r.height);
					}
				}
			if (in != null)
				{
				g.setColor(in);
				if (canvas.renderMode != RenderMode.TILED)
					g.fillRect(r.x + 1,r.y + 1,r.width - 1,r.height - 1);
				else
					{
					Dimension img = canvas.getImageSize();
					for (int dx = 0; r.x + r.width - dx >= 0; dx += img.width)
						for (int dy = 0; r.y + r.height - dy >= 0; dy += img.height)
							g.fillRect(r.x - dx + 1,r.y - dy + 1,r.width - 1,r.height - 1);
					}
				}
			}
		}

	public static class OvalAction extends ImageAction
		{
		public Color out, in;
		public Point p1, p2;
		public Canvas canvas;

		public OvalAction(Canvas canvas, Point p, Color out, Color in)
			{
			this.canvas = canvas;
			this.out = out;
			this.in = in;
			p1 = p;
			p2 = p;
			}

		public void paint(Graphics g)
			{
			Rectangle r = new Rectangle(p1);
			r.add(p2);
			if (in != null)
				{
				g.setColor(in);
				if (canvas.renderMode != RenderMode.TILED)
					g.fillOval(r.x,r.y,r.width,r.height);
				else
					{
					Dimension img = canvas.getImageSize();
					for (int dx = 0; r.x + r.width - dx >= 0; dx += img.width)
						for (int dy = 0; r.y + r.height - dy >= 0; dy += img.height)
							g.fillOval(r.x - dx,r.y - dy,r.width,r.height);
					}
				}
			if (out != null)
				{
				g.setColor(out);
				if (canvas.renderMode != RenderMode.TILED)
					g.drawOval(r.x,r.y,r.width,r.height);
				else
					{
					Dimension img = canvas.getImageSize();
					for (int dx = 0; r.x + r.width - dx >= 0; dx += img.width)
						for (int dy = 0; r.y + r.height - dy >= 0; dy += img.height)
							g.drawOval(r.x - dx,r.y - dy,r.width,r.height);
					}
				}
			}
		}

	public static class LineAction extends ImageAction
		{
		public Color c;
		public Point p1, p2;
		public int diameter;
		Canvas canvas;

		public LineAction(Canvas canvas, Point p, Color c, int diam)
			{
			this.c = c;
			this.canvas = canvas;
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
			if (canvas.renderMode != RenderMode.TILED)
				g2d.drawLine(p1.x,p1.y,p2.x,p2.y);
			else
				{
				Dimension img = canvas.getImageSize();
				for (int dx = 0; p2.x - dx >= 0 || p1.x - dx >= 0; dx += img.width)
					for (int dy = 0; p2.y - dy >= 0 || p1.y - dy >= 0; dy += img.height)
						g2d.drawLine(p1.x - dx,p1.y - dy,p2.x - dx,p2.y - dy);
				}
			g2d.setStroke(s);
			}
		}

	public static class GradientAction extends ImageAction
		{
		public Color c1, c2;
		public Point p1, p2;
		public OptionComponent.GradientOptions.GradientType type;
		Canvas canvas;

		public GradientAction(Canvas canvas, Point p, Color c1, Color c2, OptionComponent.GradientOptions.GradientType type)
			{
			this.c1 = c1;
			this.c2 = c2;
			this.canvas = canvas;
			p1 = p;
			p2 = p;
			this.type = type;
			}

		public void paint(Graphics g)
			{
			Paint paint = null;
			
			if (p1.equals(p2))
				paint = c2;
			else
				switch(type)
				{
				case LINEAR:
					paint = new LinearGradientPaint(p1, p2, new float[] { 0f, 1f }, new Color[] { c1, c2 });
					break;
					
				case MIRRORED:
					int xDist = (p2.x - p1.x);
					int yDist = (p2.y - p1.y);
					Point newPoint = new Point(p1.x - xDist, p1.y - yDist);
					paint = new LinearGradientPaint(newPoint, p2, new float[] { 0f, .5f, 1f }, new Color[] { c2, c1, c2 });
					break;
					
				case RADIAL:
					paint = new RadialGradientPaint(p1, (float) p1.distance(p2), new float[] { 0f, 1f }, new Color[] { c1, c2 });
					break;
				
				case CONICAL:
					paint = c2; //TODO: implement this.
					break;
					
				case SQUARE:
					paint = c2; //TODO: implement this.
					break;
				}
			
			Graphics2D g2d = (Graphics2D) g;
			Paint oldPaint = g2d.getPaint();
			g2d.setPaint(paint);
			g2d.fillRect(0,0,canvas.getWidth(),canvas.getHeight());
			g2d.setPaint(oldPaint);
			}
		}

	public static class PointAction extends ImageAction
		{
		static final int MAX_FREE_POINTS = 64;

		public Canvas canvas;
		public Color c;
		LinkedList<Point> pts = new LinkedList<Point>();
		BufferedImage cache;
		int cacheX, cacheY;
		int minX = Integer.MAX_VALUE, minY = minX, maxX = 0, maxY = 0;

		public PointAction(Canvas canvas, Color c)
			{
			this.canvas = canvas;
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
			paint(g,-newMinX,-newMinY);

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
				else if (p.x > canvas.imageWidth()) minX = Math.min(minX,0);
				if (p.y < minY) minY = p.y;
				else if (p.y > canvas.imageHeight()) minY = Math.min(minY,0);
				if (p.x > maxX) maxX = p.x;
				if (p.y > maxY) maxY = p.y;
				if (pts.size() > MAX_FREE_POINTS) toCache();
				}
			}

		public void paint(Graphics g)
			{
			paint(g,0,0);
			}

		public void paint(Graphics g, int shiftX, int shiftY)
			{
			if (cache != null) g.drawImage(cache,cacheX + shiftX,cacheY + shiftY,null);
			g.setColor(c);
			Point prevPoint = pts.getFirst();
			if (canvas.renderMode != RenderMode.TILED)
				for (Point p : pts)
					{
					g.drawLine(prevPoint.x + shiftX,prevPoint.y + shiftY,p.x + shiftX,p.y + shiftY);
					prevPoint = p;
					}
			else
				{
				Dimension img = canvas.getImageSize();
				for (int dx = 0; dx <= maxX + shiftX; dx += img.width)
					for (int dy = 0; dy <= maxY + shiftY; dy += img.height)
						{
						for (Point p : pts)
							{
							g.drawLine(prevPoint.x + shiftX - dx,prevPoint.y + shiftY - dy,p.x + shiftX - dx,p.y
									+ shiftY - dy);
							prevPoint = p;
							}
						prevPoint = pts.getFirst();
						}
				}
			}
		}

	public static class FillAction extends HeavyImageAction
		{
		Point origin;
		int threshold;
		Color c1, c2;
		Canvas canvas;
		BufferedImage source, myCache;
		FloodFill floodFill;
		EdgeDetect floodEdge = null;

		public FillAction(Canvas canv, BufferedImage source, Point origin, Color c1, Color c2,
				int threshold, FillType fillType)
			{
			this.source = source;
			this.origin = origin;
			this.c1 = c1;
			this.c2 = c2;
			this.threshold = threshold;
			this.canvas = canv;
			}

		protected BufferedImage getCache()
			{
			if (floodFill != null) return myCache;
			floodFill = new FloodFill(canvas,source,origin,threshold);
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

			floodEdge = new EdgeDetect(floodFill,canvas);
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
