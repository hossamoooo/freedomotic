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

package bticino.btcommlib.domain.lowlevel;

import bticino.btcommlib.domain.util.BTOpenMsgType;

/**
 * Specify Low Level BTicino OPEN Message
 */
public class BTOpenMessage {
  /** Type of message */
  private BTOpenMsgType m_msgType;
  /** Specific fields for open */
  private String m_who;
  private String m_what;
  private String m_where;
  private String m_dimension;
  private String[] m_dimValues;
  private String m_openRawMsg;

  public BTOpenMessage() {

  }

  public BTOpenMsgType getMsgType() {
    return m_msgType;
  }

  public void setMsgType(BTOpenMsgType m_msgType) {
    this.m_msgType = m_msgType;
  }

  public String getWho() {
    return m_who;
  }

  public void setWho(String m_who) {
    this.m_who = m_who;
  }

  public String getWhat() {
    return m_what;
  }

  public void setWhat(String m_what) {
    this.m_what = m_what;
  }

  public String getWhere() {
    return m_where;
  }

  public void setWhere(String m_where) {
    this.m_where = m_where;
  }

  public String getDimension() {
    return m_dimension;
  }

  public void setDimension(String m_dimension) {
    this.m_dimension = m_dimension;
  }

  public String[] getDimValues() {
    return m_dimValues;
  }

  public void setDimValues(String[] m_dimValues) {
    this.m_dimValues = m_dimValues;
  }

  public String getOpenRawMsg() {
    return m_openRawMsg;
  }

  public void setOpenRawMsg(String m_openRawMsg) {
    this.m_openRawMsg = m_openRawMsg;
  }

}
