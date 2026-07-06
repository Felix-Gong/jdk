/*
 * Copyright (c) 2026, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need information or have any questions.
 */

/**
 * @test
 * @summary Verify RISC-V Zbs single-bit instructions in C2
 *   bseti rd, rs1, imm  ->  rs1 | (1L << imm)   when (1L << imm) is power of 2
 *   bclri rd, rs1, imm  ->  rs1 & ~(1L << imm)  when ~(1L << imm) has one zero bit
 *   binvi rd, rs1, imm  ->  rs1 ^ (1L << imm)   when (1L << imm) is power of 2
 *
 *   Int patterns match:
 *     OrI(src1, immIpowerOf2)         -> bseti
 *     AndI(src1, immI_not_powerOf2)   -> bclri
 *     XorI(src1, immIpowerOf2)        -> binvi
 *
 *   Long patterns match:
 *     OrL(src1, immLpowerOf2)         -> bseti
 *     AndL(src1, immL_not_powerOf2)   -> bclri
 *     XorL(src1, immLpowerOf2)        -> binvi
 *
 * @requires os.arch == "riscv64"
 *
 * @run main/othervm -Xbatch -XX:-TieredCompilation
 *      -XX:CompileCommand=dontinline,compiler.codegen.TestZbsSingleBit::*
 *      compiler.codegen.TestZbsSingleBit
 * @run main/othervm -Xbatch -XX:+TieredCompilation -XX:TieredStopAtLevel=1
 *      -XX:CompileCommand=dontinline,compiler.codegen.TestZbsSingleBit::*
 *      compiler.codegen.TestZbsSingleBit
 * @run main/othervm -Xbatch -XX:+TieredCompilation
 *      -XX:CompileCommand=dontinline,compiler.codegen.TestZbsSingleBit::*
 *      compiler.codegen.TestZbsSingleBit
 */

package compiler.codegen;

public class TestZbsSingleBit {
    private static final int ITERATIONS = 1_000_000;

    // === int bseti: src1 | immIpowerOf2 ===

    static int bsetiIntBit0(int a)  { return a | (1 << 0); }
    static int bsetiIntBit1(int a)  { return a | (1 << 1); }
    static int bsetiIntBit11(int a) { return a | (1 << 11); }
    static int bsetiIntBit30(int a) { return a | (1 << 30); }
    static int bsetiIntBit31(int a) { return a | (1 << 31); }

    // === int bclri: src1 & immI_not_powerOf2 ===

    static int bclriIntBit0(int a)  { return a & ~(1 << 0); }
    static int bclriIntBit1(int a)  { return a & ~(1 << 1); }
    static int bclriIntBit15(int a) { return a & ~(1 << 15); }
    static int bclriIntBit30(int a) { return a & ~(1 << 30); }
    static int bclriIntBit31(int a) { return a & ~(1 << 31); }

    // === int binvi: src1 ^ immIpowerOf2 ===

    static int binviIntBit0(int a)  { return a ^ (1 << 0); }
    static int binviIntBit1(int a)  { return a ^ (1 << 1); }
    static int binviIntBit12(int a) { return a ^ (1 << 12); }
    static int binviIntBit30(int a) { return a ^ (1 << 30); }
    static int binviIntBit31(int a) { return a ^ (1 << 31); }

    // === long bseti: src1 | immLpowerOf2 ===

    static long bsetiLongBit0(long a)  { return a | (1L << 0); }
    static long bsetiLongBit1(long a)  { return a | (1L << 1); }
    static long bsetiLongBit32(long a) { return a | (1L << 32); }
    static long bsetiLongBit62(long a) { return a | (1L << 62); }
    static long bsetiLongBit63(long a) { return a | (1L << 63); }

    // === long bclri: src1 & immL_not_powerOf2 ===

    static long bclriLongBit0(long a)  { return a & ~(1L << 0); }
    static long bclriLongBit1(long a)  { return a & ~(1L << 1); }
    static long bclriLongBit33(long a) { return a & ~(1L << 33); }
    static long bclriLongBit62(long a) { return a & ~(1L << 62); }
    static long bclriLongBit63(long a) { return a & ~(1L << 63); }

    // === long binvi: src1 ^ immLpowerOf2 ===

    static long binviLongBit0(long a)  { return a ^ (1L << 0); }
    static long binviLongBit1(long a)  { return a ^ (1L << 1); }
    static long binviLongBit33(long a) { return a ^ (1L << 33); }
    static long binviLongBit62(long a) { return a ^ (1L << 62); }
    static long binviLongBit63(long a) { return a ^ (1L << 63); }

    // === Test data ===

    static int[] intVals = {
        0,
        1,
        -1,
        Integer.MAX_VALUE,
        Integer.MIN_VALUE,
        0xFF,
        0xFFFF,
        0xAAAAAAAA,
        0x55555555,
        0x12345678,
        0x89ABCDEF,
    };

    static long[] longVals = {
        0L,
        1L,
        -1L,
        Long.MAX_VALUE,
        Long.MIN_VALUE,
        0xFFL,
        0xFFFFL,
        0xFFFFFFFFL,
        0xAAAAAAAAAAAAAAAAL,
        0x5555555555555555L,
        0x123456789ABCDEF0L,
        0xFEDCBA9876543210L,
    };

