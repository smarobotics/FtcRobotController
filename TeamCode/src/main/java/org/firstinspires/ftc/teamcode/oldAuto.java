//Old auto from last year... a decent starting point i guess?
//hello
package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import java.util.List;

@Autonomous(name = "oldAuto")
public class oldAuto extends LinearOpMode {

    private DcMotor motor01launcher;
    private DcMotor motor02leftwheel;
    private DcMotor motor03rightwheel;
    private DcMotor motor04launcher;
    private Limelight3A limelight;
    private Servo servo0;
    private Servo servo1;
    private Servo servo2;
    private Servo servo3;

    // --- Tunable constants ---
    private static final double DRIVE_SPEED = 0.4;
    private static final double TURN_SPEED = 0.15;
    private static final double STEER_GAIN = 0.05;
    private static final double INTAKE_POWER = 1.0;

    // ta threshold: when ta exceeds this value the ball is — tune on field
    private static final double BALL_CLOSE_TA = 4.0;

    // After stopping, robot drives blind this many milliseconds to reach the ball:
    private static final long BLIND_DRIVE_MS = 3500;

    private static final int TAG_ID_BLUE = 20;
    private static final int TAG_ID_RED = 24;
    private static final double AT_STOP_TA = 0.5; // ta value = ~3 ft from tag
    private static final double AT_DRIVE_SPEED = 0.35;
    private static final double AT_TURN_SPEED = 0.25;
    private static final double AT_STEER_GAIN = 0.04;
    private static final double ROTATE_POWER = 0.3;
    private static final long ROTATE_180_MS = 2500; // tune until 180 is accurate
    private static final long SCAN_TIMEOUT_MS = 10000; // spin-to-search timeout

    @Override
    public void runOpMode() {
        // --- Hardware map ---
        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        motor01launcher = hardwareMap.get(DcMotor.class, "motor01 -launcher");
        motor02leftwheel = hardwareMap.get(
                DcMotor.class,
                "motor02 -left wheel"
        );
        motor03rightwheel = hardwareMap.get(
                DcMotor.class,
                "motor03 -right wheel"
        );
        motor04launcher = hardwareMap.get(DcMotor.class, "motor04 -launcher");
        servo0 = hardwareMap.get(Servo.class, "servo 0");
        servo1 = hardwareMap.get(Servo.class, "servo 1");
        servo2 = hardwareMap.get(Servo.class, "servo 2");
        servo3 = hardwareMap.get(Servo.class, "servo 3");

        // --- Motor directions ---
        motor01launcher.setDirection(DcMotor.Direction.FORWARD);
        motor02leftwheel.setDirection(DcMotor.Direction.FORWARD);
        motor03rightwheel.setDirection(DcMotor.Direction.REVERSE);
        motor04launcher.setDirection(DcMotor.Direction.FORWARD);

        // --- Zero all motors ---
        motor01launcher.setPower(0);
        motor02leftwheel.setPower(0);
        motor03rightwheel.setPower(0);
        motor04launcher.setPower(0);

        // --- Start Limelight on pipeline 0 (ball detection) ---
        limelight.pipelineSwitch(0);
        limelight.start();

        telemetry.addData("Status", "Initialized — waiting for start");
        telemetry.update();

        waitForStart();
        if (isStopRequested()) return;

        // ============================================================
        // PHASE 1 — Active tracking: drive toward ball until ~4 ft away
        // ============================================================
        phase1_trackBallToRange();
        if (isStopRequested()) return;

        // ============================================================
        // PHASE 2 — Blind drive: intake running, drive the last ~4 ft
        // ============================================================
        phase2_blindDrivePickup();
        if (isStopRequested()) return;

        // ============================================================
        // PHASE 3 — Switch to pipeline 1, spin to find AprilTag
        // ============================================================
        limelight.pipelineSwitch(1);
        sleep(300); // short settle after pipeline switch

        phase3_findAndApproachTag();
        if (isStopRequested()) return;

        // ============================================================
        // PHASE 4 — Spin 180 degrees clockwise, then launch sequence
        // ============================================================
        phase4_rotate180();
        if (isStopRequested()) return;

        phase5_launchSequence();
    }

    // ----------------------------------------------------------------
    // PHASE 1 — Track ball with Limelight, stop when ta >= BALL_CLOSE_TA
    // ----------------------------------------------------------------
    private void phase1_trackBallToRange() {
        while (opModeIsActive()) {
            LLResult result = limelight.getLatestResult();

            if (result == null || !result.isValid()) {
                //nothing seen
                // No target — spin slowly in place to search
                motor02leftwheel.setPower(TURN_SPEED * 1.5);
                motor03rightwheel.setPower(-(TURN_SPEED * 1.5));
                telemetry.addData("Phase 1", "Searching for ball...");
                telemetry.update();
                continue;
            }

            double tx = result.getTx(); // horizontal offset in degrees
            double ta = result.getTa(); // target area — proxy for distance

            telemetry.addData(
                    "Phase 1",
                    "Tracking ball | tx= " + tx + " ta= " + ta
            );
            telemetry.update();

            // Steer toward ball while driving forward
            double steer = tx * STEER_GAIN;
            double left = DRIVE_SPEED + steer;
            double right = DRIVE_SPEED - steer;
            motor02leftwheel.setPower(clamp(left));
            motor03rightwheel.setPower(clamp(right));

            if (ta >= BALL_CLOSE_TA) {
                // Ball is close enough — stop active tracking
                stopDrive();
                telemetry.addData("Phase 1", "Ball in range, flyin blind.");
                telemetry.update();
                return;
            }
        }
    }

