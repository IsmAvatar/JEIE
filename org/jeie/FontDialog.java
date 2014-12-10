/**
* @file  FontDialog.java
* @brief Dialog for choosing fonts.
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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.TextAttribute;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jeie.resources.Resources;

public class FontDialog
	{
	private static JComboBox<String> fonts;
	private static JSpinner size;
	private static JToggleButton bold, italic, underlined;
	private static Hashtable<TextAttribute, Object> map;
	private static Font font;
	private static JTextArea preview;
	private static JButton okButton, cancelButton;
	private static JDialog d;

	private static ActionListener action = new ActionListener()
		{
		public void actionPerformed(ActionEvent e)
			{
			Object comp = e.getSource();
			if (comp == size || comp == fonts || comp == bold || comp == italic || comp == underlined)
				{
				map = new Hashtable<TextAttribute,Object>();
				map.put(TextAttribute.FAMILY, fonts.getSelectedItem());
				map.put(TextAttribute.SIZE, size.getValue());
				
				if (bold.isSelected())
					map.put(TextAttribute.WEIGHT,TextAttribute.WEIGHT_BOLD);
				else
					map.put(TextAttribute.WEIGHT,TextAttribute.WEIGHT_REGULAR);
				
				if (italic.isSelected())
					map.put(TextAttribute.POSTURE,TextAttribute.POSTURE_OBLIQUE);
				else
					map.put(TextAttribute.POSTURE,TextAttribute.POSTURE_REGULAR);
				
				if (underlined.isSelected())
					map.put(TextAttribute.UNDERLINE,TextAttribute.UNDERLINE_ON);
				else
					map.put(TextAttribute.UNDERLINE,-1);
				
				font = new Font(map);
				preview.setFont(font);
				return;
				}
			
			if (comp == okButton)
				{
				d.dispose();
				}
			
			if (comp == cancelButton)
				{
				font = null;
				d.dispose();
				}
			}
		};
	
	private static ChangeListener change = new ChangeListener()
		{
		public void stateChanged(ChangeEvent e)
			{
			action.actionPerformed(new ActionEvent(e.getSource(), ActionEvent.ACTION_FIRST, ""));
			}
		};
	
	public static Font getFont(Font initial)
		{
		if (initial == null)
			initial = new Font("Arial",Font.PLAIN,12);

		map = new Hashtable<TextAttribute,Object>();
		map.put(TextAttribute.FAMILY, initial.getFamily());
		map.put(TextAttribute.SIZE, initial.getSize());
		if (initial.isBold())
			map.put(TextAttribute.WEIGHT,TextAttribute.WEIGHT_BOLD);
		else
			map.put(TextAttribute.WEIGHT,TextAttribute.WEIGHT_REGULAR);
		
		if (initial.isItalic())
			map.put(TextAttribute.POSTURE,TextAttribute.POSTURE_OBLIQUE);
		else
			map.put(TextAttribute.POSTURE,TextAttribute.POSTURE_REGULAR);
		
		if (initial.getAttributes().get(TextAttribute.UNDERLINE) == null)
			map.put(TextAttribute.UNDERLINE,-1);
		else
			map.put(TextAttribute.UNDERLINE,initial.getAttributes().get(TextAttribute.UNDERLINE));
		
		font = new Font(map);
		
		String[] fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		
		d = new JDialog((JFrame) null,Resources.getString("FontDialog.TITLE"));
		d.setModal(true);
		d.setLayout(new BorderLayout());
		d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		d.setResizable(false);
		d.addWindowListener(new WindowAdapter()
			{
				public void windowClosing(WindowEvent e)
					{
					font = null;
					}
			});
		
		JPanel gridPanel = new JPanel();
		gridPanel.setLayout(new GridLayout(3,1));
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		topPanel.add(new JLabel(Resources.getString("FontDialog.FONT")));
		fonts = new JComboBox<String>(fontNames);
		fonts.setSelectedItem(font.getName());
		fonts.addActionListener(action);
		topPanel.add(fonts);

		topPanel.add(new JLabel(Resources.getString("FontDialog.SIZE")));
		size = new JSpinner(new SpinnerNumberModel(font.getSize(),1,2000,1));
		size.addChangeListener(change);
		topPanel.add(size);
		
		gridPanel.add(topPanel);
		
		JPanel stylePanel = new JPanel();
		stylePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		bold = new JToggleButton(Resources.getIconForKey("FontDialog.BOLD"));
		bold.addActionListener(action);
		stylePanel.add(bold);
		italic = new JToggleButton(Resources.getIconForKey("FontDialog.ITALIC"));
		italic.addActionListener(action);
		stylePanel.add(italic);
		underlined = new JToggleButton(Resources.getIconForKey("FontDialog.UNDERLINED"));
		underlined.addActionListener(action);
		stylePanel.add(underlined);
		
		gridPanel.add(stylePanel);
		gridPanel.add(new JLabel(Resources.getString("FontDialog.PREVIEW")));
		d.add(gridPanel, BorderLayout.NORTH);
		
		preview = new JTextArea();
		preview.setFont(font);
		preview.setText("AaBbCc0123456789");
		preview.setPreferredSize(new Dimension(160, 128));
		d.add(preview, BorderLayout.CENTER);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		bottomPanel.add(okButton = new JButton(Resources.getString("FontDialog.OK")));
		okButton.addActionListener(action);
		bottomPanel.add(cancelButton = new JButton(Resources.getString("FontDialog.CANCEL")));
		cancelButton.addActionListener(action);
		d.add(bottomPanel, BorderLayout.SOUTH);

		d.pack();
		Dimension res = Toolkit.getDefaultToolkit().getScreenSize();
		d.setLocation(res.width / 2 - d.getWidth() / 2,res.height / 2 - d.getHeight() / 2);
		d.setVisible(true);
		
		return font;
		}
	}
