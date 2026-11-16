package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name = "TeleOp2.java")
public class TeleOp2 extends LinearOpMode {

    private DcMotor intake;
    private DcMotor launcher;
    private DcMotor leftRearWheel;
    private DcMotor rightRearWheel;
    private DcMotor leftFrontWheel;
    private DcMotor rightFrontWheel;

    @Override
    public void runOpMode() {
        leftFrontWheel = hardwareMap.get(DcMotor.class, "motor00");
        leftRearWheel = hardwareMap.get(DcMotor.class, "motor01");
        rightRearWheel = hardwareMap.get(DcMotor.class, "motor02");
        rightFrontWheel = hardwareMap.get(DcMotor.class, "motor03");
        intake = hardwareMap.get(DcMotor.class, "motor04");
        launcher = hardwareMap.get(DcMotor.class, "motor05");
        //
        intake.setDirection(DcMotor.Direction.REVERSE);
        rightRearWheel.setDirection(DcMotor.Direction.FORWARD);
        leftRearWheel.setDirection(DcMotor.Direction.FORWARD);
        rightFrontWheel.setDirection(DcMotor.Direction.FORWARD);
        leftFrontWheel.setDirection(DcMotor.Direction.FORWARD);
        //
        waitForStart();
        while (opModeIsActive()) {
            leftRearWheel.setPower(gamepad1.right_stick_x);
            leftFrontWheel.setPower(gamepad1.right_stick_x);
            rightRearWheel.setPower(gamepad1.left_stick_x);
            rightFrontWheel.setPower(gamepad1.left_stick_x);
            //if (gamepad2.circle) {
            //    intake.setPower(1.0);
            //}
            //if (gamepad2.square) {
            //    intake.setPower(-1.0);
            //}
            //if (gamepad2.cross) {
            //    intake.setPower(0.0);
            //}
            if (gamepad2.left_trigger > 0) {
                intake.setPower(gamepad2.left_trigger);
            }
            else if (gamepad2.right_trigger > 0) {
                intake.setPower(-gamepad2.right_trigger);
            }
            else {
                intake.setPower(0.0);
            }
        }

    }
}