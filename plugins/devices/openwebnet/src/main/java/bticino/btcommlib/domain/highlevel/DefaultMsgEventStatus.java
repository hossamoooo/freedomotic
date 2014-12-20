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

package bticino.btcommlib.domain.highlevel;

import bticino.btcommlib.annotations.Mapper;
import bticino.btcommlib.annotations.MsgType;
import bticino.btcommlib.annotations.Raw;
import bticino.btcommlib.annotations.What;
import bticino.btcommlib.annotations.Where;
import bticino.btcommlib.annotations.Who;
import bticino.btcommlib.mappers.DefaultMapper;

/**
 *
 * Class to manage Event Message
*/

@MsgType(TypeCode="EventStatus")
public class DefaultMsgEventStatus {
      @Who
      @Mapper(MapperClass=DefaultMapper.class)
      public String _who;

      @What
      @Mapper(MapperClass=DefaultMapper.class)
      public String _what; 

      @Where
      @Mapper(MapperClass=DefaultMapper.class)
      public String _where; 

      @Raw
      public String _openRawMsg;

      public String getOpenRawMsg(){return _openRawMsg; }
}