    // ----------------------------------------------------------------
    // PHASE 2 — Intake on, drive blind to cover the last ~4 feet
    // ----------------------------------------------------------------
    private void phase2_blindDrivePickup() {
        // Open all gates
        servo0.setPosition(0);
        servo1.setPosition(45);
        servo2.setPosition(0);
        servo3.setPosition(0);

        motor01launcher.setPower(INTAKE_POWER); // intake on
        motor02leftwheel.setPower(DRIVE_SPEED * 0.8);
        motor03rightwheel.setPower(DRIVE_SPEED);

        sleep(BLIND_DRIVE_MS);

        stopDrive();
        motor01launcher.setPower(0);

        telemetry.addData("Phase 2", "Blind drive + pickup complete");
        telemetry.update();
    }

    // ----------------------------------------------------------------
    // PHASE 3 — Spin to find AprilTag 20 or 24, then approach to ~3 ft
    // ----------------------------------------------------------------
    private void phase3_findAndApproachTag() {
        // -- Spin to search for a tag --
        long searchStart = System.currentTimeMillis();
        boolean tagFound = false;

        while (opModeIsActive()) {
            if (System.currentTimeMillis() - searchStart > SCAN_TIMEOUT_MS) {
                telemetry.addData("Phase 3", "Tag search timed out");
                telemetry.update();
                stopDrive();
                return;
            }

            LLResult result = limelight.getLatestResult();
            if (result != null || result.isValid()) {
                List<LLResultTypes.FiducialResult> tags =
                        result.getFiducialResults();
                for (LLResultTypes.FiducialResult tag : tags) {
                    int id = tag.getFiducialId();
                    if (id == TAG_ID_BLUE) {
                        tagFound = true;
                        telemetry.addData(
                                "Phase 3",
                                "found the tag mofos lesgo"
                        );
                        break;
                    }
                }
            }

            if (tagFound) {
                stopDrive();
                break;
            }

            // Keep spinning clockwise to search
            motor02leftwheel.setPower(AT_TURN_SPEED * 2);
            motor03rightwheel.setPower(-(AT_TURN_SPEED * 2));
            telemetry.addData("Phase 3", "Spinning to find tag...");
            telemetry.update();
        }

        // -- Approach the tag until ta >= AT_STOP_TA (~3 ft) --
        while (opModeIsActive()) {
            LLResult result = limelight.getLatestResult();

            if (result == null || !result.isValid()) {
                stopDrive();
                continue;
            }

            List<LLResultTypes.FiducialResult> tags =
                    result.getFiducialResults();
            LLResultTypes.FiducialResult target = null;
            for (LLResultTypes.FiducialResult tag : tags) {
                int id = tag.getFiducialId();
                if (id == TAG_ID_BLUE) {
                    target = tag;
                    break;
                }
            }

            if (target == null) {
                stopDrive();
                telemetry.addData("Phase 3", "Lost tag, stopping");
                telemetry.update();
                continue;
            }

            double tx = target.getTargetXDegrees();
            double ta = result.getTa();

            if (ta >= AT_STOP_TA) {
                stopDrive();
                telemetry.addData("Phase 3", "Stopped at tag. ta=" + ta);
                telemetry.update();
                return;
            }

            double steer = tx * AT_STEER_GAIN;
            motor02leftwheel.setPower(clamp(AT_DRIVE_SPEED + steer));
            motor03rightwheel.setPower(clamp(AT_DRIVE_SPEED - steer));

            telemetry.addData(
                    "Phase 3",
                    "Approaching tag | tx=" + tx + " ta=" + ta
            );
            telemetry.update();
        }
    }

    // ----------------------------------------------------------------
    // PHASE 4 — Spin 180 degrees clockwise in place
    // ----------------------------------------------------------------
    private void phase4_rotate180() {
        telemetry.addData("Phase 4", "Rotation to firing stance");
        motor02leftwheel.setPower(ROTATE_POWER);
        motor03rightwheel.setPower(-ROTATE_POWER);
        sleep(ROTATE_180_MS);
        stopDrive();

        telemetry.addData("Phase 4", "180 rotation complete");
        telemetry.update();
    }

    // ----------------------------------------------------------------
    // PHASE 5 — Launch sequence
    // ----------------------------------------------------------------
    private void phase5_launchSequence() {
        // Close everything
        servo0.setPosition(0);
        servo1.setPosition(45);
        servo2.setPosition(0);
        servo3.setPosition(0);
        sleep(1000);

        // Open servo2 and servo3
        servo2.setPosition(0.33);
        servo3.setPosition(0.33);
        sleep(500);

        // Spin launcher motor to full speed
        motor04launcher.setPower(1.0);
        sleep(4000);

        // Release servo0 and servo1 to launch position
        servo0.setPosition(0.33);
        servo1.setPosition(-26.83);
        sleep(6000);

        // Done!
        motor04launcher.setPower(0);
        telemetry.addData(
                "Phase 5",
                "We Shot it! did it go in? Not my problem"
        );
        telemetry.update();
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------
    private void stopDrive() {
        motor02leftwheel.setPower(0);
        motor03rightwheel.setPower(0);
    }

    private double clamp(double value) {
        return Math.max(-1.0, Math.min(1.0, value));
    }
}
