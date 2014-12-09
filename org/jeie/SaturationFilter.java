/**
* @file  SaturationFilter.java
* @brief Filter for RGB color saturation.
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
