package com.lhengi.project4;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.os.Message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    public Handler thread1Handler;
    public Handler thread2Handler;

    private TextView thread1SecretView;
    private TextView thread1GuessView; // this field will hold the guesses made by thread2 on thread1's secret

    private TextView thread2SecretView;
    private TextView thread2GuessView; // this field will hold the guesses made by thread1 on thread2's secret

    // this will hold the secrets

    public boolean endGame = false;


    public static final int SET_SECRET_1 = 1;
    public static final int SET_SECRET_2 = 2;

    public static final int UPDATE_THREAD1_GUESS = 3;
    public static final int UPDATE_THREAD2_GUESS = 4;

    public static final int ANSWER_REQUEST = 5;
    public static final int GUESS_REQUEST = 6;


    public Handler uiHandler = new Handler(){
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case(SET_SECRET_1):
                    thread1SecretView.setText((String)msg.obj);
                    break;
                case(SET_SECRET_2):
                    thread2SecretView.setText((String)msg.obj);
                    break;
                case(UPDATE_THREAD1_GUESS):
                    String displayText = (String) msg.obj;
                    thread1GuessView.setText(thread1GuessView.getText()+"\n"+displayText);
                    break;
                case(UPDATE_THREAD2_GUESS):
                    String displayText2 = (String) msg.obj;
                    thread2GuessView.setText(thread2GuessView.getText()+"\n"+displayText2);
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        thread1SecretView = (TextView) findViewById(R.id.thread1Secret);
        thread1GuessView = (TextView) findViewById(R.id.thread1Guess);

        thread2SecretView = (TextView) findViewById(R.id.thread2Secret);
        thread2GuessView = (TextView) findViewById(R.id.thread2Guess);

        Thread t1 = new Thread(new Thread1());
        Thread t2 = new Thread(new Thread2());
        t1.start();
        t2.start();

    }


    // number of with correct digits in correct position
    // number of digits in wrong position
    // index that's correct
    public String giveFeedBack(String guess, String secret)
    {

        String feedBack = "";
        int numCorrect = 0;
        boolean[] correctPos = {false,false,false,false};
        for (int i = 0; i < guess.length(); i++)
        {
            if (guess.charAt(i) == secret.charAt(i))
            {
                correctPos[i] = true;
                numCorrect++;
            }
        }

        int numWrong = 0;

        // secret = 1 2 3 4
        // guess =  1 0 5 2

        for (int i = 0; i < guess.length();i++)
        {
            if (!correctPos[i])
            {
                for (int j = 0; j < guess.length();j++)
                {
                    if(!correctPos[i] && guess.charAt(i) == secret.charAt(j))
                    {
                        numWrong++;
                    }

                }
            }
        }

        feedBack += numCorrect+"-"+numWrong;
        for (int i = 0; i < correctPos.length; i++)
        {
            if (correctPos[i])
                feedBack += "-"+i;
        }

        //System.out.println("&&&&&&&&&& in FeedBack: "+feedBack);
        return feedBack;

    }

    public String thread2Strategy(String feedBack, String originalGuess)
    {
        String guess = "";

        String[] feedBackArr = feedBack.split("-");
        boolean[] lockArr = {false,false,false,false};

        int numCorrect = Integer.parseInt(feedBackArr[0]);
        System.out.println("******"+feedBack);
        int numWrong = Integer.parseInt(feedBackArr[1]);

        if (numCorrect > 0)
        {
            for (int i = 2; i < feedBackArr.length;i++)
            {
                lockArr[Integer.parseInt(feedBackArr[i])] = true;
            }
        }

        ArrayList<Integer> partialGuess = new ArrayList<>();
        for (int i = 0; i < 10; i++)
        {
            partialGuess.add(i);
        }

        for (int i = 0; i < lockArr.length;i++)
        {
            if (lockArr[i])
            {
                partialGuess.remove(Integer.valueOf(Integer.parseInt(Character.toString(originalGuess.charAt(i)))));
            }

        }
        //Log.i("Testing","partialGuess: " + partialGuess.toString());
        Collections.shuffle(partialGuess);
        int k = 0;
        for (int i =0; i < lockArr.length;i++)
        {
            if(lockArr[i])
            {
                guess += originalGuess.charAt(i);
            }
            else
            {
                guess += partialGuess.get(k);
                k++;
            }
            //Log.i("Testing","Guess: "+guess);
        }

        return guess;
    }

    public String thread1Strategy(String feedBack, String originalGuess)
    {
        String guess = "";

        String[] feedBackArr = feedBack.split("-");
        boolean[] lockArr = {false,false,false,false};

        int numCorrect = Integer.parseInt(feedBackArr[0]);
        int numWrong = Integer.parseInt(feedBackArr[1]);

        if (numCorrect > 0)
        {
            for (int i = 2; i < feedBackArr.length;i++)
            {
                lockArr[Integer.parseInt(feedBackArr[i])] = true;
            }
        }

        ArrayList<Integer> partialGuess = new ArrayList<>();
        for (int i = 0; i < 10; i++)
        {
            partialGuess.add(i);
        }


        if (numWrong <= 0)
        {   // random guess and keep the correct index

            // remove correct guess
            for (int i = 0; i < lockArr.length; i++)
            {

                partialGuess.remove(Integer.valueOf(Integer.parseInt(Character.toString(originalGuess.charAt(i)))));

            }
            Collections.shuffle(partialGuess);
            // create new guess
            for (int i = 0; i < lockArr.length;i++)
            {
                if(lockArr[i])
                {
                    guess += Character.toString(originalGuess.charAt(i));
                }
                else
                {
                    guess += partialGuess.get(i);
                }
            }
        }
        else
        {

            ArrayList<String> wrongPosGuess = new ArrayList<>();
            for (int i = 0; i < lockArr.length; i++)
            {
                if (!lockArr[i])
                    wrongPosGuess.add(Character.toString(originalGuess.charAt(i)));
            }
            Collections.shuffle(wrongPosGuess);
            Log.i("Testing","Wrong pos Guess: " + wrongPosGuess.toString());
            int k =0;
            for (int i = 0; i < lockArr.length; i++)
            {
                if (lockArr[i])
                {
                    guess += Character.toString(originalGuess.charAt(i));
                }
                else
                {
                    guess += wrongPosGuess.get(k);
                    k++;
                }

            }

        }

        return guess;
    }

    public class Thread1 implements Runnable
    {

        private String secretStr;
        private Message myMsg;
        private String guess;
        private int counter = 0;

        public void run()
        {
            Looper.prepare();


            thread1Handler = new Handler()
            {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what)
                    {
                        case(ANSWER_REQUEST):
                            // exam thread2's answer and give feedback
                            // send answer and feedback to UI thread to display
                            // send feedback with GUESS_REQUEST
                            if (endGame)
                            {
                                break;
                            }
                            String opponentGuess = (String) msg.obj;
                            if (opponentGuess.equals(secretStr))
                            {
                                myMsg = uiHandler.obtainMessage(UPDATE_THREAD2_GUESS);
                                myMsg.obj = "Thread2 guessed: " + opponentGuess + "\nThread2 Won!!!";
                                uiHandler.sendMessage(myMsg);
                                endGame = true;

                                break;
                            }

                            String feedBack = giveFeedBack(opponentGuess,secretStr);
                            String displayText = "Thread2 guessed: " + opponentGuess + " feedback: "+feedBack;
                            myMsg = uiHandler.obtainMessage(UPDATE_THREAD2_GUESS);
                            myMsg.obj = displayText;
                            try{Thread.sleep(2000);}
                            catch (InterruptedException e){System.out.println("Thread 1 interrupt");};
                            uiHandler.sendMessage(myMsg);

                            myMsg = thread2Handler.obtainMessage(GUESS_REQUEST);
                            myMsg.obj = feedBack;
                            thread2Handler.sendMessage(myMsg);


                            break;
                        case(GUESS_REQUEST):
                            // make a strategic guess based on feedback
                            // send guess to thread2
                            if (endGame)
                            {
                                break;
                            }
                            if(counter > 20)
                            {
                                endGame = true;
                                myMsg = uiHandler.obtainMessage(UPDATE_THREAD2_GUESS);
                                myMsg.obj = "Reached 20 guesses";
                                break;
                            }
                            String feedback = (String) msg.obj;
                            guess = thread1Strategy(feedback,guess);
                            myMsg = thread2Handler.obtainMessage(ANSWER_REQUEST);
                            myMsg.obj = guess;
                            thread2Handler.sendMessage(myMsg);
                            counter++;
                            break;
                    }
                }
            };


            // Generate new secret and send to UI thread
            ArrayList<Integer> secretInt = new ArrayList<>();
            for (int i = 0; i < 10; i++)
                secretInt.add(i);
            Collections.shuffle(secretInt);
            secretStr = ""+secretInt.get(0).toString() + secretInt.get(1).toString() + secretInt.get(2).toString() + secretInt.get(3).toString();

            myMsg = uiHandler.obtainMessage(SET_SECRET_1);
            myMsg.obj = secretStr;
            uiHandler.sendMessage(myMsg);

            try{Thread.sleep(2000);}
            catch (InterruptedException e){System.out.println("Thread1 interrupted!");};


            // make an initial guess and send to thread 2
            guess = "0123";
            myMsg = thread2Handler.obtainMessage(ANSWER_REQUEST);
            myMsg.obj = guess;
            thread2Handler.sendMessage(myMsg);

            Looper.loop();

        }

    }


    public class Thread2 implements Runnable
    {

        private String secretStr;
        private String guess;
        private Message myMsg;
        private int counter = 0;

        public void run()
        {
            Looper.prepare();

            thread2Handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what)
                    {
                        case(ANSWER_REQUEST):
                            // exam thread2's answer and give feedback
                            // send answer and feedback to UI thread to display
                            // send feedback with GUESS_REQUEST
                            if (endGame)
                            {
                                break;
                            }
                            String opponentGuess = (String) msg.obj;
                            if (opponentGuess.equals(secretStr))
                            {
                                myMsg = uiHandler.obtainMessage(UPDATE_THREAD1_GUESS);
                                myMsg.obj = "Thread1 guessed: " + opponentGuess+ "\nThread1 Won!!!";
                                uiHandler.sendMessage(myMsg);
                                endGame = true;

                                break;
                            }
                            String feedBack = giveFeedBack(opponentGuess,secretStr);
                            String displayText = "Thread1 guessed: " + opponentGuess + " feedback: "+feedBack;
                            myMsg = uiHandler.obtainMessage(UPDATE_THREAD1_GUESS);
                            myMsg.obj = displayText;
                            try{Thread.sleep(2000);}
                            catch (InterruptedException e){System.out.println("Thread 2 interrupt");};
                            uiHandler.sendMessage(myMsg);

                            myMsg = thread1Handler.obtainMessage(GUESS_REQUEST);
                            myMsg.obj = feedBack;
                            thread1Handler.sendMessage(myMsg);


                            break;
                        case(GUESS_REQUEST):
                            // make a stratigic guess based on feedback
                            // send guess to thread2
                            if (endGame)
                            {
                                break;
                            }
                            if(counter > 20)
                            {
                                endGame = true;
                                myMsg = uiHandler.obtainMessage(UPDATE_THREAD1_GUESS);
                                myMsg.obj = "Reached 20 guesses";
                                break;
                            }
                            String feedback = (String) msg.obj;
                            guess = thread2Strategy(feedback,guess);
                            myMsg = thread1Handler.obtainMessage(ANSWER_REQUEST);
                            myMsg.obj = guess;
                            thread1Handler.sendMessage(myMsg);
                            counter++;
                            break;
                    }
                }
            };

            // Generate new secret and send to UI thread
            ArrayList<Integer> secretInt = new ArrayList<>();
            for (int i = 0; i < 10; i++)
                secretInt.add(i);
            Collections.shuffle(secretInt);
            secretStr = ""+secretInt.get(0).toString() + secretInt.get(1).toString() + secretInt.get(2).toString() + secretInt.get(3).toString();

            myMsg = uiHandler.obtainMessage(SET_SECRET_2);
            myMsg.obj = secretStr;
            uiHandler.sendMessage(myMsg);

            try{Thread.sleep(2000);}
            catch (InterruptedException e){System.out.println("Thread2 interrupted!");};


            // make an initial guess and send to thread 1
            guess = "0123";
            myMsg = thread1Handler.obtainMessage(ANSWER_REQUEST);
            myMsg.obj = guess;
            thread1Handler.sendMessage(myMsg);



            Looper.loop();
        }

    }
}