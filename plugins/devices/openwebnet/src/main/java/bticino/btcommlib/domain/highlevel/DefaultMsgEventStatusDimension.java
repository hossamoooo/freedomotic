package bticino.btcommlib.domain.highlevel;

import bticino.btcommlib.annotations.DimValues;
import bticino.btcommlib.annotations.Dimension;
import bticino.btcommlib.annotations.Mapper;
import bticino.btcommlib.annotations.MsgType;
import bticino.btcommlib.annotations.Raw;
import bticino.btcommlib.annotations.Where;
import bticino.btcommlib.annotations.Who;
import bticino.btcommlib.mappers.DefaultDimValuesMapper;
import bticino.btcommlib.mappers.DefaultMapper;

/**
*
* Class to manage Event Message with Dimensions
*/

@MsgType(TypeCode = "EventStatusDimension")
public class DefaultMsgEventStatusDimension {

  @Who
  @Mapper(MapperClass = DefaultMapper.class)
  public String _who;

  @Where
  @Mapper(MapperClass = DefaultMapper.class)
  public String _where;

  @Dimension
  @Mapper(MapperClass = DefaultMapper.class)
  public String _dimension;

  @DimValues
  @Mapper(MapperClass = DefaultDimValuesMapper.class)
  public String[] _dimValues;

  @Raw
  public String _openRawMsg;

  public String getOpenRawMsg() {
    return _openRawMsg;
  }

}