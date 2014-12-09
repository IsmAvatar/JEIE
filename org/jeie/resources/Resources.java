/**
* @file  Resources.java
* @brief Provides internationalization and localization support as well as other resource handling.
*
* @section License
*
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

package org.jeie.resources;

import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

import org.jeie.Jeie;

public final class Resources
	{

	private static final String LANGUAGE_BUNDLE_NAME = "org.jeie.resources.messages"; //$NON-NLS-1$
	private static final String INPUT_BUNDLE_NAME = "org.jeie.resources.keyboard"; //$NON-NLS-1$
	
	// NOTE: See comments about locale below.
	private static ResourceBundle LANGUAGE_BUNDLE = ResourceBundle.getBundle(LANGUAGE_BUNDLE_NAME);
	private static ResourceBundle KEYBOARD_BUNDLE = ResourceBundle.getBundle(INPUT_BUNDLE_NAME);
	
	public static String getString(String key)
		{
		try
			{
			return LANGUAGE_BUNDLE.getString(key);
			}
		catch (MissingResourceException e)
			{
			return '!' + key + '!';
			}
		}
	
	public static String getKeyboardString(String key)
		{
		try
			{
			return KEYBOARD_BUNDLE.getString(key);
			}
		catch (MissingResourceException e)
			{
			return '!' + key + '!';
			}
		}
	
	public static ImageIcon getIcon(String name)
		{
		String location = "org/jeie/icons/actions/" + name + ".png";
		URL url = Jeie.class.getClassLoader().getResource(location);
		if (url == null) return new ImageIcon(location);
		return new ImageIcon(url);
		}
	
	}
