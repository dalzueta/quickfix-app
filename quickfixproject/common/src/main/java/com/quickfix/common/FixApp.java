package com.quickfix.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.Application;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.RejectLogon;
import quickfix.SessionID;
import quickfix.UnsupportedMessageType;

public class FixApp implements Application {
  static Logger log = LoggerFactory.getLogger(FixApp.class);

  @Override
  public void onCreate(SessionID sessionID) {
    log.info("FixApp :: onCreate");
  }

  @Override
  public void onLogon(SessionID sessionID) {
    log.info("FixApp :: onLogon");

  }

  @Override
  public void onLogout(SessionID sessionID) {
    log.info("FixApp :: onLogout");
  }

  @Override
  public void toAdmin(Message message, SessionID sessionID) {
    log.info("FixApp :: toAdmin");
  }

  @Override
  public void fromAdmin(Message message, SessionID sessionID)
      throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
    log.info("FixApp :: fromAdmin");
  }

  @Override
  public void toApp(Message message, SessionID sessionID) throws DoNotSend {
    log.info("FixApp :: toApp");
  }

  @Override
  public void fromApp(Message message, SessionID sessionID)
      throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
    log.info("FixApp :: fromApp");
  }
}
