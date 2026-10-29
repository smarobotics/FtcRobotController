package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.dfrobot.HuskyLens;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import org.firstinspires.ftc.robotcore.internal.system.Deadline;
import java.util.concurrent.TimeUnit;
import java.util.Locale;

@Autonomous(name = "Husky2_TagApproach", group = "Sensor")
public class Husky2 extends LinearOpMode {

    // Define the rate limit for reading data
    private final int READ_PERIOD = 1;
    private HuskyLens huskyLens;
    private DcMotor motor0;

    // --- Tunable constants (from oldAuto.java) ---
    private static final double AT_DRIVE_SPEED = 0.35; // Slow approach speed
    private static final double AT_STEER_GAIN = 0.04;  // How aggressively to steer
    private static final double AT_STOP_TA = 0.5;     // Target Area threshold (stop when TA >= 0.5)
    private static final int TARGET_TAG_ID = 1;       // The ID of the AprilTag we are tracking

    @Override
    public void runOpMode() {
        // Initialize HuskyLens and Motor
        huskyLens = hardwareMap.get(HuskyLens.class, "huskyLens");
        motor0 = hardwareMap.get(DcMotor.class, "motor0");

        // Set up rate limiter
        Deadline rateLimit = new Deadline(READ_PERIOD, TimeUnit.SECONDS);
        rateLimit.expire(); // Force immediate execution on the first loop pass

        // Select Tag Recognition algorithm
        huskyLens.selectAlgorithm(HuskyLens.Algorithm.TAG_RECOGNITION);

        telemetry.addData("Status", "Initialized. HuskyLens ready for Tag Approach.");
        telemetry.update();

        waitForStart();

        // Zero all motors
        motor0.setPower(0.0);

        while (opModeIsActive()) {
            if (!rateLimit.hasExpired()) {
                continue; // Skip reading if our 1-second deadline hasn't elapsed
            }
            rateLimit.reset(); // Reset the timer for the next read

            telemetry.addLine("--- Husky2 Tag Approach Readings ---");

            // Request the blocks array from the HuskyLens
            HuskyLens.Block[] blocks = huskyLens.blocks();
            telemetry.addData("Blocks detected", blocks.length);

            // Variables to hold the target data
            double targetTa = 0.0; // Target Area (distance proxy)
            double targetTx = 0.0; // Target X (horizontal offset)
            boolean tagFound = false;

            // Loop through each detected block to find the target tag (ID 1)
            for (int i = 0; i < blocks.length; i++) {
                if (blocks[i].id == TARGET_TAG_ID) {
                    tagFound = true;
                    // Use the data from the target tag
                    targetTa = blocks[i].width;
                    targetTx = blocks[i].x;
                    break; // Found the target, no need to check others
                }
            }

            // --- Movement Logic ---
            if (tagFound) {
                // 1. Check if the tag is close enough (TA >= AT_STOP_TA)
                if (targetTa >= AT_STOP_TA) {
                    // Stop the motor when aligned/close enough
                    motor0.setPower(0.0);
                    telemetry.addData("Movement Status", "STOPPED (Tag in Range)");
                } else {
                    // 2. Calculate steering and drive speed (Approach)
                    
                    // Calculate steering based on horizontal offset (TX)
                    double steer = targetTx * AT_STEER_GAIN;
                    
                    // Calculate left and right power
                    double leftPower = AT_DRIVE_SPEED + steer;
                    double rightPower = AT_DRIVE_SPEED - steer;

                    // Set motor power (using clamp to ensure power stays between -1.0 and 1.0)
                    motor0.setPower(clamp(leftPower));
                    
                    telemetry.addData("Movement Status", "APPROACHING (Moving Slowly)");
                    telemetry.addData("Target TA", String.format(Locale.US, "%.2f", targetTa));
                    telemetry.addData("Target TX", String.format(Locale.US, "%.2f", targetTx));
                    telemetry.addData("Motor Power", String.format(Locale.US, "%.2f", motor0.getPower()));
                }
            } else {
                // Tag not found: Stop the motor
                motor0.setPower(0.0);
                telemetry.addData("Movement Status", "STOPPED (Tag Lost)");
            }

            telemetry.update();
        }
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------
    private double clamp(double value) {
        return Math.max(-1.0, Math.min(1.0, value));
    }
}