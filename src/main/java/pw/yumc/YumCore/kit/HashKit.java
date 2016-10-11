/**
 * Copyright (c) 2011-2015, James Zhan 詹波 (j@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pw.yumc.YumCore.kit;

import java.security.MessageDigest;

public class HashKit {

    private static java.security.SecureRandom random = new java.security.SecureRandom();

    /**
     * 生成种子
     * <p>
     * md5 128bit 16bytes
     * <p>
     * sha1 160bit 20bytes
     * <p>
     * sha256 256bit 32bytes
     * <p>
     * sha384 384bit 48bites
     * <p>
     * sha512 512bit 64bites
     * <p>
     *
     * @param numberOfBytes
     *            数字比特
     * @return 种子字串
     */
    public static String generateSalt(int numberOfBytes) {
        byte[] salt = new byte[numberOfBytes];
        random.nextBytes(salt);
        return toHex(salt);
    }

    /**
     * 字符串加密
     *
     * @param algorithm
     *            算法
     * @param srcStr
     *            字符串
     * @return 加密后的字符串
     */
    public static String hash(String algorithm, String srcStr) {
        try {
            StringBuilder result = new StringBuilder();
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] bytes = md.digest(srcStr.getBytes("utf-8"));
            for (byte b : bytes) {
                String hex = Integer.toHexString(b & 0xFF);
                if (hex.length() == 1) {
                    result.append("0");
                }
                result.append(hex);
            }
            return result.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * MD5加密
     *
     * @param srcStr
     *            字符串
     * @return 加密后的字符串
     */
    public static String md5(String srcStr) {
        return hash("MD5", srcStr);
    }

    /**
     * sha1加密
     *
     * @param srcStr
     *            字符串
     * @return 加密后的字符串
     */
    public static String sha1(String srcStr) {
        return hash("SHA-1", srcStr);
    }

    /**
     * sha256加密
     *
     * @param srcStr
     *            字符串
     * @return 加密后的字符串
     */
    public static String sha256(String srcStr) {
        return hash("SHA-256", srcStr);
    }

    /**
     * sha384加密
     *
     * @param srcStr
     *            字符串
     * @return 加密后的字符串
     */
    public static String sha384(String srcStr) {
        return hash("SHA-384", srcStr);
    }

    /**
     * sha512加密
     *
     * @param srcStr
     *            字符串
     * @return 加密后的字符串
     */
    public static String sha512(String srcStr) {
        return hash("SHA-512", srcStr);
    }

    /**
     * Byte转字符串
     *
     * @param bytes
     *            Byte数组
     * @return 字符串
     */
    private static String toHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                result.append("0");
            }
            result.append(hex);
        }
        return result.toString();
    }
}
