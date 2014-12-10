/**
* @file  IntegerDialog.java
* @brief Dialog for choosing an integer.
*
* @section License
*
* Copyright (C) 2009 Serge Humphrey <bob@bobtheblueberry.com>
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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jeie.resources.Resources;

public class IntegerDialog
	{
	public static Integer getInteger(String text, int min, int max, int def, int tickSpacing)
		{
		final Integer[] values = new Integer[1];
		values[0] = def;

		final JDialog d = new JDialog((JFrame) null,Resources.getString("IntegerDialog.INPUT"));
		d.setModal(true);
		d.setLayout(new BorderLayout(12,12));
		d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		d.setResizable(false);
		d.addWindowListener(new WindowAdapter()
			{
				public void windowClosing(WindowEvent e)
					{
					values[0] = null;
					}
			});
		JLabel l = new JLabel(text);
		d.add(l,BorderLayout.NORTH);

		final JSlider slider;
		final JSpinner spinner;
		slider = new JSlider(min,max,def);
		spinner = new JSpinner(new SpinnerNumberModel(def,min,max,1));

		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.setPaintTrack(true);
		slider.setMinorTickSpacing(1);
		slider.setMajorTickSpacing(tickSpacing);
		slider.setSnapToTicks(true);

		slider.addChangeListener(new ChangeListener()
			{
				public void stateChanged(ChangeEvent e)
					{
					int val = slider.getValue();
					spinner.setValue(val);
					values[0] = val;
					}
			});

		spinner.addChangeListener(new ChangeListener()
			{
				public void stateChanged(ChangeEvent e)
					{
					int val = (Integer) spinner.getValue();
					slider.setValue(val);
					values[0] = val;
					}
			});

		Box box1 = Box.createHorizontalBox();
		box1.add(slider);
		box1.add(Box.createHorizontalStrut(3));
		box1.add(spinner);
		d.add(box1,BorderLayout.CENTER);

		Box box2 = Box.createHorizontalBox();
		box2.add(Box.createHorizontalGlue());
		JButton ok = new JButton(Resources.getString("IntegerDialog.OK"));
		ok.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
					{
					d.dispose();
					}
			});
		box2.add(ok);
		box2.add(Box.createHorizontalStrut(6));
		JButton cancel = new JButton(Resources.getString("IntegerDialog.CANCEL"));
		cancel.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
					{
					values[0] = null;
					d.dispose();
					}
			});
		box2.add(cancel);
		d.add(box2,BorderLayout.SOUTH);

		d.pack();
		Dimension res = Toolkit.getDefaultToolkit().getScreenSize();
		d.setLocation(res.width / 2 - d.getWidth() / 2,res.height / 2 - d.getHeight() / 2);
		d.setVisible(true);

		return values[0];
		}
	}
