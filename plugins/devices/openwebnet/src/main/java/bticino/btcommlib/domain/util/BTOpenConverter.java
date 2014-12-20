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

package bticino.btcommlib.domain.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import bticino.btcommlib.annotations.DimValues;
import bticino.btcommlib.annotations.Dimension;
import bticino.btcommlib.annotations.Mapper;
import bticino.btcommlib.annotations.MsgType;
import bticino.btcommlib.annotations.Raw;
import bticino.btcommlib.annotations.What;
import bticino.btcommlib.annotations.Where;
import bticino.btcommlib.annotations.Who;
import bticino.btcommlib.domain.lowlevel.BTOpenMessage;
import bticino.btcommlib.exceptions.LibException;
import bticino.btcommlib.mappers.HighLevelCtxAttr;
import bticino.btcommlib.mappers.LowLevelCtxAttr;
import bticino.btcommlib.trace.BTLibLogger;

/**
 * BTOpenConverter
 */
public class BTOpenConverter {
  private static BTLibLogger logger = BTLibLogger
      .createLogger("BTOpenConverter");

  /**
   * returns a list of attributes of a derived class in an inheritance hierarchy
   * 
   * @param derivedType
   *          Class Type
   * @return returns a list of attributes of a derived class in an inheritance
   *         hierarchy, exception otherwise
   */
  @SuppressWarnings("rawtypes")
  public static ArrayList<Field> ListFieldsOfHierarchy(Class derivedType) {

    HashMap<String, Field> retVal = new HashMap<String, Field>();

    while (derivedType != Object.class) {
      for (Field f : derivedType.getDeclaredFields()) {
        if (!retVal.containsKey(f.getName())) {
          retVal.put(f.getName(), f);
        }
      }
      derivedType = derivedType.getSuperclass();
    }

    return new ArrayList<Field>(retVal.values());
  }

  /**
   * Get Right Type of Low level Msg with pre-compiled fields
   * 
   * @param type
   *          Class Type
   * @return Return e Low level messagio (BTOpenMessage) starting from
   *         BTOpenMsgType class type, exception if not found
   */
  private Object GetLowMsg(BTOpenMsgType classType) throws LibException {
    BTOpenMessage om = new BTOpenMessage();

    switch (classType) {
    case Request:
      om.setMsgType(BTOpenMsgType.Request);
      break;
    case RequestStatus:
      om.setMsgType(BTOpenMsgType.RequestStatus);
      break;
    case RequestDimension:
      om.setMsgType(BTOpenMsgType.RequestDimension);
      break;
    case RequestWriteDimension:
      om.setMsgType(BTOpenMsgType.RequestWriteDimension);
      break;
    default:
      throw new LibException("Message Type not found!");
    }

    return om;

  }

