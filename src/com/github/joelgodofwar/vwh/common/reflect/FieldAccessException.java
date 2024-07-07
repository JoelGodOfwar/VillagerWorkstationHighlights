/*
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2012 Kristian S. Stangeland
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program;
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307 USA
 */
package com.github.joelgodofwar.vwh.common.reflect;

/**
 * Invoked when a field is inaccessible due to security limitations, or when it simply doesn't exist.
 *
 * @author Kristian
 */
public class FieldAccessException extends RuntimeException {

	/**
	 * Generated by Eclipse.
	 */
	private static final long serialVersionUID = 1911011681494034617L;

	public FieldAccessException() {
		super();
	}

	public FieldAccessException(String message, Throwable cause) {
		super(message, cause);
	}

	public FieldAccessException(String message) {
		super(message);
	}

	public FieldAccessException(Throwable cause) {
		super(cause);
	}

	public static FieldAccessException fromFormat(String message, Object... params) {
		return new FieldAccessException(String.format(message, params));
	}

	@Override
	public String toString() {
		String message = getMessage();
		return "FieldAccessException" + (message != null ? ": " + message : "");
	}
}