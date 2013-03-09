package org.jeie;

import java.awt.image.RGBImageFilter;

public class SaturationFilter extends RGBImageFilter
{
	float amt;
	
	public SaturationFilter(float a)
		{
		amt = a;
		}
	
	public SaturationFilter()
		{
		this(100);
		}
	
	//Algorithm from <http://alienryderflex.com/saturation.html>
	public int filterRGB(int x, int y, int rgb)
		{
		int a = (rgb >> 24) & 0xFF;
		int r = (rgb >> 16) & 0xFF;
		int g = (rgb >> 8) & 0xFF;
		int b = rgb & 0xFF;
		
		float p = (float) Math.sqrt(r * r * .299f + g * g * .587f + b * b * .114f);
		
		r = (int) Math.max(Math.min(p + (r - p) * amt, 255), 0);
		g = (int) Math.max(Math.min(p + (g - p) * amt, 255), 0);
		b = (int) Math.max(Math.min(p + (b - p) * amt, 255), 0);
		
		return (a << 24) | (r << 16) | (g << 8) | b; 
		}
		
}
