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

/**
 * Interface to map value from low level message to high level message and vice versa
 */
public interface IMapper {
  /**
   * Translator from high level message to low level message. For specific
   * messages it need the HighLevelCtxAttr object to discriminate correct value
   * 
   * @param key
   *          Key string, it allows to find low level message associate to
   *          specific high level message
   * @param ctxAttr
   *          Object contains other frame information (WHO, WHAT, etc...)
   * @return Low level message
   */
  String getValueToLowLevel(String key, HighLevelCtxAttr ctxAttr);

  /**
   * Translator from low level message to high level message. For specific
   * messages it need the LowLevelCtxAttr object to discriminate correct value
   * 
   * @param key
   *          Key string, it allows to find high level message associate to
   *          specific low level message
   * @param ctxAttr
   *          Object contains other frame information (WHO, WHAT, etc...)
   * @return High level message
   */
  String getValueToHighLevel(String key, LowLevelCtxAttr ctxAttr);
}
