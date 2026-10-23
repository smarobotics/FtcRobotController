package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.dfrobot.HuskyLens;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.robotcore.external.Telemetry;

@TeleOp(name = "Husky")
public class HuskyLensAprilTagTeleOp extends LinearOpMode {

    private HuskyLens huskyLens;

    @Override
    public void runOpMode() {
        // Initialize the HuskyLens hardware mapping
        // Ensure this matches the name in your Driver Station configuration exactly
        huskyLens = hardwareMap.get(HuskyLens.class, "huskylens");

        telemetry.addData("Status", "Initializing...");
        telemetry.update();

        /*
         * Optional: Set the HuskyLens algorithm mode via code.
         * Note: It is often more reliable to set this manually to "Tag Recognition"
         * using the physical dial on the back of the device before running.
         */
        if (!huskyLens.selectAlgorithm(HuskyLens.Algorithm.TAG_RECOGNITION)) {
            telemetry.addData("HuskyLens Error", "Failed to select Tag Recognition mode!");
            telemetry.update();
        }

        telemetry.addData("Status", "Initialized. Press Play.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // Request the list of currently visible recognized blocks/tags
            HuskyLens.Block[] blocks = huskyLens.blocks();

            telemetry.addData(">>", "Point camera at an AprilTag");
            telemetry.addData("Tags Detected", blocks.length);

            // Loop through all detected targets and push data to telemetry
            for (int i = 0; i < blocks.length; i++) {
                HuskyLens.Block block = blocks[i];

                telemetry.addLine("\n--- Detected Tag [" + i + "] ---");
                telemetry.addData("AprilTag ID", block.id);
                telemetry.addData("Center X coordinate", block.x); // Range: 0 to 320
                telemetry.addData("Center Y coordinate", block.y); // Range: 0 to 240
                telemetry.addData("Tag Width", block.width);
                telemetry.addData("Tag Height", block.height);
            }

            // Simple heartbeat to prove the loop is updating fluidly
            telemetry.addData("\nLoop Timestamp", System.currentTimeMillis());
            telemetry.update();

            // Small pause to prevent choking the I2C bus traffic
            sleep(50);
        }
    }
}

