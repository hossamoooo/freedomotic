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

public class DefaultDimValuesMapper implements IMapperArray {

  // / <summary>
  // / Traslator from low level messages to high level messages. For specific
  // messages it need the
  // / LowLevelCtxAttr object to discriminate correct value
  // / </summary>
  // / <param name="key">Key strings, it allows to find high level messages
  // associate to specifi low level message</param>
  // / <param name="ctxAttr">Object contains other frame information (eg. WHO,
  // WHAT, etc...)</param>
  // / <returns>High level messages array</returns>
  @Override
  public String[] getValuesToHighLevelArray(String[] keys, LowLevelCtxAttr ctxAttr) {
    return keys;
  }

  // / <summary>
  // / Traslator from high level messages to low level messages. For specific
  // messages it need the
  // / HighLevelCtxAttr object to discriminate correct value
  // / </summary>
  // / <param name="key">Key strings, it allows to find low level messages
  // associate to specifi high level message</param>
  // / <param name="ctxAttr">Object contains other frame information (eg. WHO,
  // WHAT, etc...)</param>
  // / <returns>Low level messages array</returns>
  @Override
  public String[] getValuesToLowLevelArray(String[] keys,
      HighLevelCtxAttr ctxAttr) {
    // TODO Auto-generated method stub
    return keys;
  }

}
