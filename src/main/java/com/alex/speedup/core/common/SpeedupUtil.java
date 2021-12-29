package com.alex.speedup.core.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author xianshuangzhang@gmail.com
 * @date 2021-12-29 3:03 下午
 */
public class SpeedupUtil {
    public SpeedupUtil() {
    }

    public static <T> T deepCopy(T serializable) {
        if (null != serializable) {
            ByteArrayOutputStream bos = null;
            ObjectOutputStream oos = null;
            ByteArrayInputStream bis = null;
            ObjectInputStream ois = null;

            T var7;
            try {
                bos = new ByteArrayOutputStream();
                oos = new ObjectOutputStream(bos);
                oos.writeObject(serializable);
                bis = new ByteArrayInputStream(bos.toByteArray());
                ois = new ObjectInputStream(bis);
                T object = (T) ois.readObject();
                if (null == object) {
                    return null;
                }

                var7 = object;
            } catch (Exception var33) {
                var33.printStackTrace();
                return null;
            } finally {
                if (null != ois) {
                    try {
                        ois.close();
                    } catch (IOException var32) {
                        var32.printStackTrace();
                    }
                }

                if (null != bis) {
                    try {
                        bis.close();
                    } catch (IOException var31) {
                        var31.printStackTrace();
                    }
                }

                if (null != oos) {
                    try {
                        oos.close();
                    } catch (IOException var30) {
                        var30.printStackTrace();
                    }
                }

                if (null != bos) {
                    try {
                        bos.close();
                    } catch (IOException var29) {
                        var29.printStackTrace();
                    }
                }

            }

            return var7;
        } else {
            return null;
        }
    }
}