/**
* @file  OptionComponent.java
* @brief Collection of various reusable option panels.
*
* @section License
*
* Copyright (C) 2013 jimn346 <jds9496@gmail.com>
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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jeie.resources.Resources;

public class OptionComponent
	{
	/**
	 * An empty JPanel.
	 */
	public static JPanel emptyPanel = new JPanel();

	/**
	 * A list component which gives fill options.
	 */
	public static class FillOptions extends JList<ImageIcon>
		{
		private static final long serialVersionUID = 1L;

		public enum FillType
			{
			FILL,OUTLINE,BOTH
			}

		public FillType getFillType()
			{
			switch (getSelectedIndex())
				{
				case 0:
					return FillType.OUTLINE;
				case 1:
					return FillType.BOTH;
				case 2:
					return FillType.FILL;
				}
			System.err.println("Invalid fill type selection");
			return FillType.BOTH;
			}

		public FillOptions()
			{
			super(new ImageIcon[] { getBrushIcon("outline"),getBrushIcon("outline-fill"),
					getBrushIcon("fill") });
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			setSelectedIndex(0);
			}

		public void select(FillType fill)
			{
			switch (fill)
				{
				case OUTLINE:
					setSelectedIndex(0);
					return;
				case BOTH:
					setSelectedIndex(1);
					return;
				case FILL:
					setSelectedIndex(2);
					return;
				}
			}
		}

	/**
	 * A list component which gives gradient options.
	 */
	public static class GradientOptions extends JList<ImageIcon>
		{
		private static final long serialVersionUID = 1L;

		public enum GradientType
			{
			LINEAR, MIRRORED, RADIAL, CONICAL, SQUARE
			}

		public GradientType getFillType()
			{
			switch (getSelectedIndex())
				{
				case 0:
					return GradientType.LINEAR;
				case 1:
					return GradientType.MIRRORED;
				case 2:
					return GradientType.RADIAL;
				case 3:
					return GradientType.CONICAL;
				case 4:
					return GradientType.SQUARE;
				}
			System.err.println("Invalid gradient type selection");
			return GradientType.LINEAR;
			}

		public GradientOptions()
			{
			super(new ImageIcon[] { Resources.getIcon("gradient-linear"), getBrushIcon("gradient-mirrored"),
				getBrushIcon("gradient-radial"), getBrushIcon("gradient-conical"), getBrushIcon("gradient-square")	});
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			setSelectedIndex(0);
			}

		public void select(GradientType fill)
			{
			switch (fill)
				{
				case LINEAR:
					setSelectedIndex(0);
					return;
				case MIRRORED:
					setSelectedIndex(1);
					return;
				case RADIAL:
					setSelectedIndex(2);
					return;
				case CONICAL:
					setSelectedIndex(3);
					return;
				case SQUARE:
					setSelectedIndex(4);
					return;
				}
			}
		}

	/**
	 * A mixed component which gives text options.
	 */
	public static class TextOptions extends JPanel implements ActionListener
		{
		private static final long serialVersionUID = 1L;
		
		public enum Alignment
			{
			LEFT,CENTER,RIGHT,TOP,MIDDLE,BOTTOM
			}
		
		public Font font;
		public Alignment halign, valign;
		public JToggleButton left, center, right, top, middle, bottom;
		public JButton fButton;
		
		public TextOptions()
			{
			setLayout(new BorderLayout());
			font = new Font("Arial",Font.PLAIN,12);
			halign = Alignment.LEFT;
			valign = Alignment.TOP;
			
			add(fButton = new JButton("AaBbCc"), BorderLayout.CENTER);
			fButton.setFont(font);
			fButton.setToolTipText("Font");
			
			JPanel bottomPanel = new JPanel();
			bottomPanel.setLayout(new GridLayout(2, 3));
			bottomPanel.add(left = new JToggleButton(getBrushIcon("left"), true));
			bottomPanel.add(center = new JToggleButton(getBrushIcon("center"), true));
			bottomPanel.add(right = new JToggleButton(getBrushIcon("right"), true));
			bottomPanel.add(top = new JToggleButton(getBrushIcon("top"), true));
			bottomPanel.add(middle = new JToggleButton(getBrushIcon("middle"), true));
			bottomPanel.add(bottom = new JToggleButton(getBrushIcon("bottom"), true));
			
			ButtonGroup g1 = new ButtonGroup();
			g1.add(left);
			g1.add(center);
			g1.add(right);
			
			ButtonGroup g2 = new ButtonGroup();
			g2.add(top);
			g2.add(middle);
			g2.add(bottom);
			
			add(bottomPanel, BorderLayout.SOUTH);
			
			left.addActionListener(this);
			center.addActionListener(this);
			right.addActionListener(this);
			top.addActionListener(this);
			middle.addActionListener(this);
			bottom.addActionListener(this);
			fButton.addActionListener(this);
			
			left.setToolTipText("Left aligned");
			center.setToolTipText("Center aligned");
			right.setToolTipText("Right aligned");
			top.setToolTipText("Top aligned");
			middle.setToolTipText("Middle aligned");
			bottom.setToolTipText("Bottom aligned");
			
			setPreferredSize(new Dimension(64, 128));
			}

		public void actionPerformed(ActionEvent e)
			{
			if (e.getSource() == left)
				{
				halign = Alignment.LEFT;
				return;
				}
			if (e.getSource() == center)
				{
				halign = Alignment.CENTER;
				return;
				}
			if (e.getSource() == right)
				{
				halign = Alignment.RIGHT;
				return;
				}
			if (e.getSource() == top)
				{
				valign = Alignment.TOP;
				return;
				}
			if (e.getSource() == middle)
				{
				valign = Alignment.MIDDLE;
				return;
				}
			if (e.getSource() == bottom)
				{
				valign = Alignment.BOTTOM;
				return;
				}
			if (e.getSource() == fButton)
				{
				Font newFont = FontDialog.getFont(font);
				
				if (newFont == null)
					return;
				
				font = newFont;
				fButton.setFont(font);
				}
			}

		}

	/**
	 * A mixed component which displays a list of visual line sizes and a spinner selector.
	 */
	public static class SizeOptions extends JPanel implements ListSelectionListener,ChangeListener
		{
		private static final long serialVersionUID = 1L;

		JList<ImageIcon> visual;
		JSpinner spinner;

		public SizeOptions()
			{
			setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
			add(visual = new JList<ImageIcon>(new ImageIcon[] { getBrushIcon("line-1px"),getBrushIcon("line-2px"),
					getBrushIcon("line-3px"),getBrushIcon("line-4px"),getBrushIcon("line-5px") }));
			visual.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			add(spinner = new JSpinner(new SpinnerNumberModel(1,1,999,1)));
			spinner.setMaximumSize(new Dimension(48,19));
			spinner.addChangeListener(this);
			visual.addListSelectionListener(this);
			spinner.setValue(1);
			}

		boolean myChange = false;

		public void valueChanged(ListSelectionEvent e)
			{
			if (!myChange)
				{
				myChange = true;
				spinner.setValue(visual.getSelectedIndex() + 1);
				}
			myChange = false;
			}

		public void stateChanged(ChangeEvent arg0)
			{
			if (!myChange)
				{
				myChange = true;
				int ind = (Integer) spinner.getValue();
				if (ind > 5)
					visual.clearSelection();
				else
					visual.setSelectedIndex(ind - 1);
				}
			myChange = false;
			}

		public void addChangeListener(ChangeListener l)
			{
			spinner.addChangeListener(l);
			}

		public int getValue()
			{
			return (Integer) spinner.getValue();
			}
		}

	public static ImageIcon getBrushIcon(String name)
		{
		String location = "org/jeie/icons/brushes/" + name + ".png";
		URL url = Jeie.class.getClassLoader().getResource(location);
		if (url == null)
			{
			System.out.println("SHIT");
			return new ImageIcon(location);
			}
		return new ImageIcon(url);
		}
	}
