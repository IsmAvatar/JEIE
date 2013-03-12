package org.jeie;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class TransformMenu extends JMenu implements ActionListener
	{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Jeie jeie;
	JMenuItem rotate, flipH, flipV, zoom;

	public class Rotate extends ImageAction
		{
		public int angle;

		public Rotate(int ang)
			{
			angle = ang;
			copiesRaster = true;
			}

		public void paint(Graphics g)
			{
			Canvas c = jeie.canvas;
			BufferedImage img = c.getRenderImage();
			
			Graphics2D g2 = (Graphics2D) g;
			AffineTransform tr = new AffineTransform();
			tr.rotate(angle / 180D * Math.PI, img.getWidth() / 2f,img.getHeight() / 2f);
			AffineTransformOp op = new AffineTransformOp(tr, AffineTransformOp.TYPE_BILINEAR);
			g2.drawImage(c.getRenderImage(),op,0,0);
			}
		}
	
	public class Zoom extends ImageAction
		{
		public int amount;
	
		public Zoom(int amt)
			{
			amount = amt;
			copiesRaster = true;
			}
	
		public void paint(Graphics g)
			{
			Canvas c = jeie.canvas;
			BufferedImage img = c.getRenderImage();
			float z = amount / 100f;
			
			Graphics2D g2 = (Graphics2D) g;
			AffineTransform tr = new AffineTransform();
			tr.translate(img.getWidth() * (1 - z) / 2,img.getHeight() * (1 - z) / 2);
			tr.scale(z, z);
			AffineTransformOp op = new AffineTransformOp(tr, AffineTransformOp.TYPE_BILINEAR);
			g2.drawImage(c.getRenderImage(),op,0,0);
			}
		}
	
	public class FlipH extends ImageAction
		{
		public FlipH()
			{
			copiesRaster = true;
			}
		
		public void paint(Graphics g)
			{
			Canvas c = jeie.canvas;
			BufferedImage img = c.getRenderImage();
			
			Graphics2D g2 = (Graphics2D) g;
			AffineTransform tr = AffineTransform.getScaleInstance(-1, 1);
			tr.translate(-img.getWidth(), 0);
			AffineTransformOp op = new AffineTransformOp(tr, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			g2.drawImage(c.getRenderImage(),op,0,0);
			}
		}
	
	public class FlipV extends ImageAction
		{
		public FlipV()
			{
			copiesRaster = true;
			}
		
		public void paint(Graphics g)
			{
			Canvas c = jeie.canvas;
			BufferedImage img = c.getRenderImage();
			
			Graphics2D g2 = (Graphics2D) g;
			AffineTransform tr = AffineTransform.getScaleInstance(1, -1);
			tr.translate(0, -img.getHeight());
			AffineTransformOp op = new AffineTransformOp(tr, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			g2.drawImage(c.getRenderImage(),op,0,0);
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
		super("Transform");
		this.jeie = jeie;

		//	TODO: Menu Icons
		
		flipH = new JMenuItem("Flip Horizontally", Jeie.getIcon("flip-h"));
		flipH.addActionListener(this);
		add(flipH);
		
		flipV = new JMenuItem("Flip Vertically", Jeie.getIcon("flip-v"));
		flipV.addActionListener(this);
		add(flipV);

		rotate = new JMenuItem("Rotate", Jeie.getIcon("rotate"));
		rotate.addActionListener(this);
		add(rotate);
		
		zoom = new JMenuItem("Zoom", Jeie.getIcon("zoom-in"));
		zoom.addActionListener(this);
		add(zoom);
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == rotate)
			{
			Integer integer = IntegerDialog.getInteger("Rotation",0,360,0,60);
			if (integer != null) applyAction(new Rotate(integer));
			return;
			}
		if (e.getSource() == zoom)
			{
			Integer integer = IntegerDialog.getInteger("Zoom (%)",0,400,100,100);
			if (integer != null) applyAction(new Zoom(integer));
			return;
			}
		if (e.getSource() == flipH)
			{
			applyAction(new FlipH());
			return;
			}
		if (e.getSource() == flipV)
			{
			applyAction(new FlipV());
			return;
			}
		}

	}
