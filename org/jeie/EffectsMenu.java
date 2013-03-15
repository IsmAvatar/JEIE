/*
 * Copyright (C) 2009, 2012 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2009 Serge Humphrey <bob@bobtheblueberry.com>
 * Copyright (C) 2013 jimn346 <jds9496@gmail.com>
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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.FilteredImageSource;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class EffectsMenu extends JMenu implements ActionListener
	{
	private static final long serialVersionUID = 1L;
	public Jeie jeie;
	JMenuItem blur, value, invert, fade, colorize, saturation;

	public class Blur implements ImageAction
		{
		public int amount;

		public Blur(int amt)
			{
			amount = amt;
			}

		public void paint(Graphics g)
			{
			Canvas c = jeie.canvas;
			Graphics2D g2 = (Graphics2D) g;

			// create the blur kernel
			int numCoords = amount * amount;
			float blurFactor = 1.0f / numCoords;

			float[] blurKernel = new float[numCoords];
			for (int j = 0; j < numCoords; j++)
				blurKernel[j] = blurFactor;

			Kernel k = new Kernel(amount,amount,blurKernel);
			ConvolveOp blur = new ConvolveOp(k,ConvolveOp.EDGE_NO_OP,null);
			
			BufferedImage img = c.getRenderImage();
			
			//Clear the image.
			g2.setBackground(new Color(0, 0, 0, 0));
			g2.clearRect(0,0,img.getWidth(),img.getHeight());
			
			g2.drawImage(img,blur,0,0);
			}
		
		public boolean copiesRaster()
			{
			return true;
			}
		}

	public class Value implements ImageAction
		{
		public float amount;

		public Value(float amt)
			{
			amount = amt;
			}

		public void paint(Graphics g)
			{
			Canvas c = jeie.canvas;
			Graphics2D g2 = (Graphics2D) g;
			float[] scale = { amount,amount,amount,1.0f }; // keep alpha
			float[] offsets = { 0.0f,0.0f,0.0f,0.0f };
			RescaleOp value = new RescaleOp(scale,offsets,null);
			
			BufferedImage img = c.getRenderImage();
			
			//Clear the image.
			g2.setBackground(new Color(0, 0, 0, 0));
			g2.clearRect(0,0,img.getWidth(),img.getHeight());
			
			g2.drawImage(img,value,0,0);
			}
		
		public boolean copiesRaster()
			{
			return true;
			}
		}

	public class Invert implements ImageAction
		{
		public void paint(Graphics g)
			{
			Canvas c = jeie.canvas;
			Graphics2D g2d = (Graphics2D) g;
			float[] negFactors = { -1.0f,-1.0f,-1.0f,1.0f }; // keep alpha
			float[] offsets = { 255f,255f,255f,0.0f };
			RescaleOp invert = new RescaleOp(negFactors,offsets,null);
			
			BufferedImage img = c.getRenderImage();
			
			//Clear the image.
			g2d.setBackground(new Color(0, 0, 0, 0));
			g2d.clearRect(0,0,img.getWidth(),img.getHeight());
			
			g2d.drawImage(img,invert,0,0);
			}
		
		public boolean copiesRaster()
			{
			return true;
			}
		}

	public class Fade implements ImageAction
		{
		public Color fadeTo;
		public float amount;

		public Fade(Color to, float amt)
			{
			fadeTo = to;
			amount = amt;
			}

		public void paint(Graphics g)
			{
			Canvas c = jeie.canvas;
			Graphics2D g2d = (Graphics2D) g;

			Composite oldComp = g2d.getComposite();
			Color oldCol = g2d.getColor();

			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,amount));
			g2d.setColor(fadeTo);
			Dimension d = c.getImageSize();
			g2d.fillRect(0,0,d.width,d.height);
			g2d.setColor(oldCol);
			g2d.setComposite(oldComp);
			}
		
		public boolean copiesRaster()
			{
			return false;
			}
		}

	public class Saturation implements ImageAction
		{
		public int amount;

		public Saturation(int amt)
			{
			amount = amt;
			}

		public void paint(Graphics g)
			{
			Canvas c = jeie.canvas;

			SaturationFilter filter = new SaturationFilter(amount / 100f);
			FilteredImageSource filteredSrc = new FilteredImageSource(c.getRenderImage().getSource(), filter);
			
			Image image = Toolkit.getDefaultToolkit().createImage(filteredSrc);
			
			Graphics2D g2 = (Graphics2D) g;
			
			//Clear the image.
			g2.setBackground(new Color(0, 0, 0, 0));
			g2.clearRect(0,0,image.getWidth(null),image.getHeight(null));
			
			g2.drawImage(image, 0, 0, null);
			}
		
		public boolean copiesRaster()
			{
			return true;
			}
		}

	public void applyAction(ImageAction act)
		{
		Canvas c = jeie.canvas;
		c.acts.add(act);
		c.redoActs.clear();
		c.redrawCache();
		}

	public EffectsMenu(Jeie jeie)
		{
		super("Effects");
		this.jeie = jeie;

		//	TODO: Menu Icons

		blur = new JMenuItem("Blur", Jeie.getIcon("blur"));
		blur.addActionListener(this);
		add(blur);
		
		saturation = new JMenuItem("Saturation", Jeie.getIcon("saturation"));
		saturation.addActionListener(this);
		add(saturation);

		value = new JMenuItem("Value", Jeie.getIcon("value"));
		value.addActionListener(this);
		add(value);

		invert = new JMenuItem("Invert", Jeie.getIcon("invert"));
		invert.addActionListener(this);
		add(invert);

		fade = new JMenuItem("Fade to Black", Jeie.getIcon("fade"));
		fade.addActionListener(this);
		add(fade);

		addSeparator();

		colorize = new JMenuItem("Colorize", Jeie.getIcon("colorize"));
		colorize.setEnabled(false);
		add(colorize);
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == blur)
			{
			Integer integer = IntegerDialog.getInteger("Blur amount (1-9)",1,9,3,3);
			if (integer != null) applyAction(new Blur(integer));
			return;
			}
		if (e.getSource() == saturation)
			{
			Integer integer = IntegerDialog.getInteger("Saturation",0,200,100,50);
			if (integer != null) applyAction(new Saturation(integer));
			return;
			}
		if (e.getSource() == value)
			{
			Integer integer = IntegerDialog.getInteger("Value",-10,10,0,5);
			if (integer != null) applyAction(new Value((integer + 10) / 10.0f));
			return;
			}
		if (e.getSource() == invert)
			{
			applyAction(new Invert());
			return;
			}
		if (e.getSource() == fade)
			{
			Integer integer = IntegerDialog.getInteger("Fade amount (0-256)",0,256,128,64);
			if (integer != null) applyAction(new Fade(Color.BLACK,((float) integer) / 256.0f));
			return;
			}
		}
	}
