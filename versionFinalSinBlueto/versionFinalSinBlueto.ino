#include <Servo.h>
#include <SoftwareSerial.h>
SoftwareSerial mySerial(7, 8); // RX, TX 

#define abs(x) ((x) > 0 ? (x) : -(x))
#define SERVO_PIN 11
#define SERVO_BUTTON_MODE_PIN 2
#define LEFT_TRIGGER_PIN 12
#define RIGHT_TRIGGER_PIN 13
#define RIGHT_ECHO_PIN 9
#define LEFT_ECHO_PIN 10
#define INICIAL_ANGLE 1500
#define MAX_LEFT_ANGLE 2500
#define MAX_RIGHT_ANGLE 600
#define ANGLE_CHANGE 20
#define ANGLE_CHANGE_REDUX ANGLE_CHANGE*0.3
#define DIFFERENCE_DONT_CARE 5
#define CONST_TIME_TO_DISTANCE 58.2
#define CONST_TIME_TO_DISTANCE2 0.017
#define MAX_DISTANCE_TO_ANALYZE 100
#define PIN_NEOPIXEL A3
#define AMOUNT_LIGHTS 6
#define PHOTORESISTOR_MAX_VALUE 290
#define PHOTORESISTOR A0
#define LED_BUTTON_MODE_PIN 8

//----------------------------------------------
// States
#define STATE_INFLUENCER_AUTO_LIGHTS 100
#define STATE_INFLUENCER_ON 200
#define STATE_INFLUENCER_OFF 210

#define STATE_MANUAL_AUTO_LIGHTS 300
#define STATE_MANUAL_ON 400
#define STATE_MANUAL_OFF 410

#define STATE_CAM_AUTO_LIGHTS 500
#define STATE_CAM_ON 600
#define STATE_CAM_OFF 610
//----------------------------------------------

// Events
#define CHANGE_SERVO_MODE 101
#define CHANGE_LIGHTS_MODE 202
#define MOVE_SERVO_LEFT 303
#define MOVE_SERVO_RIGHT 404
#define CONTINUE 505
//----------------------------------------------

// Commands
/*
    L: Move Left
    R: Move Right
    S: Cycle Servo Mode 
    Z: Cycle Light Mode

    Commands have to be one char, because of Serial.read()
*/
//----------------------------------------------
Servo servo;

struct stSwitch
{
    int current_state;
    int previous_state;
};


int angle_in_microsec = INICIAL_ANGLE;
int left_distance;
int right_distance;

int state;

int brightness;
int turn_led;
int led_mode_manual;
int left_button;
int right_button;
bool cmp_angle_left;
bool cmp_angle_right;

int event;
int band;

void setup()
{
    Serial.begin(9600);

    servo.attach(SERVO_PIN);
    servo.writeMicroseconds(angle_in_microsec);

	pinMode(SERVO_BUTTON_MODE_PIN, INPUT);

    pinMode(LEFT_TRIGGER_PIN, OUTPUT);
    pinMode(RIGHT_TRIGGER_PIN, OUTPUT);
    pinMode(RIGHT_ECHO_PIN, INPUT);
    pinMode(LEFT_ECHO_PIN, INPUT);
	
    pinMode(PHOTORESISTOR, INPUT);
    pinMode(PIN_NEOPIXEL, OUTPUT);
	pinMode(LED_BUTTON_MODE_PIN, INPUT);
	

    // set embeded initial state
    state = STATE_INFLUENCER_AUTO_LIGHTS;
    band = 0;
    mySerial.begin(9600);  
}

void loop()
{
    FSM();
}

