/*
 * Copyright (C) 2008, 2009, 2012 IsmAvatar <IsmAvatar@gmail.com>
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;

import javax.swing.JLabel;

public class Canvas extends JLabel
	{
	private static final long serialVersionUID = 1L;
	private BufferedImage raster, cache, grid;
	public ImageAction active;

	public ArrayDeque<ImageAction> acts, redoActs;
	private int zoom = 1, curAct;
	public boolean isGridDrawn = true;
	public boolean usesCheckeredBackground = true;
	public Color transBack1 = Color.white;
	public Color transBack2 = Color.lightGray;
	public Dimension trSize = new Dimension(8, 8);
	public final boolean invertGrid = true;
	private Dimension prevSize;

	public enum RenderMode
		{
		/** The standard paint-esque display; just draw the canvas. */
		NORMAL,
		/** Draw the canvas EVERYWHERE. */
		TILED,
		/** Draw the canvas, then draw it at 1/2 precision, then 1/4 precision. */
		SCALED
		}

	public RenderMode renderMode = RenderMode.NORMAL;

	public Canvas(BufferedImage image)
		{
		setOpaque(true);
		raster = image;
		acts = new ArrayDeque<ImageAction>();
		redoActs = new ArrayDeque<ImageAction>();
		cache = new BufferedImage(raster.getWidth(),raster.getHeight(),BufferedImage.TYPE_INT_ARGB);
		prevSize = getSize();
		}

	public void setImage(BufferedImage image)
		{
		raster = image;
		acts.clear();
		redoActs.clear();
		redrawCache();
		}

	public BufferedImage getRenderImage()
		{
		BufferedImage img = new BufferedImage(raster.getWidth(),raster.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		
		if (shouldDrawRaster())
			g.drawImage(raster,0,0,null);
		g.drawImage(cache,0,0,null);

		g.dispose();
		return img;
		}

	public Dimension getImageSize()
		{
		return new Dimension(raster.getWidth(),raster.getHeight());
		}
	public int imageWidth()
		{
		return raster.getWidth();
		}
	public int imageHeight()
		{
		return raster.getHeight();
		}

	@Override
	public Dimension getPreferredSize()
		{
		switch (renderMode)
			{
			case NORMAL:
			default:
				return new Dimension(cache.getWidth() * zoom + 1,cache.getHeight() * zoom + 1);
			case SCALED:
				return new Dimension((cache.getWidth() * zoom + 1) * 7 / 4,cache.getHeight() * zoom + 1);
			case TILED:
				return new Dimension((cache.getWidth() * zoom + 1) * 4,(cache.getHeight() * zoom + 1) * 4);
			}
		}

	public void redrawCache()
		{
		cache = new BufferedImage(raster.getWidth(),raster.getHeight(),BufferedImage.TYPE_INT_ARGB);
		Graphics g = cache.getGraphics();
		curAct = 0;
		for (ImageAction act : acts)
			{
			act.paint(g);
			curAct++;
			}
		repaint();
		}

	public void redrawGrid()
		{
		int cw, ch;
		if (renderMode == RenderMode.TILED)
			{
			cw = getWidth();
			ch = getHeight();
			}
		else
			{
			cw = cache.getWidth() * zoom;
			ch = cache.getHeight() * zoom;
			}

		grid = new BufferedImage(cw,ch,BufferedImage.TYPE_INT_ARGB);
		Graphics g = grid.getGraphics();
		paintGrid(g);
		}

	public void paintGrid(Graphics g)
		{
		int cw, ch;
		if (renderMode == RenderMode.TILED)
			{
			cw = getWidth();
			ch = getHeight();
			}
		else
			{
			cw = cache.getWidth() * zoom;
			ch = cache.getHeight() * zoom;
			}

		g.setColor(invertGrid ? Color.WHITE : Color.GRAY);

		for (int y = 0; y <= ch; y += zoom)
			g.drawLine(0,y,cw,y);
		for (int x = 0; x <= cw; x += zoom)
			g.drawLine(x,0,x,ch);
		}

	public void setZoom(int zoom)
		{
		this.zoom = zoom;
		if (zoom >= 8) redrawGrid();
		}

	public void zoomIn()
		{
		if (zoom < 32)
			{
			zoom *= 2;
			redrawGrid();
			updateUI();
			}
		}

	public void zoomOut()
		{
		if (zoom > 1)
			{
			zoom /= 2;
			redrawGrid();
			updateUI();
			}
		}

	public int getZoom()
		{
		return zoom;
		}
	
	public Color getColorAt(Point p) {
		return new Color(getRenderImage().getRGB((int) p.getX(), (int) p.getY()), true);
	}
	
	public void drawTransparentBackground(Graphics g)
		{
		int tW = (int) trSize.getWidth();
		int tH = (int) trSize.getHeight();
		int fullW, fullH;
		if (renderMode == RenderMode.TILED)
			{
			fullW = getWidth();
			fullH = getHeight();
			}
		else
			{
			fullW = raster.getWidth() * zoom;
			fullH = raster.getHeight() * zoom;
			}
		g.setColor(transBack1);
		g.fillRect(0, 0, fullW, fullH);
		g.setColor(transBack2);
		for (int x = 0; x < Math.ceil((double) fullW / tW); x += 1)
			for (int y = (x + 1) % 2; y < Math.ceil((double) fullH / tH); y += 2)
				g.fillRect(x * tW, y * tH, Math.min(tW, fullW - x * tW), Math.min(tH, fullH - y * tH));
		}
	
	public boolean shouldDrawRaster()
		{
		int i = 0;
		for (ImageAction a : acts)
			{
			if (i >= curAct)
				break;
			
			if (a.copiesRaster())
				return false;
			
			i++;
			}
		
		return true;
		}

	public void repaint(Rectangle r)
		{
		if (renderMode != RenderMode.TILED) {
			r.x *= zoom;
			r.y *= zoom;
			r.width = (r.width + 1) * zoom;
			r.height = (r.height + 1) * zoom;
		}
		else
		  r = new Rectangle(0,0,getWidth(),getHeight());
		super.repaint(r);
		}

	@Override
	public void paint(Graphics g)
		{
		super.paint(g);
		
		if (!getSize().equals(prevSize))
			{
			redrawGrid();
			prevSize = getSize();
			}
		int cw = cache.getWidth() * zoom;
		int ch = cache.getHeight() * zoom;

		drawTransparentBackground(g);
		boolean drawRaster = shouldDrawRaster();
		
		if (renderMode == RenderMode.NORMAL)
			{
			if (drawRaster)
				g.drawImage(raster,0,0,raster.getWidth() * zoom,raster.getHeight() * zoom,null);
			g.drawImage(cache,0,0,cw,ch,null);
			}
		else if (renderMode == RenderMode.TILED)
			{
			for (int i = 0; i < getWidth(); i += cw)
				for (int j = 0; j < getHeight(); j += ch)
					{
					if (drawRaster)
						g.drawImage(raster,i,j,raster.getWidth() * zoom,raster.getHeight() * zoom,null);
					g.drawImage(cache,i,j,cw,ch,null);
					}
			}

		if (active != null)
			{
			BufferedImage activeImg = new BufferedImage(cache.getWidth(),cache.getHeight(),
					BufferedImage.TYPE_INT_ARGB);
			active.paint(activeImg.getGraphics());
			if (renderMode == RenderMode.NORMAL)
			  g.drawImage(activeImg,0,0,cw,ch,null);
			else if (renderMode == RenderMode.TILED) {
			  for (int i = 0; i < 4 || i < getWidth(); i += cw)
			  	for (int j = 0; j < 4 || j < getHeight(); j += ch)
			  		g.drawImage(activeImg,i,j,cw,ch,null);
			}
			}

		if (isGridDrawn && zoom >= 8)
			{
			if (invertGrid) g.setXORMode(Color.BLACK);
			g.drawImage(grid,0,0,null);
			if (invertGrid) g.setPaintMode();
			}

		g.clipRect(0,0,cw,ch);
		}
	}
