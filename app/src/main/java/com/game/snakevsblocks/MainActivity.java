package com.game.snakevsblocks;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    RelativeLayout gameContainer,upperBox;
    TextView hint, balls;
    View initBall;
    GestureDetector gestureDetector;
    Display display;
    Point size;
    float screenWidth, screenHeight;
    LinearLayout linearLayout;
    View[] tail;
    int tailLength=5, displayLength=5;
    Handler handler;
    boolean gameStarted=false;
    double fallingVelocity=0.4;
    int fallingTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE); // request screen display
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        tail=new View[200];

        handler=new Handler();
        gameContainer=(RelativeLayout)findViewById(R.id.game_container);
        linearLayout=(LinearLayout)findViewById(R.id.linear_layout);
        upperBox=(RelativeLayout)findViewById(R.id.upper_box);
        hint=(TextView)findViewById(R.id.hint);
        balls=(TextView)findViewById(R.id.balls);
        initBall=(View)findViewById(R.id.init_dot);

        display=getWindowManager().getDefaultDisplay();
        size=new Point();
        display.getSize(size);
        screenWidth=size.x;
        screenHeight=size.y;

        fallingTime= (int) (screenHeight/fallingVelocity);


        gestureDetector=new GestureDetector(MainActivity.this, MainActivity.this);


        for (int i=0; i<tailLength; i++) { // initial length of snake
            View newBall = new View(MainActivity.this);
            if(i%2==0)
                newBall.setBackgroundResource(R.drawable.even);
            else
                newBall.setBackgroundResource(R.drawable.odd);
            newBall.setLayoutParams(new LinearLayout.LayoutParams(60, 60));
            tail[i]=newBall;
            linearLayout.addView(tail[i]);
        }




        gameContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) { // starting the game on first touch and calliberating the balls
                float bY=balls.getY();
                float ibY=initBall.getY();


                upperBox.setVisibility(View.INVISIBLE);  // just vanish the heading and hints from screen, so that just snake remains
                hint.setVisibility(View.INVISIBLE);


                balls.setY(bY);
                initBall.setY(ibY);

                if (!gameStarted)
                {
                    gameStarted=true;
                    startGame();
                }

                return false;
            }
        });


    }

    float ibcurrentX, bcurrentX; // the X-axis movements for init_ball(ie the snake) and the ball(ie the number written on head of snake)

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
//
        return gestureDetector.onTouchEvent(motionEvent);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) { // down means finger pressed down on screen and NOT swiping down
//        Toast.makeText(getApplicationContext(), "Down", Toast.LENGTH_SHORT).show();
        ibcurrentX=initBall.getX();
        bcurrentX=balls.getX();

//        Toast.makeText(getApplicationContext(), String.valueOf(initBall.getY()),Toast.LENGTH_SHORT).show();

        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        // motionEvent - when finger pressed down for first time, motionevent1 - when current/scroll generated
        float ibdx= ibcurrentX-motionEvent.getX();
        float bdx= bcurrentX-motionEvent.getX(); // diff b/w initial and final pos of snake head

//        if (Math.abs(motionEvent.getX()-initBall.getX())<50) {
        if (motionEvent1.getX()+ibdx>10 && motionEvent1.getX()+ibdx<screenWidth-100) // 10 pixel margin on left and 100 px margin on right
        {
            final float posX=motionEvent1.getX()+ibdx;

            final float startX=initBall.getX();

            initBall.setX(motionEvent1.getX()+ibdx);
            balls.setX(motionEvent1.getX()+bdx);

            final float endibdx=initBall.getX();

            final float deltaX=Math.abs(endibdx-startX);



            for (int i=0; i<tailLength; i++) // woah. moving the tail later, just the head first
            {
                tail[i].setY(tail[i].getY() - deltaX/5);
            }




                handler.postDelayed(new Runnable() {
                    int i=0;
                    @Override //this is the code which moves the complete snake ball by ball
                    public void run() {
                        tail[i].setX(posX);
                        tail[i].setY(tail[i].getY() + deltaX/5);
//                        try {
////                            tail[i + 1].setY(tail[i + 1].getY() - 30);
//                        }
//                        catch (Exception e)
//                        {
//
//                        }
                        i++;

                        if (i<tailLength)
                        {
                            handler.postDelayed(this, 1);
                        }
                    }
                },1);

        }
//        else {
//
//        }


        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {



        return false;
    }


    public void startGame() // this is the key fn
    {
        Runnable runnable=new Runnable() {//taaki handler use krke, oss code ko running me daal ske theads ke upar like async something like that
            @Override
            public void run() {
                int energyProbability=new Random().nextInt(7); // prob of a powerup appearing
                int wallProbability=new Random().nextInt(9); // prob of walls appearing

                if (energyProbability==2 || energyProbability==5 )//2 4 5 kyu, i think wo bhi random hai, i am waiting for baaki code waise
                {
                    int noOfBalls=new Random().nextInt(3)+1; // 1-3, the no of energy balls at the same level means, means ek hi horizontal level pe for ex 3 powerups, jinme se tu ek hi le skti hai uska random fn to use krlia upar, wo wala random tha ki abb jb snake aage barhega to wall aayegi ya powerup ok

                    for (int i=0; i<noOfBalls; i++) {

                        int energyX=new Random().nextInt((int)screenWidth-110)+10; // randomly positioning the energy balls
//                        int energyY=new Random().nextInt((int)screenHeight*3/4-200);
                        final LinearLayout newEnergyHolder = new LinearLayout(MainActivity.this);
                        final int energyAmount=new Random().nextInt(5)+1; // value of powerup
                        final View newEnergy=new View(MainActivity.this);
                        final TextView energyAmountHolder=new TextView(MainActivity.this);

                        newEnergyHolder.setBackgroundColor(Color.TRANSPARENT);
                        newEnergyHolder.setOrientation(LinearLayout.VERTICAL);

                        energyAmountHolder.setText(String.valueOf(energyAmount)); // the code in all these ines is just displaying that energy balls
                        newEnergy.setBackgroundResource(R.drawable.dot);
                        energyAmountHolder.setTextColor(Color.WHITE);

                        newEnergyHolder.setGravity(Gravity.CENTER);
                        energyAmountHolder.setGravity(Gravity.CENTER);


                        newEnergy.setLayoutParams(new LinearLayout.LayoutParams(48, 48));

                        newEnergyHolder.addView(energyAmountHolder);
                        newEnergyHolder.addView(newEnergy);

//                        newEnergyHolder.setY(-(int)screenWidth/6);
                        newEnergyHolder.setY(-300);
                        newEnergyHolder.setX(energyX);

//                        Toast.makeText(getApplicationContext(), String.valueOf(initBall.getX()),Toast.LENGTH_SHORT).show();
//                        Toast.makeText(getApplicationContext(), "Created",Toast.LENGTH_SHORT).show();


                        gameContainer.addView(newEnergyHolder);
                        newEnergyHolder.animate() // code for animating the background to come down yea igotit
                              .translationY(screenHeight)
                                .setInterpolator(new LinearInterpolator())
//                                .setInterpolator(new BounceInterpolator())
                                .setDuration(fallingTime);

                        handler.postDelayed(new Runnable() { // used when we got to run some functionality after some delay ona thread
                            @Override
                            public void run() {
                                try
                                {
                                    if (initBall.getX()==tail[0].getX()) { // means if the first ball and the second ball are both at same X, then only get the powerup
                                        if (Math.abs(newEnergyHolder.getX() - initBall.getX()) < 65) { // getting the powerup
                                            gameContainer.removeView(newEnergyHolder);

                                       for (int j = 0; j < energyAmount; j++) { // if curr length of snake is <10, increase the length by the amount of powerup
                                           if (tailLength<10) {
                                               View newBall = new View(MainActivity.this);
                                               if(tailLength%2==0)
                                                   newBall.setBackgroundResource(R.drawable.even);
                                               else
                                                   newBall.setBackgroundResource(R.drawable.odd);
                                               newBall.setLayoutParams(new LinearLayout.LayoutParams(60, 60));
                                               tail[tailLength] = newBall;
                                               tail[tailLength].setX(tail[tailLength-1].getX());
                                               linearLayout.addView(tail[tailLength]);
                                               tailLength++;
                                           }
                                           else
                                           {
                                               break;
                                           }
                                       }

                                            displayLength += energyAmount;
                                            balls.setText(String.valueOf(displayLength));
//                                   Toast.makeText(getApplicationContext(), String.valueOf(newEnergy.getY()),Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    else if (initBall.getX()!=tail[0].getX())
                                    {
                                        if (Math.abs(newEnergyHolder.getX() - initBall.getX()) < 100) {
                                            gameContainer.removeView(newEnergyHolder);

                                       for (int j = 0; j < energyAmount; j++) {
                                            if (tailLength<10) {
                                                View newBall = new View(MainActivity.this);
                                                if(tailLength%2==0)
                                                    newBall.setBackgroundResource(R.drawable.even);
                                                else
                                                    newBall.setBackgroundResource(R.drawable.odd);
                                                newBall.setLayoutParams(new LinearLayout.LayoutParams(60, 60));
                                                tail[tailLength] = newBall;
                                                tail[tailLength].setX(tail[tailLength-1].getX());
                                                linearLayout.addView(tail[tailLength]);
                                                tailLength++;
                                            }
                                            else
                                            {
                                                break;
                                            }
                                        }


                                        displayLength += energyAmount;
                                            balls.setText(String.valueOf(displayLength));
//                                   Toast.makeText(getApplicationContext(), String.valueOf(newEnergy.getY()),Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                                catch (Exception e)
                                {

                                }

                            }
                        }, (long)(0.7*fallingTime) );

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                gameContainer.removeView(newEnergyHolder);
                            }
                        }, fallingTime);

                    }

                }
                else if (wallProbability==2 || wallProbability==6)
                {
                    final Button[] tiles=new Button[6];
                    for (int i=0; i<6; i++)
                    {
                        tiles[i] = new Button(MainActivity.this);
                        tiles[i].setLayoutParams(new LinearLayout.LayoutParams((int)screenWidth/6, (int)screenWidth/6));
                        tiles[i].setTextColor(Color.WHITE);
                        tiles[i].setTextSize(25);
                        final int weight= new Random().nextInt(5)+1;
                        if(weight<2)
                            tiles[i].setBackgroundResource(R.drawable.box1);
                        else if(weight<3)
                            tiles[i].setBackgroundResource(R.drawable.box2);
                        else if(weight<4)
                            tiles[i].setBackgroundResource(R.drawable.box3);
                        else
                            tiles[i].setBackgroundResource(R.drawable.box4);
                        tiles[i].setText(Integer.toString(weight));
                        tiles[i].setY(-300);
                        tiles[i].setX(i*screenWidth/6);
                        gameContainer.addView(tiles[i]);
                        tiles[i].animate()
                                .translationY(screenHeight)
                                //.setInterpolator(new AccelerateInterpolator())
                                .setInterpolator(new LinearInterpolator())
                                .setDuration(fallingTime);

                        final int finalI = i;
                        handler.postDelayed(new Runnable() { // used when we got to run some functionality after some delay ona thread
                            @Override
                            public void run() {
                                try
                                {
                                      if ((initBall.getX() - tiles[finalI].getX()) < screenWidth/6 && (initBall.getX() - tiles[finalI].getX())>0) { // getting thet tile
                                            gameContainer.removeView(tiles[finalI]);

                                            for (int j = 0; j < weight; j++) { // if curr length of snake is <10, increase the length by the amount of powerup
                                                if ((displayLength-j)<10 && (displayLength-j)>0) {

                                                    tailLength--;
                                                    linearLayout.removeView(tail[tailLength]);
                                                    Thread.sleep(100);
                                                }

                                            }

                                            displayLength -= weight;
                                            if(displayLength<=1){
                                                Toast.makeText(MainActivity.this, "GameOver", Toast.LENGTH_SHORT).show();
                                                android.os.Process.killProcess(android.os.Process.myPid());
                                                System.exit(1);
                                            }
                                            balls.setText(String.valueOf(displayLength));
//                                   Toast.makeText(getApplicationContext(), String.valueOf(newEnergy.getY()),Toast.LENGTH_SHORT).show();
                                        }

                                }
                                catch (Exception e)
                                {

                                }

                            }
                        }, (long)(0.7*fallingTime) );

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                gameContainer.removeView(tiles[finalI]);
                            }
                        }, fallingTime);
                    }

                }

                else if (wallProbability==4 || wallProbability==3)
                {
                    final Button[] tiles=new Button[6];
                    for (int i=0; i<6; i++)
                    {
                        int hideTile=new Random().nextInt(3);
                        if (hideTile==1)
                        {
                            continue;
                        }
                        tiles[i] = new Button(MainActivity.this);

                        tiles[i].setLayoutParams(new LinearLayout.LayoutParams((int)screenWidth/6, (int)screenWidth/6));
                        tiles[i].setTextColor(Color.WHITE);
                        tiles[i].setTextSize(25);
                        final int weight= new Random().nextInt(5)+1;
                        if(weight<2)
                            tiles[i].setBackgroundResource(R.drawable.box1);
                        else if(weight<3)
                            tiles[i].setBackgroundResource(R.drawable.box2);
                        else if(weight<4)
                            tiles[i].setBackgroundResource(R.drawable.box3);
                        else
                            tiles[i].setBackgroundResource(R.drawable.box4);
                        tiles[i].setText(Integer.toString(weight));
                        tiles[i].setY(-300);
                        tiles[i].setX(i*screenWidth/6);
                        gameContainer.addView(tiles[i]);
                        tiles[i].animate()
                                .translationY(screenHeight)
                                .setInterpolator(new LinearInterpolator())
//                                .setInterpolator(new BounceInterpolator())
                                .setDuration(fallingTime);

                        final int finalI = i;
                        handler.postDelayed(new Runnable() { // used when we got to run some functionality after some delay ona thread
                            @Override
                            public void run() {
                                try
                                {
                                    if ((initBall.getX() - tiles[finalI].getX()) < screenWidth/6 && (initBall.getX() - tiles[finalI].getX())>0) { // getting thet tile
                                        gameContainer.removeView(tiles[finalI]);

                                        for (int j = 0; j < weight; j++) { // if curr length of snake is <10, increase the length by the amount of powerup
                                            if ((displayLength-j)<10 && (displayLength-j)>0) {

                                                tailLength--;
                                                linearLayout.removeView(tail[tailLength]);
                                            }

                                        }

                                        displayLength -= weight;
                                        if(displayLength<1){
                                            Toast.makeText(MainActivity.this, "GameOver", Toast.LENGTH_SHORT).show();
                                            android.os.Process.killProcess(android.os.Process.myPid());
                                            System.exit(1);
                                        }
                                        balls.setText(String.valueOf(displayLength));
//                                   Toast.makeText(getApplicationContext(), String.valueOf(newEnergy.getY()),Toast.LENGTH_SHORT).show();
                                    }

                                }
                                catch (Exception e)
                                {

                                }

                            }

                        }, (long)(0.7*fallingTime));
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                gameContainer.removeView(tiles[finalI]);
                            }
                        }, fallingTime);
                    }
                }
                fallingVelocity=fallingVelocity+ ((1.2-fallingVelocity)/80);

                Log.i("Velocity",Double.toString(fallingVelocity));
                fallingTime= (int) (screenHeight/fallingVelocity);
                handler.postDelayed(this, fallingTime/4);
            }
        };

        handler.postDelayed(runnable, 100);


    }
}