  /**
   * Convert from High level language message, to Low level language message
   * (BTOpenMessage) This process is enabled through the use of reflection
   * 
   * @param theHighMsg
   *          Frame to be sent, write in high level object
   * @return Object converted, exception if some errors are occurred</returns>
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Object ConvertToLowMsg(Object theHighMsg) throws LibException,
      IllegalArgumentException, IllegalAccessException {

    Class pType = theHighMsg.getClass();
    HighLevelCtxAttr highCtxAttr = new HighLevelCtxAttr(theHighMsg);

    Annotation pMsgType = pType.getAnnotation(MsgType.class);
    if (pMsgType == null) {
      throw new LibException("Class \"" + pType.getName()
          + "\" does not contain MsgType[...] attribute");
    }

    logger.debug("[MsgType] TypeName=" + ((MsgType) pMsgType).TypeCode());

    // Open Message
    BTOpenMessage oMsg = (BTOpenMessage) GetLowMsg(BTOpenMsgType
        .valueOf(((MsgType) pMsgType).TypeCode()));

    for (Field field : ListFieldsOfHierarchy(pType)) {
      String resValue = null;
      String[] resValueArray = null;

      // Get Field Value
      Object val = field.get(theHighMsg);
      if (val == null) {
        logger.debug("Field " + field.getName() + " is null, not processed...");
        continue;
      }

      Mapper pMapper = (Mapper) field.getAnnotation(Mapper.class);

      if (pMapper != null) {
        // Get Attribute
        Class classMapper = pMapper.MapperClass();
        if (classMapper == null) {
          throw new LibException("Mapper Attribute has not a correct value");
        }

        try {
          // Create Instance of Target Class
          Object targetInstance = classMapper.newInstance();

          if (val.getClass().isArray()) {
            // Invoke target (Array) method and get result
            resValueArray = (String[]) classMapper.getMethod(
                "getValuesToLowLevelArray",new Class[] { String[].class , HighLevelCtxAttr.class }).invoke(targetInstance,
                new Object[] { (String[])val, highCtxAttr });

            if (resValueArray == null) {
              throw new LibException("Field " + field.getName()
                  + " taken from " + classMapper.getName() + " is null");
            }
          } else {
            // Invoke target method and get result
            resValue = (String) classMapper.getMethod("getValueToLowLevel",
                new Class[] { String.class, HighLevelCtxAttr.class }).invoke(
                targetInstance, new Object[] { val, highCtxAttr });

            if (resValue == null) {
              throw new LibException("Field " + field.getName()
                  + " taken from " + classMapper.getName() + " is null");
            }
          }

          logger.debug("[MAPPER] ClassName=" + classMapper.getName());
          logger.debug("Field Value=" + resValue);
        } catch (LibException e) {
          throw e;
        } catch (Exception e1) {

          logger.error("Error - couldn't obtain method GetValue from "
              + classMapper.getName());
          logger.error("EXCEPTION OUTPUT " + e1.getMessage());

          throw new LibException("Conversion Method ", e1);
        }

      }
      // NO Mapper Found
      else {
        if (val.getClass().isArray())
          resValueArray = (String[]) val;
        else
          resValue = (String) val;
      }

      Who pWho = (Who) field.getAnnotation(Who.class);
      if (pWho != null) {
        logger.debug("[WHO]");
        oMsg.setWho(resValue);
        continue;
      }

      What pWhat = (What) field.getAnnotation(What.class);
      if (pWhat != null) {
        logger.debug("[What]");
        oMsg.setWhat(resValue);
        continue;
      }

      Where pWhere = (Where) field.getAnnotation(Where.class);
      if (pWhere != null) {
        logger.debug("[Where]");
        oMsg.setWhere(resValue);
        continue;
      }

      Dimension pDimension = (Dimension) field.getAnnotation(Dimension.class);
      if (pDimension != null) {
        logger.debug("[Dimension]");
        oMsg.setDimension(resValue);
        continue;
      }

      DimValues pDimValues = (DimValues) field.getAnnotation(DimValues.class);
      if (pDimValues != null) {
        logger.debug("[DimValues]");
        oMsg.setDimValues(resValueArray);
        continue;
      }

      Raw pRaw = (Raw) field.getAnnotation(Raw.class);
      if (pRaw != null) {
        logger.debug("[Raw]");
        logger.debug("Raw Value=" + resValue);
        oMsg.setOpenRawMsg(resValue);
        continue;
      }

    }

    return oMsg;
  }

  /**
   * Conversion from Low level language message (BTOpenmessage), high level
   * language message This process is enabled through the use of reflection
   * 
   * @param openMsg
   *          BTicino OPEN frame, translate in BTOpenMessage
   * @param highMsg
   *          returned object
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void ConvertToHighMsg(BTOpenMessage openMsg, Object highMsg)
      throws LibException, IllegalArgumentException, IllegalAccessException {

    Class pType = highMsg.getClass();
    LowLevelCtxAttr lowCtxAttr = new LowLevelCtxAttr(openMsg);

    for (Field field : ListFieldsOfHierarchy(pType)) {
      Object curValue = null;

      Who pWho = (Who) field.getAnnotation(Who.class);
      if (pWho != null) {
        curValue = openMsg.getWho();
        logger.debug("[WHO]");
      }

      What pWhat = (What) field.getAnnotation(What.class);
      if (pWhat != null) {
        curValue = openMsg.getWhat();
        logger.debug("[What]");
      }

      Where pWhere = (Where) field.getAnnotation(Where.class);
      if (pWhere != null) {
        curValue = openMsg.getWhere();
        logger.debug("[Where]");
      }

      Dimension pDimension = (Dimension) field.getAnnotation(Dimension.class);
      if (pDimension != null) {
        curValue = openMsg.getDimension();
        logger.debug("[Dimension]");
      }

      DimValues pDimValues = (DimValues) field.getAnnotation(DimValues.class);
      if (pDimValues != null) {
        curValue = openMsg.getDimValues();
        logger.debug("[DimValues], curValue: " + curValue.getClass().isArray());
      }

      Raw pRaw = (Raw) field.getAnnotation(Raw.class);
      if (pRaw != null) {
        curValue = openMsg.getOpenRawMsg();
        logger.debug("[Raw]");
        logger.debug("Raw Value=" + curValue);
      }

      // Check if current value is null
      if (curValue == null) {
        throw new LibException("Field " + field.getName() + " is null");
      }

      // Check for Mapper Value
      Object valueMapper = null;

      Mapper pMapper = (Mapper) field.getAnnotation(Mapper.class);
      if (pMapper != null) {
        // Get Attribute
        Class classMapper = pMapper.MapperClass();
        if (classMapper == null) {
          throw new LibException("Mapper Attribute has not a correct value");
        }

        try {
          // Create Instance of Target Class
          Object targetInstance = classMapper.newInstance();

          if (curValue.getClass().isArray())
            // Invoke target (Array) method and get result
            valueMapper = (String[]) classMapper.getMethod("getValuesToHighLevelArray", new Class[] { String[].class,  LowLevelCtxAttr.class})
                .invoke(targetInstance, new Object[] { (String[])curValue, lowCtxAttr });
          else
            // Invoke target method and get result
            valueMapper = classMapper.getMethod("getValueToHighLevel",
                new Class[] { String.class, LowLevelCtxAttr.class }).invoke(
                targetInstance, new Object[] { curValue, lowCtxAttr });

          if (valueMapper == null) {
            throw new LibException("Field " + field.getName() + " taken from "
                + classMapper.getName() + " is null");
          }

          field.set(highMsg, valueMapper);

          logger.debug("[MAPPER] ClassName=" + classMapper.getName());
          if (valueMapper.getClass().isArray()) {
            String debug = "";
            for (String val : (String[]) valueMapper) {
              debug += val;
            }
            logger.debug("Field Value=" + debug);
          } else
            logger.debug("Field Value=" + valueMapper);
        } catch (LibException e) {
          throw e;
        } catch (Exception ex) {

          logger.error("Error - couldn't obtain method GetValue from "
              + classMapper.getName());
          logger.error("EXCEPTION OUTPUT " + ex.getMessage());

          throw new LibException("Conversion Method ", ex);
        }

      }
      // NO Mapper Found
      else {
        field.set(highMsg, curValue);
      }
    }
  }
}
