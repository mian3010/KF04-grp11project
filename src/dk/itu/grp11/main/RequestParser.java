package dk.itu.grp11.main;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;

import dk.itu.grp11.data.Map;
import dk.itu.grp11.data.Parser;
import dk.itu.grp11.data.Road;
import dk.itu.grp11.enums.TransportationType;
import dk.itu.grp11.files.ResourceGetter;
import dk.itu.grp11.gui.GUIResourceGetter;
/**
 * 
 * @author Group 11
 *
 * @param con Our socket
 * @param in Our Reader
 * @param out Sends all the html and svg to the browser
 * @param pout Tells the browser which content is send, and if it fails
 * @param fileserver An object of our fileserver class
 */
public class RequestParser extends Thread {
  
  private Map map;
  private String request;
  
  private Socket con;
  private BufferedReader in;
  private OutputStream out;
  private PrintStream pout;
  
  public RequestParser(Map map, Socket con) throws IOException {
    this.con = con;
    
    in = new BufferedReader
        (new InputStreamReader(con.getInputStream()));
    request = in.readLine();
    con.shutdownInput();    
    out = new BufferedOutputStream(con.getOutputStream());
    pout = new PrintStream(out);
    
    this.map = map;
  }
  /** 
   * Overrided from Thread, this method is needed for running with multiple threads at the same time. 
   */
  public void run() {
    if (request != null) {
      try {
        parseRequest(request);
        pout.flush();
        con.close();
      } catch (IOException e) {
        System.err.println(e);
      }
    }
  }
  /**Takes a String that could look like this: /getMap?x=943702&y=6366840&width=69061&height=35214&zoomlevel=1&=1334088780921
   * Then it splits it so we get the data we actually need
   * The last part is the actual time its parsed, we should never have two identical values here
   */
  private void parseRequest(String request) throws IOException {
    //Get file request and params
    String[] split = request.split("\\?");
    String[] split2 = split[0].split(" ");
    String file = split2[1].substring(1);
    HashMap<String, String> params = new HashMap<String, String>();
    if (split.length > 1) {
      String[] split3 = split[1].split(" ");
      String[] paramSplit = split3[0].split("&");
      for (String param : paramSplit) {
        String[] split4 = param.split("=");
        String parameter = "";
        if (split4.length == 2) parameter = split4[1];
        params.put(split4[0], parameter);
      }
    };
    if (file.length() == 0) processRequest("head.html", null);
    else processRequest(file, params);
  }
  /** Returns a String containing the values of the XStart YStart, XDiff and YDiff
   * which is needed when you resize the canvas, when you zoom in on the map 
   */
  private String getMinMax(String sessionID) {
    return FileServer.sessions.get(sessionID).getXStart() + " " + FileServer.sessions.get(sessionID).getYStart() + " " + FileServer.sessions.get(sessionID).getXDiff() + " " + FileServer.sessions.get(sessionID).getYDiff();
  }
  /**
   * processRequest takes the file if we look at our other example from line64 it would be getMap
   * Then it does many checks to see what kind of info it received and acts accordingly 
   * So if the file is something else than the else if's it sets the set of coordinates used to draw the map
   * The next bit, decides what the pout will tell the browser about which content will be sent, and if problems occurred 
   */
  private void processRequest(String file, HashMap<String, String> params) throws IOException {
    InputStream outStream = null;
    String contenttype = "text/html";
    if (file.indexOf("getMinMax") != -1) {
      outStream = new ByteArrayInputStream(getMinMax(params.get("sessionID")).getBytes("UTF-8"));
    } else if (file.indexOf("generateSessionID") != -1) {
      outStream = new ByteArrayInputStream(generateSessionID().getBytes("UTF-8"));
    } else if (file.indexOf("getZoomLevelX") != -1) { 
      outStream = new ByteArrayInputStream((""+Map.getZoomLevelX(FileServer.sessions.get(params.get("sessionID")).getXDiff())).getBytes("UTF-8"));
    } else if (file.indexOf("getZoomLevelY") != -1) { 
      outStream = new ByteArrayInputStream((""+Map.getZoomLevelY(FileServer.sessions.get(params.get("sessionID")).getYDiff())).getBytes("UTF-8"));
    } else if (file.indexOf("getMap") != -1) {
      outStream = new ByteArrayInputStream(map.getPart(Double.parseDouble(params.get("x")), Double.parseDouble(params.get("y")), Double.parseDouble(params.get("width")), Double.parseDouble(params.get("height")), Integer.parseInt(params.get("zoomlevel")), FileServer.sessions.get(params.get("sessionID"))).getBytes("UTF-8"));
    } else if (file.indexOf("getRoute") != -1) {
      Road from = Parser.getParser().roadNames().get(URLDecoder.decode(params.get("from"), "UTF-8").toLowerCase());
      Road to = Parser.getParser().roadNames().get(URLDecoder.decode(params.get("to"), "UTF-8").toLowerCase());
      boolean ferry = params.get("ferry").equals("true");
      boolean highway = params.get("highway").equals("true");
      
      TransportationType trans; boolean fastest = false;
      if(params.get("type").equals("walk")) {
        trans = TransportationType.WALK;
      }
      else if(params.get("type").equals("bicycle")) {
        trans = TransportationType.BICYCLE;
      }
      else {
        trans = TransportationType.CAR;
        fastest = true; //The time data is only for cars
      }
      
      String route;
      if(from == null || to == null) route = "alert('Could not calculate route. From- or to-road is not valid.');";
      else if(from == to) route = Map.getMap().getRoute(from.getFrom(), from.getTo(), trans, fastest, ferry, highway);
      else route = Map.getMap().getRoute(from.getFrom(), to.getFrom(), trans, fastest, ferry, highway);
      
      if(route.equals(""))
        route = "alert('No such route exist. If you have disabled either highways or ferries, try enabling them again.');";

      outStream = new ByteArrayInputStream(route.getBytes("UTF-8"));
    } else if (file.indexOf("autoCompletion") != -1) {
      String term = URLDecoder.decode(params.get("term"), "UTF-8");
      outStream = new ByteArrayInputStream(Parser.getParser().roadsWithPrefix(term).getBytes("UTF-8")); //term
    } else if (file.indexOf("removeRoads") != -1) {
      outStream = new ByteArrayInputStream((FileServer.sessions.get(params.get("sessionID")).removeRoads(params.get("IDs"))).getBytes("UTF-8"));
    } else if (file.indexOf("getCoastLine") != -1) {
      outStream = new ByteArrayInputStream(map.getCoastLine().getBytes("UTF-8"));
    } else if (file.indexOf("setCanvas") != -1) {
      if (params.size() < 4) {
        synchronized(FileServer.sessions.get(params.get("sessionID"))) {
          FileServer.sessions.get(params.get("sessionID")).resetMinMax();
        }
      }
      else {
        synchronized (FileServer.sessions.get(params.get("sessionID"))) {
          FileServer.sessions.get(params.get("sessionID")).setXStart(Integer.parseInt(params.get("x")));
          FileServer.sessions.get(params.get("sessionID")).setYStart(Integer.parseInt(params.get("y")));
          FileServer.sessions.get(params.get("sessionID")).setXDiff(Integer.parseInt(params.get("width")));
          FileServer.sessions.get(params.get("sessionID")).setYDiff(Integer.parseInt(params.get("height")));
        }
      }
      outStream = new ByteArrayInputStream(("Success").getBytes("UTF-8"));
    } else {
      InputStream f = null;
      if (file.indexOf("head.html") != -1) f = GUIResourceGetter.class.getResourceAsStream(file);
      else if (file.indexOf("layout.css") != -1) f = GUIResourceGetter.class.getResourceAsStream(file);
      else if (file.indexOf("load.js") != -1) f = GUIResourceGetter.class.getResourceAsStream(file);
      else f = ResourceGetter.class.getResourceAsStream(file);
      outStream = f;
      if (file.indexOf(".js") != -1) contenttype = "text/javascript";
      else if (file.indexOf(".html") != -1) contenttype = "text/html";
      else if (file.indexOf(".css") != -1) contenttype = "text/css";
      else if (file.indexOf(".png") != -1 || file.indexOf(".gif") != -1) contenttype = "Image";
      else contenttype = "text/plain";
    }
    pout.print("HTTP/1.0 200 OK\r\n");
    pout.print("Content-Type: "+contenttype+"\r\n");
    pout.print(": "+new Date()+"\r\n"+
               "Server: IXWT FileServer 1.0\r\n\r\n");
    sendOutput(outStream, out); // send raw output
    FileServer.log("Done processing "+request+" (200 OK)");
  }
  /**
   * Generates a unique ID for each computer accessing our fileserver, it is based on the Systems current
   * time as well as well as the RemoteSocketAdress, which is the users IP amongst other information.
   * @return the ID generated.
   */
  private String generateSessionID() {
    String sessionID = ""+System.nanoTime()+con.getRemoteSocketAddress();
    FileServer.sessions.put(sessionID, new Session(sessionID));
    return sessionID;
  }
  
  /** 
   * We make our output into a byte[] for the browser to read
   *  
   */
  private void sendOutput(InputStream outStream, OutputStream out) 
      throws IOException {
    byte[] buffer = new byte[1000];
    while (outStream.available()>0) 
      out.write(buffer, 0, outStream.read(buffer));
  }
}
