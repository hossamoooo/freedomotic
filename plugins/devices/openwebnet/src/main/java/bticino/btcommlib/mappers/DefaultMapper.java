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
 * A default implementation of a Mapper class.
 */
public class DefaultMapper implements IMapper {

  /**
   * @see bticino.btcommlib.mappers.IMapper#getValueToLowLevel(java.lang.String, bticino.btcommlib.mappers.HighLevelCtxAttr)
   */
  @Override
  public String getValueToLowLevel(String key, HighLevelCtxAttr ctxAttr) {
    return key;
  }

  /** 
   * @see bticino.btcommlib.mappers.IMapper#getValueToHighLevel(java.lang.String, bticino.btcommlib.mappers.LowLevelCtxAttr)
   */
  @Override
  public String getValueToHighLevel(String key, LowLevelCtxAttr ctxAttr) {
    return key;
  }

}
