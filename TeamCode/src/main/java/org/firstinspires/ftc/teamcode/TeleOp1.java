package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name = "TeleOp1.java")
public class TeleOp1 extends LinearOpMode {

    private DcMotor motor01launcher;
    private DcMotor motor04launcher;
    private DcMotor motor02leftwheel;
    private DcMotor motor03rightwheel;
    private DcMotor motor05rand;
    private Servo servo0;

    @Override
    public void runOpMode() {
        motor01launcher = hardwareMap.get(DcMotor.class, "motor00");
        motor04launcher = hardwareMap.get(DcMotor.class, "motor03");
        motor02leftwheel = hardwareMap.get(DcMotor.class, "motor01");
        motor03rightwheel = hardwareMap.get(DcMotor.class, "motor02");
        motor05rand = hardwareMap.get(DcMotor.class, "motor05");
        servo0 = hardwareMap.get(Servo.class, "servo 0");
        waitForStart();
        while (opModeIsActive()) {
            //
            motor01launcher.setDirection(DcMotor.Direction.REVERSE);
            motor04launcher.setDirection(DcMotor.Direction.REVERSE);
            motor03rightwheel.setDirection(DcMotor.Direction.FORWARD);
            motor02leftwheel.setDirection(DcMotor.Direction.FORWARD);
            //
            motor02leftwheel.setPower(gamepad1.right_stick_x);
            motor03rightwheel.setPower(gamepad1.left_stick_x);
            //
            if (gamepad1.left_trigger > 0) {
                motor01launcher.setDirection(DcMotor.Direction.FORWARD);
                motor01launcher.setPower(gamepad1.left_trigger);
            }
            // motor01launcher.setPower(gamepad1.left_trigger);
            motor01launcher.setPower(gamepad1.right_trigger);
            if (gamepad1.dpad_up) {
                servo0.setPosition(0);
            }

        }

    }
}