/**
* @file  TransformMenu.java
* @brief Collection of transformation menu options.
*
* @section License
*
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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.jeie.resources.Resources;

public class TransformMenu extends JMenu implements ActionListener
	{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Jeie jeie;

	public class Rotate implements ImageAction
		{
		public int angle;

		public Rotate(int ang)
			{
			angle = ang;
			}

		public void paint(Graphics g)
			{
			Canvas c = jeie.canvas;
			BufferedImage img = c.getRenderImage();
			
			Graphics2D g2 = (Graphics2D) g;
			
			//Clear the image.
			g2.setBackground(new Color(0, 0, 0, 0));
			g2.clearRect(0,0,img.getWidth(),img.getHeight());
			
			AffineTransform tr = new AffineTransform();
			tr.rotate(angle / 180D * Math.PI, img.getWidth() / 2f,img.getHeight() / 2f);
			AffineTransformOp op = new AffineTransformOp(tr, AffineTransformOp.TYPE_BILINEAR);
			g2.drawImage(img,op,0,0);
			}
		
		public boolean copiesRaster()
			{
			return true;
			}
		}
	
	public class Zoom implements ImageAction
		{
		public int amount;
	
		public Zoom(int amt)
			{
			amount = amt;
			}
	
		public void paint(Graphics g)
			{
			Canvas c = jeie.canvas;
			BufferedImage img = c.getRenderImage();
			float z = amount / 100f;
			
			Graphics2D g2 = (Graphics2D) g;
			
			//Clear the image.
			g2.setBackground(new Color(0, 0, 0, 0));
			g2.clearRect(0,0,img.getWidth(),img.getHeight());
			
			AffineTransform tr = new AffineTransform();
			tr.translate(img.getWidth() * (1 - z) / 2,img.getHeight() * (1 - z) / 2);
			tr.scale(z, z);
			AffineTransformOp op = new AffineTransformOp(tr, AffineTransformOp.TYPE_BILINEAR);
			g2.drawImage(img,op,0,0);
			}
		
		public boolean copiesRaster()
			{
			return true;
			}
		}
	
	public class FlipH implements ImageAction
		{
		public void paint(Graphics g)
			{
			Canvas c = jeie.canvas;
			BufferedImage img = c.getRenderImage();
			
			Graphics2D g2 = (Graphics2D) g;
			
			//Clear the image.
			g2.setBackground(new Color(0, 0, 0, 0));
			g2.clearRect(0,0,img.getWidth(),img.getHeight());
			
			AffineTransform tr = AffineTransform.getScaleInstance(-1, 1);
			tr.translate(-img.getWidth(), 0);
			AffineTransformOp op = new AffineTransformOp(tr, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			g2.drawImage(img,op,0,0);
			}
		
		public boolean copiesRaster()
			{
			return true;
			}
		}
	
	public class FlipV implements ImageAction
		{
		public void paint(Graphics g)
			{
			Canvas c = jeie.canvas;
			BufferedImage img = c.getRenderImage();
			
			Graphics2D g2 = (Graphics2D) g;
			
			//Clear the image.
			g2.setBackground(new Color(0, 0, 0, 0));
			g2.clearRect(0,0,img.getWidth(),img.getHeight());
			
			AffineTransform tr = AffineTransform.getScaleInstance(1, -1);
			tr.translate(0, -img.getHeight());
			AffineTransformOp op = new AffineTransformOp(tr, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			g2.drawImage(img,op,0,0);
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

	public TransformMenu(Jeie jeie)
		{
		super(Resources.getString("TransformMenu.TRANSFORM"));
		this.jeie = jeie;

		//	TODO: Menu Icons
		add(makeMenuItem("FLIP_HORIZONTALLY"));
		add(makeMenuItem("FLIP_VERTICALLY"));

		add(makeMenuItem("ROTATE"));
		add(makeMenuItem("ZOOM"));
		}
	
	public JMenuItem makeMenuItem(String key) {
		JMenuItem mi = new JMenuItem(Resources.getString("TransformMenu." + key));
		mi.setActionCommand(key);
		mi.addActionListener(this);
		mi.setAccelerator(KeyStroke.getKeyStroke(Resources.getKeyboardString("TransformMenu." + key)));
		mi.setIcon(Resources.getIconForKey("TransformMenu." + key));
		return mi;
	}

	public void actionPerformed(ActionEvent e)
		{
		String act = e.getActionCommand();
		if (act.equals("ROTATE"))
			{
			Integer integer = IntegerDialog.getInteger(Resources.getString("TransformMenu.ROTATION"),0,360,0,60);
			if (integer != null) applyAction(new Rotate(integer));
			return;
			}
		if (act.equalsIgnoreCase("ZOOM"))
			{
			Integer integer = IntegerDialog.getInteger(Resources.getString("TransformMenu.ZOOM_PERCENT"),0,400,100,100);
			if (integer != null) applyAction(new Zoom(integer));
			return;
			}
		if (act.equals("FLIP_HORIZONTALLY"))
			{
			applyAction(new FlipH());
			return;
			}
		if (act.equals("FLIP_VERTICALLY"))
			{
			applyAction(new FlipV());
			return;
			}
		}

	}
