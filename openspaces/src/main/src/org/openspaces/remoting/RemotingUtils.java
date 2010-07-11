package org.openspaces.remoting;

import com.gigaspaces.internal.reflection.IMethod;
import com.gigaspaces.internal.reflection.ReflectionUtil;
import com.gigaspaces.internal.reflection.standard.StandardMethod;
import org.springframework.util.StringUtils;

import java.io.*;
import java.lang.reflect.Method;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.*;

/**
 * @author kimchy (shay.banon)
 */
public class RemotingUtils {

    public static class MethodHash implements Externalizable {

        private byte[] hash;

        public MethodHash() {
        }

        public MethodHash(byte[] hash) {
            this.hash = hash;
        }

        public byte[] hash() {
            return this.hash;
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(hash.length);
            out.write(hash);
        }

        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            hash = new byte[in.readInt()];
            in.readFully(hash);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MethodHash that = (MethodHash) o;

            if (!Arrays.equals(hash, that.hash)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return hash != null ? Arrays.hashCode(hash) : 0;
        }
    }

    public static Map<MethodHash, IMethod> buildHashToMethodLookupForInterface(Class service, boolean useFastReflection) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DigestOutputStream dos = new DigestOutputStream(bos, digest);
            DataOutput out = new DataOutputStream(dos);

            Map<MethodHash, IMethod> map = new HashMap<MethodHash, IMethod>();
            Class[] interfaces = getAllInterfacesForInterface(service);
            for (Class inf : interfaces) {
                for (Method method : inf.getMethods()) {
                    IMethod imethod;
                    if (useFastReflection) {
                        imethod = ReflectionUtil.createMethod(method);
                    } else {
                        imethod = new StandardMethod(method);
                    }

                    digest.reset();
                    bos.reset();
                    serializeMethod(method, out);
                    map.put(new MethodHash(digest.digest()), imethod);
                }
            }
            return map;
        } catch (Exception e) {
            throw new RuntimeException("Failed to build method lookup hash", e);

        }
    }

    public static Map<Method, MethodHash> buildMethodToHashLookupForInterface(Class service, String asyncPrefix) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DigestOutputStream dos = new DigestOutputStream(bos, digest);
            DataOutput out = new DataOutputStream(dos);

            Map<Method, MethodHash> map = new HashMap<Method, MethodHash>();
            Class[] interfaces = getAllInterfacesForInterface(service);
            for (Class inf : interfaces) {
                for (Method method : inf.getMethods()) {
                    if (method.getName().startsWith(asyncPrefix)) {
                        // the method to hash is the one *without* the async prefix, but we still key by the async method
                        // this means that the method to invoke on the server side will be the non async one (the actual impl)
                        String nonAsyncMethodName = method.getName().substring(asyncPrefix.length());
                        nonAsyncMethodName = StringUtils.uncapitalize(nonAsyncMethodName);
                        Method nonAsyncMethod = inf.getMethod(nonAsyncMethodName, method.getParameterTypes());
                        digest.reset();
                        bos.reset();
                        serializeMethod(nonAsyncMethod, out);
                        map.put(method, new MethodHash(digest.digest()));
                    } else {
                        digest.reset();
                        bos.reset();
                        serializeMethod(method, out);
                        map.put(method, new MethodHash(digest.digest()));
                    }
                }
            }
            return map;
        } catch (Exception e) {
            throw new RuntimeException("Failed to build method lookup hash", e);
        }
    }

    private static void serializeMethod(Method method, DataOutput out) throws IOException {
        serializeMethod(method.getName(), method, out);
    }

    private static void serializeMethod(String methodName, Method method, DataOutput out) throws IOException {
        out.writeUTF(methodName);
        // don't serialize return type (because of async invocation)
        for (Class paramType : method.getParameterTypes()) {
            out.writeUTF(paramType.getName());
        }
    }

    private static Class[] getAllInterfacesForInterface(Class service) {
        LinkedHashSet<Class> interfaces = new LinkedHashSet<Class>();
        getAllInterfacesForInterface(service, interfaces);
        return interfaces.toArray(new Class[interfaces.size()]);
    }

    private static void getAllInterfacesForInterface(Class service, Set<Class> interfaces) {
        if (service.getInterfaces() != null && service.getInterfaces().length > 0) {
            for (Class inf : service.getInterfaces()) {
                getAllInterfacesForInterface(inf, interfaces);
            }
        } else {
            interfaces.add(service);
        }
    }
}
