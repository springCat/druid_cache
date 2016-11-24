package org.springcat.druidcache.redis.serializer;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;
import redis.clients.util.SafeEncoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class FstSerializer implements ISerializer {
    public static final ISerializer me = new FstSerializer();

    public FstSerializer() {
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
        FSTObjectOutput fstOut = null;

        byte[] var4;
        try {
            ByteArrayOutputStream e = new ByteArrayOutputStream();
            fstOut = new FSTObjectOutput(e);
            fstOut.writeObject(value);
            fstOut.flush();
            var4 = e.toByteArray();
        } catch (Exception var13) {
            throw new RuntimeException(var13);
        } finally {
            if(fstOut != null) {
                try {
                    fstOut.close();
                } catch (IOException var12) {
//                    LogKit.error(var12.getMessage(), var12);
                }
            }

        }

        return var4;
    }

    public Object valueFromBytes(byte[] bytes) {
        if(bytes != null && bytes.length != 0) {
            FSTObjectInput fstInput = null;

            Object e;
            try {
                fstInput = new FSTObjectInput(new ByteArrayInputStream(bytes));
                e = fstInput.readObject();
            } catch (Exception var12) {
                throw new RuntimeException(var12);
            } finally {
                if(fstInput != null) {
                    try {
                        fstInput.close();
                    } catch (IOException var11) {
//                        LogKit.error(var11.getMessage(), var11);
                    }
                }

            }

            return e;
        } else {
            return null;
        }
    }
}