    public static void main(String[] args) {
        System.out.println("Testing RISC-V Zbs single-bit instructions via C2...");

        // === Int bseti tests ===
        for (int a : intVals) {
            checkInt("bsetiIntBit0(" + a + ")",  bsetiIntBit0(a),  a | (1 << 0));
            checkInt("bsetiIntBit1(" + a + ")",  bsetiIntBit1(a),  a | (1 << 1));
            checkInt("bsetiIntBit11(" + a + ")", bsetiIntBit11(a), a | (1 << 11));
            checkInt("bsetiIntBit30(" + a + ")", bsetiIntBit30(a), a | (1 << 30));
            checkInt("bsetiIntBit31(" + a + ")", bsetiIntBit31(a), a | (1 << 31));
        }

        // === Int bclri tests ===
        for (int a : intVals) {
            checkInt("bclriIntBit0(" + a + ")",  bclriIntBit0(a),  a & ~(1 << 0));
            checkInt("bclriIntBit1(" + a + ")",  bclriIntBit1(a),  a & ~(1 << 1));
            checkInt("bclriIntBit15(" + a + ")", bclriIntBit15(a), a & ~(1 << 15));
            checkInt("bclriIntBit30(" + a + ")", bclriIntBit30(a), a & ~(1 << 30));
            checkInt("bclriIntBit31(" + a + ")", bclriIntBit31(a), a & ~(1 << 31));
        }

        // === Int binvi tests ===
        for (int a : intVals) {
            checkInt("binviIntBit0(" + a + ")",  binviIntBit0(a),  a ^ (1 << 0));
            checkInt("binviIntBit1(" + a + ")",  binviIntBit1(a),  a ^ (1 << 1));
            checkInt("binviIntBit12(" + a + ")", binviIntBit12(a), a ^ (1 << 12));
            checkInt("binviIntBit30(" + a + ")", binviIntBit30(a), a ^ (1 << 30));
            checkInt("binviIntBit31(" + a + ")", binviIntBit31(a), a ^ (1 << 31));
        }

        // === Long bseti tests ===
        for (long a : longVals) {
            checkLong("bsetiLongBit0(" + a + ")",  bsetiLongBit0(a),  a | (1L << 0));
            checkLong("bsetiLongBit1(" + a + ")",  bsetiLongBit1(a),  a | (1L << 1));
            checkLong("bsetiLongBit32(" + a + ")", bsetiLongBit32(a), a | (1L << 32));
            checkLong("bsetiLongBit62(" + a + ")", bsetiLongBit62(a), a | (1L << 62));
            checkLong("bsetiLongBit63(" + a + ")", bsetiLongBit63(a), a | (1L << 63));
        }

        // === Long bclri tests ===
        for (long a : longVals) {
            checkLong("bclriLongBit0(" + a + ")",  bclriLongBit0(a),  a & ~(1L << 0));
            checkLong("bclriLongBit1(" + a + ")",  bclriLongBit1(a),  a & ~(1L << 1));
            checkLong("bclriLongBit33(" + a + ")", bclriLongBit33(a), a & ~(1L << 33));
            checkLong("bclriLongBit62(" + a + ")", bclriLongBit62(a), a & ~(1L << 62));
            checkLong("bclriLongBit63(" + a + ")", bclriLongBit63(a), a & ~(1L << 63));
        }

        // === Long binvi tests ===
        for (long a : longVals) {
            checkLong("binviLongBit0(" + a + ")",  binviLongBit0(a),  a ^ (1L << 0));
            checkLong("binviLongBit1(" + a + ")",  binviLongBit1(a),  a ^ (1L << 1));
            checkLong("binviLongBit33(" + a + ")", binviLongBit33(a), a ^ (1L << 33));
            checkLong("binviLongBit62(" + a + ")", binviLongBit62(a), a ^ (1L << 62));
            checkLong("binviLongBit63(" + a + ")", binviLongBit63(a), a ^ (1L << 63));
        }

        // === Hot loops to force C2 compilation ===
        // Int bseti
        {
            int r = intVals[0];
            for (int i = 0; i < ITERATIONS; i++) {
                r = bsetiIntBit1(r);
                r = bclriIntBit0(r);
                r = binviIntBit12(r);
                r = bsetiIntBit31(r);
            }
            if (r == bsetiIntBit1(r)) { r ^= 1; }
        }

        // Long bseti
        {
            long r = longVals[0];
            for (int i = 0; i < ITERATIONS; i++) {
                r = bsetiLongBit32(r);
                r = bclriLongBit33(r);
                r = binviLongBit62(r);
                r = bsetiLongBit63(r);
            }
            if (r == bsetiLongBit1(r)) { r ^= 1L; }
        }

        System.out.println("PASSED: All Zbs single-bit tests.");
    }

    static void checkInt(String name, int actual, int expected) {
        if (actual != expected) {
            throw new Error("FAIL: " + name
                + " expected 0x" + Integer.toHexString(expected)
                + " but got 0x" + Integer.toHexString(actual));
        }
    }

    static void checkLong(String name, long actual, long expected) {
        if (actual != expected) {
            throw new Error("FAIL: " + name
                + " expected 0x" + Long.toHexString(expected)
                + " but got 0x" + Long.toHexString(actual));
        }
    }
}
