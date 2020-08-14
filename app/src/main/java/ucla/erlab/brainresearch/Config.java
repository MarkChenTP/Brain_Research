/*
 * Copyright (c) 2020 Mark Chen (@MarkChenTP, https://github.com/MarkChenTP)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/**
 *  Config.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */

package ucla.erlab.brainresearch;

public class Config {

    public enum RestType {
        Independent,           // independent rest
        BeforeValsalva,        // 1st rest of valsalva
        AfterValsalva,         // 2nd rest of valsalva
        BeforeBreathHold,      // 1st rest of breath hold
        AfterBreathHold        // 2nd rest of breath hold
    }

    public enum BPType {
        AfterQuestions,        // blood pressure before standalone Rest
        AfterRest,             // blood pressure after standalone Rest
        AfterValsalva,         // blood pressure after valsalva
        BeforeStressReduce,    // blood pressure after breath hold, PVT, or Stroop
        AfterStressReduce      // blood pressure after stress reduction
    }

    public enum CommType {
        InitReq,
        InitRes,
        TimeSetReq,
        TimeGetReq,
        TimeGetRes,
        DataIn
    }
}