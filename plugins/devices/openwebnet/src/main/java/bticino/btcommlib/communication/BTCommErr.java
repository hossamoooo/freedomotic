/* 
 * BtCommLib - the OpenWebNet Java Library
 *
 * Copyright (C) 2011 BTicino S.p.A.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package bticino.btcommlib.communication;

import java.util.HashMap;

/**
 * Communication library error object
 */
public class BTCommErr {
	private long m_errCode;
	private String m_errMsg;
	private String m_errArgs;
	private m_errorType m_errEnum;

	/**
	 * Default constructor
	 */
	public BTCommErr() {
		m_errCode = 0;
		m_errMsg = "";
		m_errEnum = m_errorType.e_NoError;
		m_errArgs = "";
	}

	/**
	 * Enumerate library communication errors
	 */
	public enum m_errorType {
		e_NoError, e_NotConnected, e_AlreadyConnected, e_NotInRange, e_UnableBuilOpenFrame, e_UnableSendFrame, e_MissedOpenFrame, e_ConnNotAvailable, e_ReadOnlySession
	}

	/**
	 * Translation dictionary from m_errorType to high level language
	 */
	private final static HashMap<m_errorType, String> m_errorList = new HashMap<m_errorType, String>();
	static {
		m_errorList.put(m_errorType.e_NoError, "No Error");
		m_errorList.put(m_errorType.e_NotConnected,
				"Unable to connect the gateway");
		m_errorList.put(m_errorType.e_NotInRange,
				"Unable to connect the gateway, IP Not in range");
		m_errorList.put(m_errorType.e_AlreadyConnected, "Is already connected");
		m_errorList.put(m_errorType.e_ConnNotAvailable,
				"Connection not available");
		m_errorList.put(m_errorType.e_UnableBuilOpenFrame,
				"Unable to build an Open Frame");
		m_errorList.put(m_errorType.e_UnableSendFrame, "Frame not send");
		m_errorList.put(m_errorType.e_MissedOpenFrame, "Missed open frame");
		m_errorList.put(m_errorType.e_ReadOnlySession, "Read only session");
	}

	/**
	 * Constructor with error type as parameter
	 * 
	 * @param theErrCode
	 *            Error Type, select from List of e_errorType
	 */
	public BTCommErr(m_errorType theErrCode) {
		m_errCode = theErrCode.ordinal();
		m_errMsg = m_errorList.get(theErrCode);
		m_errEnum = theErrCode;
		m_errArgs = "";
	}

	/**
	 * Constructor with error type and error message as parameters
	 * 
	 * @param theErrCode
	 *            Error Type, select from List of e_errorType
	 * @param errorArgs
	 *            Error message, more information on the error
	 */
	public BTCommErr(m_errorType theErrCode, String errorArgs) {
		m_errCode = theErrCode.ordinal();
		m_errMsg = m_errorList.get(theErrCode);
		m_errEnum = theErrCode;
		m_errArgs = errorArgs;
	}

	/**
	 * Check if object represent an error
	 * 
	 * @return true for error
	 */
	public boolean isErr() {
		if (m_errCode == 0)
			return false;
		else
			return true;
	}

	/**
	 * Retrieve the error string
	 * 
	 * @return the error message string
	 */
	public String getErrMsg() {
		return m_errMsg;
	}

	/**
	 * Retrieve the error arguments
	 * 
	 * @return error arguments as String
	 */
	public String getErrArgs() {
		return m_errArgs;
	}

	/**
	 * Retrieve the error type
	 * 
	 * @return the error type
	 */
	public m_errorType getErrEnum() {
		return m_errEnum;
	}

}
