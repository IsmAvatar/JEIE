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
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.jeie.Tool.FillTool;
import org.jeie.Tool.LineTool;
import org.jeie.Tool.PointTool;
import org.jeie.Tool.RectangleTool;

public class Jeie implements ActionListener,WindowListener
	{
	private JFrame frame;
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

		frame = new JFrame("Easy Image Editor");
		frame.setJMenuBar(makeMenuBar());
		frame.setContentPane(p);
		frame.setMinimumSize(new Dimension(500,500));
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.addWindowListener(this);
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

		bUndo = setupButton(toolBar,new JButton("Undo",getIcon("undo")));
		bGrid = setupButton(toolBar,new JToggleButton("Grid",true));

		bZoomOut = setupButton(toolBar,new JButton(getIcon("zoom-out")));
		bZoomIn = setupButton(toolBar,new JButton(getIcon("zoom-in")));
		ButtonGroup bg = new ButtonGroup();
		setupButton(toolBar,new ToolButton("Pt",bg,new PointTool()));
		ToolButton tb = setupButton(toolBar,new ToolButton("Ln",bg,new LineTool()));
		setupButton(toolBar,new ToolButton("Rect",bg,new RectangleTool()));
		setupButton(toolBar,new ToolButton("Fill",bg,new FillTool()));

		// select our default button
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

	// LGM code
	private static ImageIcon getIcon(String name)
		{
		String filename = name.toLowerCase() + ".png";
		String location = "org/jeie/icons/actions/" + filename;
		ImageIcon ico = new ImageIcon(location);
		if (ico.getIconWidth() == -1)
			{
			URL url = Jeie.class.getClassLoader().getResource(location);
			if (url != null)
				{
				ico = new ImageIcon(url);
				}
			}
		return ico;
		}

	class ToolDelegate extends MouseAdapter
		{
		protected Tool tool;

		public void setTool(Tool t)
			{
			if (tool != null) tool.finish(canvas,pal);
			tool = t;
			}

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
		// java6u10 regression causes graphical xor to be very slow
		System.setProperty("sun.java2d.d3d","false"); //$NON-NLS-1$ //$NON-NLS-2$

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
		j.frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
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
			del.setTool(((ToolButton) e.getSource()).tool);
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

	public void windowClosing(WindowEvent e)
		{
		if (canvas.acts.isEmpty())
			{
			//TODO: this is annoying as hell, have an option to enable/disable it
			/*
			int c = JOptionPane.showConfirmDialog(frame,"Do you really want to quit?",
					"Really Quit, Really?",JOptionPane.YES_NO_OPTION);
			if (c == JOptionPane.YES_OPTION)
				{*/
			frame.dispose();
			//	}
			return;
			}
		int c = JOptionPane.showConfirmDialog(frame,"OMG DO U WANT TO SAVE?","Confirm",
				JOptionPane.YES_NO_CANCEL_OPTION);
		if (c == JOptionPane.CANCEL_OPTION)
			{
			return;
			}
		else if (c == JOptionPane.OK_OPTION)
			{
			doSave();
			frame.dispose();
			}
		else if (c == JOptionPane.NO_OPTION)
			{
			frame.dispose();
			}

		}

	private void doSave()
		{
		// TODO Auto-generated method stub

		}

	public void windowOpened(WindowEvent e)
		{
		}

	public void windowClosed(WindowEvent e)
		{
		}

	public void windowIconified(WindowEvent e)
		{
		}

	public void windowDeiconified(WindowEvent e)
		{
		}

	public void windowActivated(WindowEvent e)
		{
		}

	public void windowDeactivated(WindowEvent e)
		{
		}
	}
