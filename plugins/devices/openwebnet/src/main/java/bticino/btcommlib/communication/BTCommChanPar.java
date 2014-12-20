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
 * Contains BTicino channel information ( MONITOR PORT, COMMAND PORT, GATEWAY
 * IP)
 */
public class BTCommChanPar {
  private HashMap<String, String> m_connPar;
  private HashMap<String, String> m_mediaPar;

  /**
   * Empty constructor
   */
  public BTCommChanPar() {
    m_connPar = new HashMap<String, String>(0);
    m_mediaPar = new HashMap<String, String>(0);
  }

  /**
   * BTicino connection parameters setter
   * 
   * @param theName
   *          parameter name
   * @param theVal
   *          parameter value
   */
  public void setConnPar(String theName, String theVal) {
    m_connPar.put(theName, theVal);
  }

  /**
   * BTicino media parameters setter
   * 
   * @param theName
   *          parameter name
   * @param theVal
   *          parameter value
   */
  public void setMediaPar(String theName, String theVal) {
    m_mediaPar.put(theName, theVal);
  }

  /**
   * BTicino connection parameters getter
   * 
   * @param theName
   *          parameter name
   * @return the parameter value (string)</returns>
   */
  public String getConnPar(String theName) {
    if (m_connPar.containsKey(theName))
      return (m_connPar.get(theName));

    return ("");
  }

  /**
   * BTicino connection parameters getter
   * 
   * @param theName
   *          parameter name
   * @return the parameter value (string)</returns>
   */
  public String getMediaPar(String theName) {
    if (m_mediaPar.containsKey(theName))
      return (m_mediaPar.get(theName));

    return ("");
  }
}
