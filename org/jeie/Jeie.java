/**
* @file  Jeie.java
* @brief Main class
*
* @section License
*
* Copyright (C) 2008, 2009, 2012 IsmAvatar <IsmAvatar@gmail.com>
* Copyright (C) 2009, 2012 Serge Humphrey <bobtheblueberry@gmail.com>
* Copyright (C) 2013 jimn346 <jds9496@gmail.com>
* Copyright (C) 2014 Robert B. Colton
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import org.jeie.Canvas.RenderMode;
import org.jeie.resources.Resources;

public class Jeie implements ActionListener
	{
	private JFrame frame;
	private JToggleButton bGrid;
	private JScrollPane scroll;

	public File file;
	public Canvas canvas;
	public Palette pal;
	private JMenuBar menuBar;
	private JToolBar toolBar;
	private ToolPanel toolPanel;
	public final String TITLE = "Easy Image Editor ";

	public Jeie(BufferedImage image)
		{
		if (image == null) image = createWhiteBufferedImage(32,32);
		pal = new Palette();
		canvas = new Canvas(image);
		scroll = new JScrollPane(canvas);
		toolPanel = new ToolPanel(new ToolDelegate(canvas));
		toolPanel.selectDefault();

		JPanel p = new JPanel(new BorderLayout());
		p.add(makeToolBar(),BorderLayout.NORTH);
		p.add(toolPanel,BorderLayout.WEST);
		scroll.getVerticalScrollBar().setUnitIncrement(10);
		scroll.getHorizontalScrollBar().setUnitIncrement(10);
		p.add(scroll,BorderLayout.CENTER);
		p.add(pal,BorderLayout.SOUTH);

		frame = new JFrame();
		frame.setJMenuBar(makeMenuBar());
		frame.setContentPane(p);
		frame.setMinimumSize(new Dimension(500,500));
		updateTitle();
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.addWindowListener(new WindowAdapter()
			{
				public void windowClosing(WindowEvent e)
					{
					doClose();
					}
			});
		}

	public void updateTitle()
		{
		if (file == null)
			frame.setTitle(TITLE + "<untitled>");
		else
			frame.setTitle(TITLE + file.getName());
		}

	public JMenuBar makeMenuBar()
		{
		menuBar = new JMenuBar();
		JMenu fm = new JMenu(Resources.getString("Jeie.FILE"));
		menuBar.add(fm);
		addMenuItem(fm,"NEW");
		addMenuItem(fm,"OPEN");
		fm.addSeparator();
		addMenuItem(fm,"SAVE");
		addMenuItem(fm,"SAVE_AS");
		fm.addSeparator();
		addMenuItem(fm,"EXIT");
		
		JMenu em = new JMenu(Resources.getString("Jeie.EDIT"));
		menuBar.add(em);
		addMenuItem(em,"UNDO");
		addMenuItem(em,"REDO");

		JMenu vm = new JMenu(Resources.getString("Jeie.VIEW"));
		menuBar.add(vm);
		addMenuItem(vm,"ZOOM_IN");
		addMenuItem(vm,"ZOOM_OUT");
		vm.addSeparator();
		addMenuItem(vm,"TILED");
		addMenuItem(vm,"GRID");

		menuBar.add(new TransformMenu(this));
		menuBar.add(new EffectsMenu(this));
		return menuBar;
		}

	public JMenuItem addMenuItem(JMenu menu, String key)
		{
		JMenuItem mi = new JMenuItem(Resources.getString("Jeie." + key),Resources.getIconForKey("Jeie." + key));
		mi.setAccelerator(KeyStroke.getKeyStroke(Resources.getKeyboardString("Jeie." + key)));
		mi.setActionCommand(key);
		mi.addActionListener(this);
		menu.add(mi);
		return mi;
		}

	public JToolBar makeToolBar()
		{
		toolBar = new JToolBar();
		toolBar.setFloatable(false);

		addButton(toolBar,new JButton(),"NEW");
		addButton(toolBar,new JButton(),"OPEN");
		addButton(toolBar,new JButton(),"SAVE");
		
		toolBar.addSeparator();
		
		addButton(toolBar,new JButton(),"UNDO");
		addButton(toolBar,new JButton(),"REDO");
		
		toolBar.addSeparator();
		
		addButton(toolBar,new JButton(),"ZOOM_OUT");
		addButton(toolBar,new JButton(),"ZOOM_IN");
		
		toolBar.addSeparator();
		
		bGrid = addButton(toolBar,new JToggleButton(),"GRID");

		return toolBar;
		}

	public <K extends AbstractButton>K addButton(Container c, K b, String key)
		{
		c.add(b);
		b.setActionCommand(key);
		b.addActionListener(this);
		b.setIcon(Resources.getIconForKey("Jeie." + key));
		b.setToolTipText(Resources.getString("Jeie." + key));
		return b;
		}


	protected class ToolDelegate extends MouseAdapter
		{
		protected Tool tool;
		protected Canvas canvas;

		public ToolDelegate(Canvas canvas)
			{
			this.canvas = canvas;
			canvas.addMouseListener(this);
			canvas.addMouseMotionListener(this);
			canvas.addMouseWheelListener(this);
			}

		public void setTool(Tool t)
			{
			if (tool != null) tool.finish(canvas,pal);
			tool = t;
			if (toolPanel != null) toolPanel.showOptions(tool);
			}

		protected MouseEvent refactor(MouseEvent e)
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
		UIManager.put("swing.boldMetal",false); //$NON-NLS-1$

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
		String act = e.getActionCommand();
		if (act.equals("ZOOM_IN"))
			{
			canvas.zoomIn();
			return;
			}
		if (act.equals("ZOOM_OUT"))
			{
			canvas.zoomOut();
			return;
			}
		if (act.equals("UNDO"))
			{
			if (!canvas.acts.isEmpty())
				{
				canvas.redoActs.addFirst(canvas.acts.removeLast());
				canvas.redrawCache();
				}
			return;
			}
		if (act.equals("REDO"))
			{
			if (!canvas.redoActs.isEmpty())
				{
				canvas.acts.add(canvas.redoActs.removeFirst());
				canvas.redrawCache();
				}
			return;
			}
		if (act.equals("NEW"))
			{
			doNew();
			return;
			}
		if (act.equals("OPEN"))
			{
			doOpen();
			return;
			}
		if (act.equals("SAVE"))
			{
			doSave(false);
			return;
			}
		if (act.equals("SAVE_AS"))
			{
			doSave(true);
			return;
			}
		if (act.equals("EXIT"))
			{
			doClose();
			return;
			}
		if (act.equals("TILED"))
			{
			canvas.renderMode = (canvas.renderMode != RenderMode.TILED) ? RenderMode.TILED
					: RenderMode.NORMAL;
			canvas.repaint();
			}
		if (act.equals("GRID"))
			{
			canvas.isGridDrawn = bGrid.isSelected();
			canvas.repaint();
			return;
			}
		}

	public boolean hasChanged()
		{
		return !canvas.acts.isEmpty();
		}

	public boolean doNew()
		{
		if (!checkSave()) return false;
		//TODO: Ask for sizes
		file = null;
		BufferedImage img = createWhiteBufferedImage(120,120);
		canvas.setImage(img);
		scroll.updateUI();
		updateTitle();
		return true;
		}

	public void doClose()
		{
		if (!hasChanged()) System.exit(0);
		int c = JOptionPane.showConfirmDialog(frame,
				Resources.getString("Jeie.UNSAVED_MESSAGE"),
				Resources.getString("Jeie.UNSAVED_TITLE"),
				JOptionPane.YES_NO_CANCEL_OPTION);
		if (c == JOptionPane.CANCEL_OPTION) return;
		if (c == JOptionPane.YES_OPTION) if (doSave(false)) { System.exit(0); }
		if (c == JOptionPane.NO_OPTION) System.exit(0);
		}

	/**
	 * @return false if the action was canceled
	 */
	public boolean checkSave()
		{
		if (hasChanged())
			{
			int c = JOptionPane.showConfirmDialog(frame,
					Resources.getString("Jeie.UNSAVED_MESSAGE"),
					Resources.getString("Jeie.UNSAVED_TITLE"),
					JOptionPane.YES_NO_CANCEL_OPTION);
			if (c == JOptionPane.CANCEL_OPTION) return false;
			if (c == JOptionPane.YES_OPTION) return doSave(false);
			if (c == JOptionPane.NO_OPTION) return true;
			}
		return true;
		}

	public boolean doOpen()
		{
		if (!checkSave()) return false;
		File f = getFile(false);
		if (f == null) return false;
		try
			{
			BufferedImage img = ImageIO.read(f);
			canvas.setImage(img);
			file = f;
			scroll.updateUI();
			updateTitle();
			return true;
			}
		catch (IOException e)
			{
			JOptionPane.showMessageDialog(frame,
					Resources.getString("Jeie.OPEN_FAIL_MESSAGE") + " \"" + f.getPath() + "\"",
					Resources.getString("Jeie.OPEN_FAIL_TITLE"),
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			}
		return false;
		}

	public boolean doSave(boolean saveAs)
		{
		File f = file;
		if (saveAs || file == null)
			{
			f = getFile(true);
			if (f == null) return false;
			// just use PNG..
			String name = f.getName().toLowerCase();
			if (!name.endsWith(".png"))
				{
				if (name.contains(".")) name = name.substring(0,name.lastIndexOf('.'));
				f = new File(f.getParentFile(),name + ".png");
				}
			}
		try
			{
			ImageIO.write(canvas.getRenderImage(),"PNG",f);
			file = f;
			return true;
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}

		return false;
		}

	private File getFile(final boolean save)
		{
		final JFileChooser fc = new JFileChooser((file != null) ? file.getParent() : null);
		fc.setFileFilter(new FileFilter()
			{
				@Override
				public String getDescription()
					{
					return "Image Files";
					}

				@Override
				public boolean accept(File f)
					{
					if (f.isDirectory()) return true;

					String[] filters;
					if (save)
						filters = ImageIO.getWriterFileSuffixes();
					else
						filters = ImageIO.getReaderFileSuffixes();
					String name = f.getName().toLowerCase();
					for (String s : filters)
						if (name.endsWith(s.toLowerCase())) return true;
					return false;
					}
			});
		int result;
		if (save)
			result = fc.showSaveDialog(frame);
		else
			result = fc.showOpenDialog(frame);
		if (result != JFileChooser.APPROVE_OPTION) return null;
		File f = fc.getSelectedFile();
		if (f == null || !save || !f.exists()) return f;
		int o = JOptionPane.showConfirmDialog(fc,"File " + f.getName() + " already exists. Replace?");
		if (o == JOptionPane.YES_OPTION) return f;
		if (o == JOptionPane.NO_OPTION) return getFile(save);
		return null;
		}
	}
