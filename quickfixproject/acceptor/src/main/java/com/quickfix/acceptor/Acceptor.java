package com.quickfix.acceptor;

import com.quickfix.common.FixApp;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.FileLogFactory;
import quickfix.FileStoreFactory;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.LogFactory;
import quickfix.Message;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;
import quickfix.SocketInitiator;
import quickfix.UnsupportedMessageType;
import quickfix.field.AvgPx;
import quickfix.field.ClOrdID;
import quickfix.field.ContraBroker;
import quickfix.field.ContraTrader;
import quickfix.field.CumQty;
import quickfix.field.ExecID;
import quickfix.field.ExecType;
import quickfix.field.LastPx;
import quickfix.field.LastQty;
import quickfix.field.LeavesQty;
import quickfix.field.MsgType;
import quickfix.field.NoContraBrokers;
import quickfix.field.OrdStatus;
import quickfix.field.OrderID;
import quickfix.field.Password;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.Text;
import quickfix.field.Username;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.QuoteRequest;
import quickfix.mina.SessionConnector;

public class Acceptor {
  private static final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
  private static Map<SessionID, Session> sessionsMap = new HashMap<>();
  private static Session defaultSession;
  static Logger log = LoggerFactory.getLogger(Acceptor.class);
  static String beginString ="FIX.4.4";
  public static void main(String[] args) throws IOException, ConfigError {
//    List<SessionID> sessionIDList = new ArrayList<>();


    String type = args[0]; // type of socket acceptor or initiator
    String sender = args[1]; // sender
    String target = args[2]; // target

//    sessionIDList.add(createSessionId(beginString, sender, target));
    SessionID sessionId = createSessionId(beginString, sender, target);

    InputStream settingsStream = Acceptor.class.getClassLoader().getResourceAsStream("quickfix_session.conf");
    SessionSettings settings = new SessionSettings(settingsStream);

    configureSettings(settings, sessionId, type);

    MessageStoreFactory storeFactory = new FileStoreFactory(settings);
    LogFactory logFactory = new FileLogFactory(settings);
    MessageFactory messageFactory = new DefaultMessageFactory();

    log.info("Iniciando " + type);

    FixApp acceptorFix = new FixApp(){
      @Override
      public void onLogon(SessionID sessionID) {
        Session session = Session.lookupSession(sessionID);
        if(!sessionsMap.containsKey(sessionID) || sessionsMap.get(sessionID) == session){
          sessionsMap.put(sessionID, session);
          defaultSession = session;
        }
        super.onLogon(sessionID);
      }

      @Override
      public void onCreate(SessionID sessionID) {
        log.info("onCreate:: " + sessionID);
      }

      @Override
      public void onLogout(SessionID sessionID) {
        if(sessionsMap.containsKey(sessionID)){
          sessionsMap.remove(sessionID);
        }
        super.onLogout(sessionID);
      }

      @Override
      public void toAdmin(Message message, SessionID sessionID) {
        super.toAdmin(message, sessionID);
      }

      @Override
      public void fromAdmin(Message message, SessionID sessionID)
          throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        super.fromAdmin(message, sessionID);
      }

      @Override
      public void toApp(Message message, SessionID sessionID) throws DoNotSend {
        super.toApp(message, sessionID);
      }

      @Override
      public void fromApp(Message message, SessionID sessionID)
          throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        super.fromApp(message, sessionID);
      }
    };

    SessionConnector socket;
    if(type.equalsIgnoreCase("acceptor")){
      socket =  new SocketAcceptor(acceptorFix, storeFactory, settings, logFactory, messageFactory);
    }else {
      // initiator



      socket =  new SocketInitiator(acceptorFix, storeFactory, settings, logFactory, messageFactory);

    }
    socket.start();
//
    if(!type.equalsIgnoreCase("acceptor")) {

//      if(sessionsMap.containsKey(sessionId) && sessionsMap.get(sessionId) !=null ){
//
//
//
//
        Message message = messageFactory.create(beginString, MsgType.LOGON);
        message.setField(Username.FIELD, new Text("daniel"));
        message.setField(Password.FIELD, new Password("tucuman"));

      scheduledExecutorService.scheduleAtFixedRate(() -> sendHelloWorld(sessionId, message), 5, 5, TimeUnit.SECONDS);

    }
  }

  private static SessionID createSessionId(String beginString, String sender, String target) {
    return new SessionID(beginString, sender, target);
  }
  private static void configureSettings(SessionSettings settings, SessionID sessionID, String connectionType) {
      settings.setString(sessionID, "BeginString", sessionID.getBeginString());
      settings.setString(sessionID, "SenderCompID", sessionID.getSenderCompID());
      settings.setString(sessionID, "TargetCompID", sessionID.getTargetCompID());
      settings.setString(sessionID, "ConnectionType", connectionType);
      if(connectionType.equalsIgnoreCase("acceptor")){
        settings.setString(sessionID, "FileStorePath", "target/data/acceptor_store");
        settings.setString(sessionID, "FileLogPath", "target/data/acceptor_log");
      }else {
        settings.setString(sessionID, "FileStorePath", "target/data/initiator_store");
        settings.setString(sessionID, "FileLogPath", "target/data/initiator_log");
      }
  }

  private static void sendHelloWorld(SessionID sessionId, Message message) {
    log.info("ENVIANDO MENSAJE 1 : isLoggon :: " +isLoggedOn());

    if (isLoggedOn()) {
      log.info("ENVIANDO MENSAJE ");

      QuoteRequest quoteRequest = new QuoteRequest();
      quoteRequest.setString(Text.FIELD, "Hello World  LLLLLLL");
      sessionsMap.get(sessionId).send(message);


      ExecutionReport executionReport = new ExecutionReport();

      // Set required fields
      executionReport.set(new OrderID("123456789"));
      executionReport.set(new ExecID("987654321"));
      executionReport.set(new ExecType(ExecType.FILL));
      executionReport.set(new OrdStatus(OrdStatus.FILLED));
      executionReport.set(new Symbol("AAPL"));
      executionReport.set(new Side(Side.BUY));
      executionReport.set(new LeavesQty(0));
      executionReport.set(new CumQty(100));
      executionReport.set(new AvgPx(150.25));

      // Add optional fields
      executionReport.set(new ClOrdID("987654"));
      executionReport.set(new Price(150.50));
      executionReport.set(new LastQty(100));
      executionReport.set(new LastPx(150.50));
      executionReport.set(new Text("Execution report for order #123456789"));

      // Add NoContraBrokers group (optional repeating group)
      ;

      sessionsMap.get(sessionId).send(executionReport);

//
//      Message message = messageFactory.create(beginString, MsgType.LOGON);
//      message.getHeader().setString(Username.FIELD, "daniel");
//      message.getHeader().setString(Password.FIELD, "tucuman");
//
//      sessionsMap.get(sessionId).send(message);



    } else {
      log.info("LA DE LA LORA");
      log.info("La sesión no ha iniciado sesión, el mensaje no será enviado.");
    }
  }

  private static boolean isLoggedOn() {
    return defaultSession != null && defaultSession.isLoggedOn();
  }
}
