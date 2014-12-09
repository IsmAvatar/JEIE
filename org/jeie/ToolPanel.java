/**
* @file  ToolPanel.java
* @brief Image tool container.
*
* @section License
*
* Copyright (C) 2012 IsmAvatar <IsmAvatar@gmail.com>
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

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.jeie.Jeie.ToolDelegate;
import org.jeie.Tool.*;
import org.jeie.resources.Resources;

public class ToolPanel extends JPanel implements ActionListener
	{
	private static final long serialVersionUID = 1L;
	protected ToolDelegate del;
	protected JPanel toolGrid;
	protected ButtonGroup bg = new ButtonGroup();
	protected JPanel toolOptions = new JPanel();

	AbstractButton defTool;
	
	public ToolButton makeToolButton(String key, Tool t) {
		ToolButton tb = new ToolButton(Resources.getString("ToolPanel." + key), t);
		tb.setActionCommand(key);
		tb.addActionListener(this);
		//tb.setAccelerator(KeyStroke.getKeyStroke(Resources.getKeyboardString("TransformMenu." + key)));
		tb.setIcon(Resources.getIconForKey("ToolPanel." + key));
		return tb;
	}

	public ToolPanel(ToolDelegate del)
		{
		super();
		this.del = del;
		BoxLayout layout = new BoxLayout(this,BoxLayout.PAGE_AXIS);
		this.setLayout(layout);

		toolGrid = new JPanel(new GridLayout(0,2));

		addButton(makeToolButton("PENCIL",new PointTool()));
		addButton(makeToolButton("PAINTBRUSH",new PaintbrushTool()));
		//addButton(makeToolButton("ERASE",new EraseTool()));
		defTool = addButton(makeToolButton("LINE",new LineTool()));
		addButton(makeToolButton("OVAL",new OvalTool()));
		addButton(makeToolButton("RECT",new RectangleTool()));
		addButton(makeToolButton("ROUNDRECT",new RoundRectangleTool()));
		addButton(makeToolButton("COLOR_FILL",new FillTool()));
		addButton(makeToolButton("COLOR_PICKER",new ColorPickerTool()));
		addButton(makeToolButton("TEXT",new TextTool()));
		addButton(makeToolButton("GRADIENT_LINEAR",new GradientTool()));

		toolOptions.setBorder(BorderFactory.createLoweredBevelBorder());
		toolOptions.setMaximumSize(toolGrid.getPreferredSize());
		toolGrid.setMaximumSize(toolGrid.getPreferredSize());

		add(toolGrid);
		add(toolOptions);
		add(new JPanel());
		}

	public void selectDefault()
		{
		defTool.doClick();
		}

	public <K extends AbstractButton>K addButton(K b)
		{
		toolGrid.add(b);
		bg.add(b);
		b.addActionListener(this);
		return b;
		}

	public class ToolButton extends JToggleButton
		{
		private static final long serialVersionUID = 1L;

		public final Tool tool;
		
		public ToolButton(String tip, Tool t)
			{
			this(null,tip,t);
			}

		public ToolButton(Tool t)
			{
			this(null,null,t);
			}
		
		public ToolButton(ImageIcon ico, Tool t)
			{
			this(ico,null,t);
			}

		public ToolButton(ImageIcon ico, String tip, Tool t)
			{
			super(ico);
			tool = t;
			setToolTipText(tip);
			setPreferredSize(new Dimension(32,32));
			}
		}

	public void actionPerformed(ActionEvent e)
		{
		del.setTool(((ToolButton) e.getSource()).tool);
		return;
		}

	JComponent curOpt = null;

	public void showOptions(Tool tool)
		{
		if (curOpt != null) toolOptions.remove(curOpt);
		toolOptions.add(curOpt = tool.getOptionsComponent());
		toolOptions.updateUI();
		}
	}
