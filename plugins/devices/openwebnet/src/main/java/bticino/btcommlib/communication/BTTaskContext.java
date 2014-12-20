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

public class BTTaskContext {
  public final static String BT_IP_NOT_IN_RANGE_ERROR = "BT_CONN_ERR";

  private String m_ctx_name;
  private String m_in_value;
  private boolean m_in_exactMD;
  private String m_out_value;
  private boolean m_out_result;
  private String m_out_errInd;

  public BTTaskContext() {
    m_ctx_name = null;
    m_in_value = null;
    m_in_exactMD = false;
    m_out_value = null;
    m_out_result = false;
    m_out_errInd = null;
  }

  public String getCtx_name() {
    return m_ctx_name;
  }

  public void setCtx_name(String m_ctx_name) {
    this.m_ctx_name = m_ctx_name;
  }

  public String getIn_value() {
    return m_in_value;
  }

  public void setIn_value(String m_in_value) {
    this.m_in_value = m_in_value;
  }

  public boolean isIn_exactMD() {
    return m_in_exactMD;
  }

  public void setIn_exactMD(boolean m_in_exactMD) {
    this.m_in_exactMD = m_in_exactMD;
  }

  public String getOut_value() {
    return m_out_value;
  }

  public void setOut_value(String m_out_value) {
    this.m_out_value = m_out_value;
  }

  public boolean isOut_result() {
    return m_out_result;
  }

  public void setOut_result(boolean m_out_result) {
    this.m_out_result = m_out_result;
  }

  public String getOut_errInd() {
    return m_out_errInd;
  }

  public void setOut_errInd(String m_out_errInd) {
    this.m_out_errInd = m_out_errInd;
  }
}
