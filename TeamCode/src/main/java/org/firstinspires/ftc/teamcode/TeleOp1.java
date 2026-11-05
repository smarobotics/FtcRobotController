package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name = "TeleOp1.java")
public class TeleOp1 extends LinearOpMode {

    private DcMotor launcher;
    private DcMotor intake;
    private DcMotor leftRearWheel;
    private DcMotor rightRearWheel;
    private DcMotor next;

    @Override
    public void runOpMode() {
        intake = hardwareMap.get(DcMotor.class, "motor00");
        launcher = hardwareMap.get(DcMotor.class, "motor03");
        leftRearWheel = hardwareMap.get(DcMotor.class, "motor01");
        rightRearWheel = hardwareMap.get(DcMotor.class, "motor02");
        next = hardwareMap.get(DcMotor.class, "motor05");

        // motor05rand = hardwareMap.get(DcMotor.class, "motor05");
        //
        intake.setDirection(DcMotor.Direction.REVERSE);
        launcher.setDirection(DcMotor.Direction.REVERSE);
        rightRearWheel.setDirection(DcMotor.Direction.FORWARD);
        leftRearWheel.setDirection(DcMotor.Direction.FORWARD);
        next.setDirection(DcMotor.Direction.FORWARD);
        //
        waitForStart();
        while (opModeIsActive()) {
            leftRearWheel.setPower(gamepad1.right_stick_x);
            rightRearWheel.setPower(gamepad1.left_stick_x);
            if (gamepad1.circle) {
                next.setPower(1.0);
            }
            if (gamepad1.square) {
                next.setPower(-1.0);
            }
            if (gamepad1.cross) {
                next.setPower(0.0);
            }
            if (gamepad1.left_trigger > 0) {
                launcher.setPower(gamepad1.left_trigger);
            }
            else if (gamepad1.right_trigger > 0) {
                launcher.setPower(-gamepad1.right_trigger);
            }
            else {
                launcher.setPower(0.0); // Stop
            }
        }

    }
}