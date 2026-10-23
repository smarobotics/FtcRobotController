package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.dfrobot.HuskyLens;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import org.firstinspires.ftc.robotcore.internal.system.Deadline;
import java.util.concurrent.TimeUnit;

@Autonomous(name = "Husky", group = "Sensor")
public class Husky extends LinearOpMode {

    // Define the rate limit for reading data to keep telemetry from lagging
    private final int READ_PERIOD = 1;
    private HuskyLens huskyLens;

    @Override
    public void runOpMode() {
        // Initialize HuskyLens using your exact hardware map name
        huskyLens = hardwareMap.get(HuskyLens.class, "huskyLens");
        motor0 = hardwareMap.get(DcMotor.class, "motor0");

        /*
         * Set up a rate limiter so we don't spam the I2C bus too quickly.
         * This forces the code to wait 1 second between reads solely for human visibility.
         */
        Deadline rateLimit = new Deadline(READ_PERIOD, TimeUnit.SECONDS);
        rateLimit.expire(); // Force immediate execution on the first loop pass

        /*
         * Select your starting algorithm here. Common examples:
         * HuskyLens.Algorithm.COLOR_RECOGNITION
         * HuskyLens.Algorithm.TAG_RECOGNITION
         * HuskyLens.Algorithm.OBJECT_RECOGNITION
         */
        huskyLens.selectAlgorithm(HuskyLens.Algorithm.COLOR_RECOGNITION);

        telemetry.addData("Status", "Initialized. HuskyLens ready.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            if (!rateLimit.hasExpired()) {
                continue; // Skip reading if our 1-second deadline hasn't elapsed
            }
            rateLimit.reset(); // Reset the timer for the next read

            telemetry.addLine("--- HuskyLens Readings ---");

            // Request the blocks array from the HuskyLens
            HuskyLens.Block[] blocks = huskyLens.blocks();
            telemetry.addData("Blocks detected", blocks.length);

            /*
             * Loop through each detected block.
             * For color recognition, the 'id' field matches the trained color ID.
             * 'x' is the center horizontal position (ranges from 0 to 320 pixels).
             */
            for (int i = 0; i < blocks.length; i++) {
                // Check if the block is the target AprilTag (assuming ID 1).
                // Replace '1' with the actual ID of your AprilTag if necessary.
                if (blocks[i].id == 1) {
                    motor0.setDirection(DcMotor.Direction.FORWARD);
                    motor0.setPower(1.0); // Set power to full forward
                    telemetry.addData("Motor 0 Status", "Moving Forward (AprilTag Detected)");
                } else {
                    // Stop the motor if the current block is not the target AprilTag
                    motor0.setPower(0.0);
                    telemetry.addData("Motor 0 Status", "Stopped");
                }

                telemetry.addData("Block " + i, blocks[i].toString());
                telemetry.addData("  ID", blocks[i].id);
                telemetry.addData("  Center X", blocks[i].x);
                telemetry.addData("  Center Y", blocks[i].y);
                telemetry.addData("  Width", blocks[i].width);
                telemetry.addData("  Height", blocks[i].height);
            }

            telemetry.update();
        }
    }
}
