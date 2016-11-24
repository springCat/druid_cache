//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.springcat.druidcache.redis.serializer;

public interface ISerializer {
    byte[] keyToBytes(String var1);

    String keyFromBytes(byte[] var1);

    byte[] fieldToBytes(Object var1);

    Object fieldFromBytes(byte[] var1);

    byte[] valueToBytes(Object var1);

    Object valueFromBytes(byte[] var1);
}
