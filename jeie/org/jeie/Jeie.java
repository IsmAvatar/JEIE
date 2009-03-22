/*
 * Copyright (C) 2008, 2009 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2009 Serge Humphrey <bob@bobtheblueberry.com>
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

public class Jeie implements ActionListener
	{
	public JFrame f;
	public Canvas canvas;
	public JButton bUndo, bGrid, bZoomIn, bZoomOut;
	private JScrollPane scroll;

	public JMenuBar menuBar;
	public JMenu effectsMenu;

	public Jeie(BufferedImage image)
		{
		if (image == null) image = createBufferedImage(new Dimension(32,32));

		f = new JFrame("Easy Image Editor");
		f.setJMenuBar(makeMenuBar());
		JPanel p = new JPanel(new BorderLayout());
		f.setContentPane(p);
		p.add(makeToolBar(),BorderLayout.WEST);
		canvas = new Canvas(image);
		scroll = new JScrollPane(canvas);
		scroll.getVerticalScrollBar().setUnitIncrement(10);
		scroll.getHorizontalScrollBar().setUnitIncrement(10);
		p.add(scroll,BorderLayout.CENTER);
		f.setMinimumSize(new Dimension(200,200));
		f.pack();
		f.setLocationRelativeTo(null);
		f.setVisible(true);
		}

	public JMenuBar makeMenuBar()
		{
		menuBar = new JMenuBar();
		menuBar.add(new EffectsMenu(this));
		return menuBar;
		}

	public JToolBar makeToolBar()
		{
		JToolBar tool = new JToolBar(JToolBar.VERTICAL);
		tool.setLayout(new GridLayout(0,2));

		bUndo = addButton(tool,"<-");
		bGrid = addButton(tool,"Grid");
		bZoomOut = addButton(tool,"-");
		bZoomIn = addButton(tool,"+");

		return tool;
		}

	public JButton addButton(Container c, String label)
		{
		JButton b = new JButton(label);
		b.addActionListener(this);
		c.add(b);
		return b;
		}

	public static void main(String[] args)
		{
		BufferedImage bi = null;
		try
			{
			bi = ImageIO.read(Jeie.class.getResource("/test.png"));
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		Jeie j = new Jeie(bi);
		j.f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}

	public static BufferedImage createBufferedImage(Dimension size)
		{
		if (size == null) size = new Dimension(32,32);
		BufferedImage image = new BufferedImage(size.width,size.height,BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0,0,32,32);
		return image;
		}

	public static BufferedImage createBufferedImage(int w, int h)
		{
		BufferedImage image = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0,0,w,h);
		return image;
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == bUndo)
			{
			if (!canvas.acts.isEmpty())
				{
				canvas.acts.remove(canvas.acts.size() - 1);
				canvas.redrawCache();
				canvas.repaint();
				}
			return;
			}
		if (e.getSource() == bGrid)
			{
			canvas.drawGrid = !canvas.drawGrid;
			canvas.repaint();
			return;
			}
		if (e.getSource() == bZoomIn)
			{
			if (canvas.zoom < 32)
				{
				canvas.zoom *= 2;
				scroll.updateUI();
				}
			return;
			}
		if (e.getSource() == bZoomOut)
			{
			if (canvas.zoom > 1)
				{
				canvas.zoom /= 2;
				scroll.updateUI();
				}
			return;
			}
		}
	}