void FSM()
{
    event = get_event();
    switch (state)
    {
	case STATE_INFLUENCER_AUTO_LIGHTS:
        switch (event)
        {
        case CHANGE_SERVO_MODE:
            Serial.println("STATE_INFLUENCER_AUTO_LIGHTS -> STATE_MANUAL_AUTO_LIGHTS");
            state = STATE_MANUAL_AUTO_LIGHTS;
            break;
        case CHANGE_LIGHTS_MODE:
            Serial.println("STATE_INFLUENCER_AUTO_LIGHTS -> STATE_INFLUENCER_ON");
            state = STATE_INFLUENCER_ON;
			turn_lights_on();
      break;
        case CONTINUE:
            automatic_trace_mode_servo();
            automatic_led_room_brightness();
            break;
        }

        break;
    case STATE_MANUAL_AUTO_LIGHTS:
        switch (event)
        {
        case CHANGE_SERVO_MODE:
            Serial.println("STATE_MANUAL_AUTO_LIGHTS -> STATE_CAM_AUTO_LIGHTS");
            state = STATE_CAM_AUTO_LIGHTS;
            break;
        case CHANGE_LIGHTS_MODE:
            Serial.println("STATE_MANUAL_AUTO_LIGHTS -> STATE_MANUAL_ON");
            state = STATE_MANUAL_ON;
			turn_lights_on();
            break;
		case MOVE_SERVO_LEFT:
            angle_in_microsec += ANGLE_CHANGE;
			servo.writeMicroseconds(angle_in_microsec);
            break;
		case MOVE_SERVO_RIGHT:
            angle_in_microsec -= ANGLE_CHANGE;
			servo.writeMicroseconds(angle_in_microsec);
            break;
        case CONTINUE:
            automatic_led_room_brightness();
            break;
        }

        break;
	case STATE_CAM_AUTO_LIGHTS:
        switch (event)
        {
        case CHANGE_SERVO_MODE:
            Serial.println("STATE_CAM_AUTO_LIGHTS -> STATE_INFLUENCER_AUTO_LIGHTS");
            state = STATE_INFLUENCER_AUTO_LIGHTS;
            break;
        case CHANGE_LIGHTS_MODE:
            Serial.println("STATE_CAM_AUTO_LIGHTS -> STATE_CAM_ON");
            state = STATE_CAM_ON;
			turn_lights_on();
            break;
        case CONTINUE:
            automatic_mode_servo();
            automatic_led_room_brightness();
            break;
        }
		
        break;
		
	case STATE_INFLUENCER_ON:
        switch (event)
        {
        case CHANGE_SERVO_MODE:
            Serial.println("STATE_INFLUENCER_ON -> STATE_MANUAL_ON");
            state = STATE_MANUAL_ON;
            break;
        case CHANGE_LIGHTS_MODE:
            Serial.println("STATE_INFLUENCER_ON -> STATE_INFLUENCER_OFF");
            state = STATE_INFLUENCER_OFF;
			turn_lights_off();
            break;
        case CONTINUE:
            automatic_trace_mode_servo();
            break;
        }

        break;	
    case STATE_MANUAL_ON:
        switch (event)
        {
        case CHANGE_SERVO_MODE:
            Serial.println("STATE_MANUAL_ON -> STATE_CAM_ON");
            state = STATE_CAM_ON;
            break;
        case CHANGE_LIGHTS_MODE:
            Serial.println("STATE_MANUAL_ON -> STATE_MANUAL_OFF");
            state = STATE_MANUAL_OFF;
			turn_lights_off();
            break;
		case MOVE_SERVO_LEFT:
            angle_in_microsec += ANGLE_CHANGE;
			servo.writeMicroseconds(angle_in_microsec);
            break;
		case MOVE_SERVO_RIGHT:
            angle_in_microsec -= ANGLE_CHANGE;
			servo.writeMicroseconds(angle_in_microsec);
            break;
        case CONTINUE:
            break;
        }

        break;
	case STATE_CAM_ON:
        switch (event)
        {
        case CHANGE_SERVO_MODE:
            Serial.println("STATE_CAM_ON -> STATE_INFLUENCER_ON");
            state = STATE_INFLUENCER_ON;
            break;
        case CHANGE_LIGHTS_MODE:
            Serial.println("STATE_CAM_ON -> STATE_CAM_OFF");
            state = STATE_CAM_OFF;
			turn_lights_off();
            break;

        case CONTINUE:
            automatic_mode_servo();
            break;
        }

        break;
		
	case STATE_INFLUENCER_OFF:
        switch (event)
        {
        case CHANGE_SERVO_MODE:
            Serial.println("STATE_INFLUENCER_OFF -> STATE_MANUAL_OFF");
            state = STATE_MANUAL_OFF;
            break;
        case CHANGE_LIGHTS_MODE:
            Serial.println("STATE_INFLUENCER_OFF -> STATE_INFLUENCER_AUTO_LIGHTS");
            state = STATE_INFLUENCER_AUTO_LIGHTS;
            break;
        case CONTINUE:
            automatic_trace_mode_servo();
            break;
        }

        break;	
    case STATE_MANUAL_OFF:
        switch (event)
        {
        case CHANGE_SERVO_MODE:
            Serial.println("STATE_MANUAL_OFF -> STATE_CAM_OFF");
            state = STATE_CAM_OFF;
            break;
        case CHANGE_LIGHTS_MODE:
            Serial.println("STATE_MANUAL_OFF -> STATE_MANUAL_AUTO_LIGHTS");
            state = STATE_MANUAL_AUTO_LIGHTS;
            break;
		case MOVE_SERVO_LEFT:
            angle_in_microsec += ANGLE_CHANGE;
			servo.writeMicroseconds(angle_in_microsec);
            break;
		case MOVE_SERVO_RIGHT:
            angle_in_microsec -= ANGLE_CHANGE;
			servo.writeMicroseconds(angle_in_microsec);
            break;
        case CONTINUE:
            break;
        }

        break;
	case STATE_CAM_OFF:
        switch (event)
        {
        case CHANGE_SERVO_MODE:
            Serial.println("STATE_CAM_OFF -> STATE_INFLUENCER_OFF");
            state = STATE_INFLUENCER_OFF;
            break;
        case CHANGE_LIGHTS_MODE:
            Serial.println("STATE_CAM_OFF -> STATE_CAM_AUTO_LIGHTS");
            state = STATE_CAM_AUTO_LIGHTS;
            break;

        case CONTINUE:
            automatic_mode_servo();
            break;
        }

        break;

    
    }
}

