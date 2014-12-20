package bticino.btcommlib.communication;

/**
 * BTicino constant open message
 */
public class BTOpenConstFrame {
  /** OPEN frame ACK */
  public static String ACK_FRM = "*#*1##";
  /** OPEN Frame NACK */
  public static String NACK_FRM = "*#*0##";
  /** OPEN Frame Command channel code */
  public static String CMDCHAN_FRM = "*99*0##";
  /** OPEN frame Monitor channle code */
  public static String MONCHAN_FRM = "*99*1##";
  /** OPEN Frame Ack / Nack */
  public static String ACK_NACK_FRM = "*#*1##|*#*0##";
  /** OPEN connecion error, IP not in range OPEN response */
  public static String NOT_IN_RANGE_FRM = ".*\\*#[0-9]([0-9])+##";
}
