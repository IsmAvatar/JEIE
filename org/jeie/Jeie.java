/*
 * Copyright (C) 2008, 2009, 2012 IsmAvatar <IsmAvatar@gmail.com>
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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.jeie.Tool.FillTool;
import org.jeie.Tool.LineTool;
import org.jeie.Tool.PointTool;

public class Jeie implements ActionListener
	{
	private JFrame f;
	private JButton bUndo, bZoomIn, bZoomOut;
	private JToggleButton bGrid;
	private JScrollPane scroll;

	public Canvas canvas;
	public Palette pal;
	public ToolDelegate del;
	private JMenuBar menuBar;
	private JToolBar toolBar;

	public Jeie(BufferedImage image)
		{
		if (image == null) image = createWhiteBufferedImage(32,32);
		pal = new Palette();
		canvas = new Canvas(image);
		scroll = new JScrollPane(canvas);

		del = new ToolDelegate();
		canvas.addMouseListener(del);
		canvas.addMouseMotionListener(del);
		canvas.addMouseWheelListener(del);

		JPanel p = new JPanel(new BorderLayout());
		p.add(makeToolBar(),BorderLayout.WEST);
		scroll.getVerticalScrollBar().setUnitIncrement(10);
		scroll.getHorizontalScrollBar().setUnitIncrement(10);
		p.add(scroll,BorderLayout.CENTER);
		p.add(pal,BorderLayout.SOUTH);

		f = new JFrame("Easy Image Editor");
		f.setJMenuBar(makeMenuBar());
		f.setContentPane(p);
		f.setMinimumSize(new Dimension(500,500));
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
		toolBar = new JToolBar(JToolBar.VERTICAL);
		toolBar.setLayout(new GridLayout(0,2));

		bUndo = setupButton(toolBar,new JButton("Undo"));
		bGrid = setupButton(toolBar,new JToggleButton("Grid",true));

		bZoomOut = setupButton(toolBar,new JButton("Z-"));
		bZoomIn = setupButton(toolBar,new JButton("Z+"));

		ButtonGroup bg = new ButtonGroup();
		setupButton(toolBar,new ToolButton("Pt",bg,new PointTool()));
		ToolButton tb = setupButton(toolBar,new ToolButton("Ln",bg,new LineTool()));
		setupButton(toolBar,new ToolButton("Fill",bg,new FillTool()));

		//select our default button
		tb.doClick();

		return toolBar;
		}

	public <K extends AbstractButton>K setupButton(Container c, K b)
		{
		c.add(b);
		b.addActionListener(this);
		return b;
		}

	class ToolButton extends JToggleButton
		{
		private static final long serialVersionUID = 1L;

		public final Tool tool;

		public ToolButton(String label, ButtonGroup bg, Tool t, boolean sel)
			{
			super(label,sel);
			tool = t;
			bg.add(this);
			}

		public ToolButton(String label, ButtonGroup bg, Tool t)
			{
			this(label,bg,t,false);
			}
		}

	class ToolDelegate extends MouseAdapter
		{
		Tool tool;

		MouseEvent refactor(MouseEvent e)
			{
			int x = e.getX() / canvas.getZoom();
			int y = e.getY() / canvas.getZoom();
			return new MouseEvent((Component) e.getSource(),e.getID(),e.getWhen(),e.getModifiers(),x,y,
					e.getClickCount(),e.isPopupTrigger(),e.getButton());
			}

		public void mousePressed(MouseEvent e)
			{
			if (tool != null) tool.mousePress(refactor(e),canvas,pal);
			}

		public void mouseReleased(MouseEvent e)
			{
			if (tool != null) tool.mouseRelease(refactor(e),canvas,pal);
			}

		public void mouseDragged(MouseEvent e)
			{
			if (tool != null) tool.mouseMove(refactor(e),canvas,pal,true);
			}

		public void mouseMoved(MouseEvent e)
			{
			if (tool != null) tool.mouseMove(refactor(e),canvas,pal,false);
			}

		public void mouseWheelMoved(MouseWheelEvent e)
			{
			if (e.isControlDown())
				{
				int rot = e.getWheelRotation();
				if (rot < 0)
					canvas.zoomIn();
				else if (rot > 0) canvas.zoomOut();
				}
			}
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

	public static BufferedImage createWhiteBufferedImage(int w, int h)
		{
		BufferedImage image = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0,0,w,h);
		return image;
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() instanceof ToolButton)
			{
			ToolButton tb = (ToolButton) e.getSource();
			del.tool = tb.tool;
			return;
			}
		if (e.getSource() == bUndo)
			{
			if (!canvas.acts.isEmpty())
				{
				canvas.acts.removeLast();
				canvas.redrawCache();
				}
			return;
			}
		if (e.getSource() == bGrid)
			{
			canvas.isGridDrawn = bGrid.isSelected();
			canvas.repaint();
			return;
			}
		if (e.getSource() == bZoomIn)
			{
			canvas.zoomIn();
			return;
			}
		if (e.getSource() == bZoomOut)
			{
			canvas.zoomOut();
			return;
			}
		}
	}
