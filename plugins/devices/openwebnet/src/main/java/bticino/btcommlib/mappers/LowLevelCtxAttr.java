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

package bticino.btcommlib.mappers;

import bticino.btcommlib.annotations.BTAttributeName;
import bticino.btcommlib.domain.lowlevel.BTOpenMessage;
import bticino.btcommlib.exceptions.LibException;

/**
 * 
 * LowLevelCtxAttr
 * 
 */

public class LowLevelCtxAttr implements ICtxAttr {
  private BTOpenMessage _lowLevelOpenMsg;

  public LowLevelCtxAttr(BTOpenMessage lowLevelOpenMsg) {
    _lowLevelOpenMsg = lowLevelOpenMsg;
  }

  /**
   * Retrieve generic BTicino field value from LowLevelOpenMsg
   * 
   * @param field
   *          Field name, correspond to annotation field ( Who, Where,
   *          etc...)
   * @return Field value
   */
  public String getCtxAttr(BTAttributeName field) throws LibException {
    switch (field) {
    case Who:
      return _lowLevelOpenMsg.getWho();
    case Where:
      return _lowLevelOpenMsg.getWhere();
    case What:
      return _lowLevelOpenMsg.getWhat();
    case Dimension:
      return _lowLevelOpenMsg.getDimension();
    default:
      throw new LibException("Field not Found");
    }
  }
}
