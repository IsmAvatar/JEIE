/*
 * Copyright (C) 2008, 2009, 2012 IsmAvatar <IsmAvatar@gmail.com>
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
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;

import javax.swing.JLabel;

public class Canvas extends JLabel
	{
	private static final long serialVersionUID = 1L;
	private BufferedImage raster, cache, grid;
	public ImageAction active;

	public ArrayDeque<ImageAction> acts;
	private int zoom = 1;
	public boolean isGridDrawn = true;
	public final boolean invertGrid = true;

	public Canvas(BufferedImage image)
		{
		setOpaque(true);
		raster = image;
		acts = new ArrayDeque<ImageAction>();
		cache = new BufferedImage(raster.getWidth(),raster.getHeight(),BufferedImage.TYPE_INT_ARGB);
		}

	public void setImage(BufferedImage image)
		{
		raster = image;
		acts.clear();
		redrawCache();
		}

	public BufferedImage getRenderImage()
		{
		BufferedImage img = new BufferedImage(raster.getWidth(),raster.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();

		g.drawImage(raster,0,0,null);
		g.drawImage(cache,0,0,null);

		g.dispose();
		return img;
		}

	public Dimension getImageSize()
		{
		return new Dimension(raster.getWidth(),raster.getHeight());
		}

	@Override
	public Dimension getPreferredSize()
		{
		return new Dimension(cache.getWidth() * zoom + 1,cache.getHeight() * zoom + 1);
		}

	public void redrawCache()
		{
		cache = new BufferedImage(raster.getWidth(),raster.getHeight(),BufferedImage.TYPE_INT_ARGB);
		Graphics g = cache.getGraphics();
		for (ImageAction act : acts)
			act.paint(g);
		repaint();
		}

	public void redrawGrid()
		{
		int cw = cache.getWidth() * zoom;
		int ch = cache.getHeight() * zoom;

		grid = new BufferedImage(cw,ch,BufferedImage.TYPE_INT_ARGB);
		Graphics g = grid.getGraphics();
		paintGrid(g);
		}

	public void paintGrid(Graphics g)
		{
		int cw = cache.getWidth() * zoom;
		int ch = cache.getHeight() * zoom;

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
			repaint();
			}
		}

	public void zoomOut()
		{
		if (zoom > 1)
			{
			zoom /= 2;
			redrawGrid();
			repaint();
			}
		}

	public int getZoom()
		{
		return zoom;
		}

	@Override
	public void paint(Graphics g)
		{
		super.paint(g);

		int cw = cache.getWidth() * zoom;
		int ch = cache.getHeight() * zoom;

		g.drawImage(raster,0,0,raster.getWidth() * zoom,raster.getHeight() * zoom,null);
		g.drawImage(cache,0,0,cw,ch,null);
		BufferedImage activeImg = new BufferedImage(cache.getWidth(),cache.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		if (active != null) active.paint(activeImg.getGraphics());
		g.drawImage(activeImg,0,0,cw,ch,null);

		if (isGridDrawn && zoom >= 8)
			{
			if (invertGrid) g.setXORMode(Color.BLACK);
			g.drawImage(grid,0,0,null);
			if (invertGrid) g.setPaintMode();
			}

		g.clipRect(0,0,cw,ch);
		}
	}
