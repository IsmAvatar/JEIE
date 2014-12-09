package org.jeie;

import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;

public final class Util
	{

	public static String rectangleToString(Rectangle r)
		{
		return String.format("%d %d %d %d",r.x,r.y,r.width,r.height);
		}
	
	public static Rectangle stringToRectangle(String s, Rectangle defaultValue)
		{
		if (s == null) return defaultValue;
		String[] sa = s.split(" +");
		if (sa.length != 4) return defaultValue;
		int[] ia = new int[4];
		for (int i = 0; i < 4; i++)
			try
				{
				ia[i] = Integer.parseInt(sa[i]);
				}
			catch (NumberFormatException e)
				{
				return defaultValue;
				}
		return new Rectangle(ia[0],ia[1],ia[2],ia[3]);
		}

	static public Shape getOutsideEdge(Graphics gc, Rectangle bb, int top, int lft, int btm, int rgt) {
  int                                 ot=bb.y            , it=(ot+top);
  int                                 ol=bb.x            , il=(ol+lft);
  int                                 ob=(bb.y+bb.height), ib=(ob-btm);
  int                                 or=(bb.x+bb.width ), ir=(or-rgt);

  return new Polygon(
   new int[]{ ol, ol, or, or, ol, ol,   il, ir, ir, il, il },
   new int[]{ it, ot, ot, ob, ob, it,   it, it, ib, ib, it },
   11
   );
  }
	
	}
