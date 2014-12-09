/*
 * Copyright (C) 2012 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of Jeie.
 * Jeie is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

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
import org.jeie.Tool.FillTool;
import org.jeie.Tool.LineTool;
import org.jeie.Tool.PointTool;
import org.jeie.Tool.RectangleTool;
import org.jeie.Tool.OvalTool;
import org.jeie.Tool.ColorPickerTool;
import org.jeie.Tool.TextTool;
import org.jeie.Tool.GradientTool;
import org.jeie.resources.Resources;

public class ToolPanel extends JPanel implements ActionListener
	{
	private static final long serialVersionUID = 1L;
	protected ToolDelegate del;
	protected JPanel toolGrid;
	protected ButtonGroup bg = new ButtonGroup();
	protected JPanel toolOptions = new JPanel();

	AbstractButton defTool;

	public ToolPanel(ToolDelegate del)
		{
		super();
		this.del = del;
		BoxLayout layout = new BoxLayout(this,BoxLayout.PAGE_AXIS);
		this.setLayout(layout);

		toolGrid = new JPanel(new GridLayout(0,2));

		addButton(new ToolButton(Resources.getIcon("pencil"),"Pencil - draws freehand strokes",
				new PointTool()));
		defTool = addButton(new ToolButton(Resources.getIcon("line"),"Line - draws a straight line",
				new LineTool()));
		addButton(new ToolButton(Resources.getIcon("rect"),"Rect - draws a rectangle",
				new RectangleTool()));
		addButton(new ToolButton(Resources.getIcon("oval"),"Oval - draws an oval",
				new OvalTool()));
		addButton(new ToolButton(Resources.getIcon("color-fill"),"Fill - flood-fills a region",
				new FillTool()));
		addButton(new ToolButton(Resources.getIcon("color-picker"),"Color Picker - get the color at a point",
				new ColorPickerTool()));
		addButton(new ToolButton(Resources.getIcon("text"),"Text - Draw text",
				new TextTool()));
		addButton(new ToolButton(Resources.getIcon("gradient-linear"),"Gradient - draw gradients",
				new GradientTool()));

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
