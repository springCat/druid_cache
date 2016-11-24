//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.springcat.druidcache.redis.serializer;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import redis.clients.util.SafeEncoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class JdkSerializer implements ISerializer {
    public static final ISerializer me = new JdkSerializer();
    private final static Log LOG = LogFactory.getLog(JdkSerializer.class);

    public JdkSerializer() {
    }

    public byte[] keyToBytes(String key) {
        return SafeEncoder.encode(key);
    }

    public String keyFromBytes(byte[] bytes) {
        return SafeEncoder.encode(bytes);
    }

    public byte[] fieldToBytes(Object field) {
        return this.valueToBytes(field);
    }

    public Object fieldFromBytes(byte[] bytes) {
        return this.valueFromBytes(bytes);
    }

    public byte[] valueToBytes(Object value) {
        ObjectOutputStream objectOut = null;

        byte[] var4;
        try {
            ByteArrayOutputStream e = new ByteArrayOutputStream();
            objectOut = new ObjectOutputStream(e);
            objectOut.writeObject(value);
            objectOut.flush();
            var4 = e.toByteArray();
        } catch (Exception var13) {
            throw new RuntimeException(var13);
        } finally {
            if(objectOut != null) {
                try {
                    objectOut.close();
                } catch (Exception var12) {
                    LOG.error(var12.getMessage(), var12);
                }
            }

        }

        return var4;
    }

    public Object valueFromBytes(byte[] bytes) {
        if(bytes != null && bytes.length != 0) {
            ObjectInputStream objectInput = null;

            Object var4;
            try {
                ByteArrayInputStream e = new ByteArrayInputStream(bytes);
                objectInput = new ObjectInputStream(e);
                var4 = objectInput.readObject();
            } catch (Exception var13) {
                throw new RuntimeException(var13);
            } finally {
                if(objectInput != null) {
                    try {
                        objectInput.close();
                    } catch (Exception var12) {
                        LOG.error(var12.getMessage(), var12);
                    }
                }

            }

            return var4;
        } else {
            return null;
        }
    }
}
