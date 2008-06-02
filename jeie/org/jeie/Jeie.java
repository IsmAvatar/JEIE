/*
 * Copyright (C) 2008 IsmAvatar <cmagicj@nni.com>
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
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

public class Jeie implements ActionListener
	{
	BufferedImage oldimage;
	public JFrame f;
	public Canvas canvas;
	public JButton zoomIn, zoomOut;

	public Jeie(BufferedImage image)
		{
		oldimage = image;
		f = new JFrame("Easy Image Editor");
		JPanel p = new JPanel(new BorderLayout());
		f.setContentPane(p);
		p.add(makeToolBar(),BorderLayout.WEST);
		canvas = new Canvas(image);
		JScrollPane sp = new JScrollPane(canvas);
		sp.getVerticalScrollBar().setUnitIncrement(10);
		sp.getHorizontalScrollBar().setUnitIncrement(10);
		p.add(sp,BorderLayout.CENTER);
		f.pack();
		f.setVisible(true);
		}

	public JToolBar makeToolBar()
		{
		JToolBar tool = new JToolBar(JToolBar.VERTICAL);

		zoomOut = addButton(tool,"-");
		zoomIn = addButton(tool,"+");

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
		Jeie j = new Jeie(null);
		j.f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}

	@Override
	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == zoomIn)
			{
			if (canvas.zoom < 32)
				{
				canvas.zoom *= 2;
				canvas.updateUI();
				}
			}
		if (e.getSource() == zoomOut)
			{
			if (canvas.zoom > 1)
				{
				canvas.zoom /= 2;
				canvas.updateUI();
				}
			}
		}
	}
