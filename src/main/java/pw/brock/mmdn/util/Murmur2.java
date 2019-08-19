/**
 * Copyright 2014 Prasanth Jayachandran
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pw.brock.mmdn.util;

/**
 * Murmur2 32 variant.
 * 32-bit Java port of https://code.google.com/p/smhasher/source/browse/trunk/MurmurHash2.cpp#37
 * <p>
 * Obtained from https://github.com/prasanthj/hasher/blob/master/src/main/java/hasher/Murmur2.java
 * <p>
 * Changes:
 * - Removed 64 bit variant from class.
 *
 * This code is licensed under the Apache 2.0 License.
 *
 * @author Prasanth Jayachandran
 */
public class Murmur2 {

    private static final int M_32 = 0x5bd1e995;
    private static final int R_32 = 24;
    private static final int DEFAULT_SEED = 1;

    /**
     * Murmur2 32-bit variant.
     *
     * @param data - input byte array
     * @return - hashcode
     */
    public static int hash32(byte[] data) {
        return hash32(data, data.length, DEFAULT_SEED);
    }

    /**
     * Murmur2 32-bit variant.
     *
     * @param data   - input byte array
     * @param length - length of array
     * @param seed   - seed. (default 0)
     * @return - hashcode
     */
    public static int hash32(byte[] data, int length, int seed) {
        int h = seed ^ length;
        int len_4 = length >> 2;

        // body
        for (int i = 0; i < len_4; i++) {
            int i_4 = i << 2;
            int k = (data[i_4] & 0xff)
                    | ((data[i_4 + 1] & 0xff) << 8)
                    | ((data[i_4 + 2] & 0xff) << 16)
                    | ((data[i_4 + 3] & 0xff) << 24);

            // mix functions
            k *= M_32;
            k ^= k >>> R_32;
            k *= M_32;
            h *= M_32;
            h ^= k;
        }

        // tail
        int len_m = len_4 << 2;
        int left = length - len_m;
        if (left != 0) {
            if (left >= 3) {
                h ^= (int) data[length - 3] << 16;
            }
            if (left >= 2) {
                h ^= (int) data[length - 2] << 8;
            }
            if (left >= 1) {
                h ^= (int) data[length - 1];
            }

            h *= M_32;
        }

        // finalization
        h ^= h >>> 13;
        h *= M_32;
        h ^= h >>> 15;

        return h;
    }
}