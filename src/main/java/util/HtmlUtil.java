package util;
public final class HtmlUtil { private HtmlUtil(){} public static String esc(Object v){ if(v==null)return ""; return v.toString().replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&#39;"); } }
