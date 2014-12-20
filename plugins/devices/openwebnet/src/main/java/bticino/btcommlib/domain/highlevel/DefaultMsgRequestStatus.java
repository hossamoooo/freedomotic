package bticino.btcommlib.domain.highlevel;

import bticino.btcommlib.annotations.Mapper;
import bticino.btcommlib.annotations.MsgType;
import bticino.btcommlib.annotations.Raw;
import bticino.btcommlib.annotations.Where;
import bticino.btcommlib.annotations.Who;
import bticino.btcommlib.mappers.DefaultMapper;

/**
 * 
 * Class to manage Request Status
 */

@MsgType(TypeCode = "RequestStatus")
public class DefaultMsgRequestStatus {

  @Who
  @Mapper(MapperClass = DefaultMapper.class)
  public String _who;

  @Where
  @Mapper(MapperClass = DefaultMapper.class)
  public String _where;

  @Raw
  public String _openRawMsg;

  public String getOpenRawMsg() {
    return _openRawMsg;
  }

}