//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.springcat.druidcache.redis;

public interface IKeyNamingPolicy {
    IKeyNamingPolicy defaultKeyNamingPolicy = new IKeyNamingPolicy() {
        public String getKeyName(Object key) {
            return key.toString();
        }
    };

    String getKeyName(Object var1);
}
