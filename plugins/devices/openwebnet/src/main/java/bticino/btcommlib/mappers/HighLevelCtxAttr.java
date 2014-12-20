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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import bticino.btcommlib.annotations.BTAttributeName;
import bticino.btcommlib.annotations.Dimension;
import bticino.btcommlib.annotations.What;
import bticino.btcommlib.annotations.Where;
import bticino.btcommlib.annotations.Who;
import bticino.btcommlib.domain.util.BTOpenConverter;
import bticino.btcommlib.exceptions.LibException;
import bticino.btcommlib.trace.BTLibLogger;

/**
 * The high level attributes class
 */
public class HighLevelCtxAttr implements ICtxAttr {
  private Object _highLevelOpenMsg;

  /**
   * It contain the list of fields of highLevelOpenMsg
   */
  @SuppressWarnings("rawtypes")
  private HashMap<Class, String> ListFields;

  private static BTLibLogger logger = BTLibLogger
      .createLogger("HighLevelCtxAttr");

  /**
   * Create new instance of HighLevelCtxAttr. It rebuild high level message,
   * defined by user, into basic BTicino attributes
   * 
   * @param highLevelOpenMsg
   *          High level message, defined by user
   */
  @SuppressWarnings("rawtypes")
  public HighLevelCtxAttr(Object highLevelOpenMsg)
      throws IllegalArgumentException, IllegalAccessException {
    _highLevelOpenMsg = highLevelOpenMsg;
    ListFields = new HashMap<Class, String>();
    ArrayList<Field> fieldList = BTOpenConverter
        .ListFieldsOfHierarchy(highLevelOpenMsg.getClass());
    for (Field field : fieldList) {
      Who pWho = (Who) field.getAnnotation(Who.class);
      if (pWho != null) {
        logger.debug("[MsgType] TypeName=" + pWho.getClass().getName());
        ListFields.put(Who.class, (String) field.get(_highLevelOpenMsg));
        continue;
      }
      What pWhat = (What) field.getAnnotation(What.class);
      if (pWhat != null) {
        logger.debug("[MsgType] TypeName=" + pWhat.getClass().getName());
        ListFields.put(What.class, (String) field.get(_highLevelOpenMsg));
        continue;
      }
      Where pWhere = (Where) field.getAnnotation(Where.class);
      if (pWhere != null) {
        logger.debug("[MsgType] TypeName=" + pWhere.getClass().getName());
        ListFields.put(Where.class, (String) field.get(_highLevelOpenMsg));
        continue;
      }
      Dimension pDimension = (Dimension) field.getAnnotation(Dimension.class);
      if (pDimension != null) {
        logger.debug("[MsgType] TypeName=" + pDimension.getClass().getName());
        ListFields.put(Dimension.class, (String) field.get(_highLevelOpenMsg));
        continue;
      }
    }
  }

  /**
   * Retrieve generic BTicino field value from HighLevelOpenMsg
   * 
   * @param fieldField
   *          name, correspond to annotation field ( Who, Where, etc...)
   * @return Field value
   */
  public String getCtxAttr(BTAttributeName field) throws LibException {
    switch (field) {
    case Who:
      if (ListFields.containsKey(Who.class))
        return ListFields.get(Who.class);
      break;
    case Where:
      if (ListFields.containsKey(Where.class))
        return ListFields.get(Where.class);
      break;
    case What:
      if (ListFields.containsKey(What.class))
        return ListFields.get(What.class);
      break;
    case Dimension:
      if (ListFields.containsKey(Dimension.class))
        return ListFields.get(Dimension.class);
      break;
    default:
      throw new LibException("Field not Found");
    }
    return "";
  }
}
