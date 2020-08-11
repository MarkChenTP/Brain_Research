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