int get_event()
{
    int input;
    int serial_input=0;
    digitalWrite(LEFT_TRIGGER_PIN, LOW);
    delayMicroseconds(5);

    digitalWrite(LEFT_TRIGGER_PIN, HIGH); 
    delayMicroseconds(10);
    digitalWrite(LEFT_TRIGGER_PIN, LOW);
    left_distance = pulseIn(LEFT_ECHO_PIN, HIGH)  / CONST_TIME_TO_DISTANCE;;

    //Serial.println(left_distance);

    digitalWrite(RIGHT_TRIGGER_PIN, LOW);
    delayMicroseconds(5);

    digitalWrite(RIGHT_TRIGGER_PIN, HIGH);
    delayMicroseconds(10);
    digitalWrite(RIGHT_TRIGGER_PIN, LOW);
    right_distance = pulseIn(RIGHT_ECHO_PIN, HIGH) / CONST_TIME_TO_DISTANCE;

     //Serial.println(right_distance);

    brightness = analogRead(PHOTORESISTOR);

    //serial_input = Serial.read();
    //mySerial.println("estoy");
    if (mySerial.available())
    {
      serial_input = mySerial.read();
      
      //Serial.write(serial_input);
      mySerial.println("Recibi");
    }      

    cmp_angle_left = (angle_in_microsec <= MAX_LEFT_ANGLE);
    cmp_angle_right = (angle_in_microsec > MAX_RIGHT_ANGLE);

    if (serial_input == 'S')
		return CHANGE_SERVO_MODE;

    if (serial_input == 'Z')
        return CHANGE_LIGHTS_MODE;
	
	if (serial_input == 'L')
		return MOVE_SERVO_LEFT;
	
	if (serial_input == 'R')
		return MOVE_SERVO_RIGHT;
    return CONTINUE;
}


void automatic_trace_mode_servo()
{
    if (left_distance < MAX_DISTANCE_TO_ANALYZE && right_distance < MAX_DISTANCE_TO_ANALYZE)
    {
        if ( angle_in_microsec  <= MAX_LEFT_ANGLE && left_distance - right_distance > DIFFERENCE_DONT_CARE)
        {
            angle_in_microsec += ANGLE_CHANGE_REDUX;
            
            servo.writeMicroseconds(angle_in_microsec);
        }
        else if (angle_in_microsec > MAX_RIGHT_ANGLE && right_distance - left_distance > DIFFERENCE_DONT_CARE)
        {
            angle_in_microsec -= ANGLE_CHANGE_REDUX;
            servo.writeMicroseconds(angle_in_microsec);
        }
    }
}

void automatic_mode_servo()
{
    if (angle_in_microsec <= MAX_LEFT_ANGLE) // 0
    {
        angle_in_microsec += ANGLE_CHANGE;
        servo.writeMicroseconds(angle_in_microsec);
    }
    else
    {
        angle_in_microsec = MAX_RIGHT_ANGLE;
        servo.writeMicroseconds(angle_in_microsec);
    }
}

void automatic_led_room_brightness()
{
    if (brightness > PHOTORESISTOR_MAX_VALUE)
      digitalWrite(PIN_NEOPIXEL,HIGH);
    else 
     digitalWrite(PIN_NEOPIXEL,LOW);

}
void turn_lights_on()
{
	Serial.println("turn_lights_on");
  digitalWrite(PIN_NEOPIXEL,HIGH);
}
void turn_lights_off()
{
	Serial.println("turn_lights_off");
	 digitalWrite(PIN_NEOPIXEL,LOW);
}
