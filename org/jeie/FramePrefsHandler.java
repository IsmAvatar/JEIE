/*
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2014 Robert B. Colton
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.jeie;

import java.awt.GraphicsEnvironment;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.util.prefs.Preferences;

import javax.swing.JFrame;

public class FramePrefsHandler implements ComponentListener,WindowStateListener
	{
	private final JFrame frame;
	private static final Preferences PREFS = Preferences.userRoot().node("/org/jeie");

	public FramePrefsHandler(JFrame frame)
		{
		this.frame = frame;
		frame.pack(); // makes the frame displayable, so that maximizing works
		frame.setMinimumSize(frame.getSize());
		frame.setMaximizedBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
		frame.setBounds(Util.stringToRectangle(PREFS.get("WINDOW_BOUNDS",null),frame.getBounds()));
		int state = frame.getExtendedState()
				| (PREFS.getBoolean("WINDOW_MAXIMIZED",true) ? JFrame.MAXIMIZED_BOTH : 0);
		frame.setExtendedState(state);
		frame.addComponentListener(this);
		frame.addWindowStateListener(this);
		}

	private boolean isMaximized()
		{
		return (frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH;
		}

	public void componentMoved(ComponentEvent e)
		{
		if (!isMaximized()) PREFS.put("WINDOW_BOUNDS",Util.rectangleToString(frame.getBounds()));
		}

	public void componentResized(ComponentEvent e)
		{
		if (!isMaximized()) PREFS.put("WINDOW_BOUNDS",Util.rectangleToString(frame.getBounds()));
		}

	public void windowStateChanged(WindowEvent e)
		{
			PREFS.putBoolean("WINDOW_MAXIMIZED",isMaximized());
		}

	public void componentHidden(ComponentEvent e)
		{
		//Unused
		}

	public void componentShown(ComponentEvent e)
		{
		//Unused
		}
	}
