package com.szanfu.sdk.test.util;

public class Event
{
    private final String TAG = Event.class.getSimpleName();

    public final static int INFINITE = 0xFFFFFFFF;
    public final static int WAIT_TIMEOUT = -100;

    public final static int EVENT__ALL = 0xFFFFFFFF;

    //use the highest bit as the flag of  "rc != 0 on "Retrieve_Events()"
    public final static int EVENT__RETRIEVED_NOTHING = 0x80000000;

    String name = "";
    Object object = new Object();
    int eventID = 0;

    public Event(String eventName)
    {
        name = eventName;
    }

    public int Clear_Events()
    {
        synchronized (object) {
            //LOG.i(TAG, "[" + name + "] " + "Clear_Events [" + LOG.__FILE__(2) + "]");
            eventID = 0;
        }
        return 0;
    }

    public int Clear_Event(int event)
    {
        synchronized (object) {
            //LOG.i(TAG, "[" + name + "] " + "Clear_Event [" + LOG.__FILE__(2) + "]");
            eventID = eventID & (~event);
        }
        return 0;
    }

    public int Set_Events(int event)
    {
        synchronized (object)
        {
            eventID = eventID | event;

            //LOG.i(TAG, "[" + name + "] " + "Set_Events " +
            //				Integer.toHexString(event) + " " +
            //				Integer.toHexString(eventID) + " " +
            //				" [" + LOG.__FILE__(2) + "]");

            object.notifyAll();
        }
        return 0;
    }

    public int Retrieve_Events(int wait_events, int return_events[])
    {
        return Retrieve_Events(wait_events, return_events, INFINITE);
    }

    public int Retrieve_Events(int wait_events, int return_events[], int timeout_ms)
    {
        //don't add log here which will make the running slower
		/*
		if(timeout_ms == INFINITE)
			LOG.i(TAG, "[" + name + "] " + "Retrieve_Events: INFINITE [" + LOG.__FILE__(2) + "]");
		else
			LOG.i(TAG, "[" + name + "] " + "Retrieve_Events: " + timeout_ms + " [" + LOG.__FILE__(2) + "]");
		*/

        long timestart = 0;
        long timestop  = 0;
        int last_wait_timeout = timeout_ms;

        boolean bUpdateTime;
        if(timeout_ms == INFINITE)
            bUpdateTime = false;
        else
            bUpdateTime = true;

        timestart = System.currentTimeMillis();
        while(true)
        {
            synchronized (object)
            {
                try {
                    //Immediately mode
                    if(timeout_ms == 0)
                    {
                        if ((eventID & wait_events) != 0)
                        {
                            return_events[0] = eventID;
                            //don't add log here which will make the running slower
                            //LOG.i(TAG, "[" + name + "] " + "Retrieve_Events: return 0 [" + LOG.__FILE__(2) + "]");
                            return 0;
                        }
                        else
                        {
                            //don't add log here which will make the running slower
                            //LOG.i(TAG, "[" + name + "] " + "Retrieve_Events: return -1 [" + LOG.__FILE__(2) + "]");
                            return -1;
                        }
                    }

                    //for timeout mode, we also check the event ready immediately before wait()
                    if ((eventID & wait_events) != 0)
                    {
                        return_events[0] = eventID;
                        //don't add log here which will make the running slower
                        //LOG.i(TAG, "[" + name + "] " + "Retrieve_Events: return 0 [" + LOG.__FILE__(2) + "]");
                        return 0;
                    }

                    if(timeout_ms == INFINITE)
                    {
                        //don't add log here which will make the running slower
                        //LOG.i(TAG, "[" + name + "] " + "Retrieve_Events: INFINITE [" + LOG.__FILE__(2) + "]");
                        object.wait();
                        //LOG.i(TAG, "[" + name + "] " + "Retrieve_Events: got it [" + LOG.__FILE__(2) + "]");
                    }
                    else
                    {
                        //don't add log here which will make the running slower
                        //LOG.i(TAG, "[" + name + "] " + "Retrieve_Events: " + timeout_ms + " [" + LOG.__FILE__(2) + "]");
                        object.wait(last_wait_timeout);
                        //LOG.i(TAG, "[" + name + "] " + "Retrieve_Events: got it [" + LOG.__FILE__(2) + "]");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    //LOG.e(TAG_SYSTEM_ERR, LOG.getErrorInfoFromException(e));
                    //don't add log here which will make the running slower
                    //LOG.i(TAG, "[" + name + "] " + "Retrieve_Events: return -1 (exception) [" + LOG.__FILE__(2) + "]");
                    return -1;
                }
            }
            if ((eventID & wait_events) != 0)
            {
                return_events[0] = eventID;
                //don't add log here which will make the running slower
                //LOG.i(TAG, "[" + name + "] " + "Retrieve_Events: return 0 [" + LOG.__FILE__(2) + "]");
                return 0;
            }

            if(bUpdateTime)
            {
                timestop = System.currentTimeMillis();
                last_wait_timeout = timeout_ms - (int)(timestop - timestart);
                if(last_wait_timeout <= 0)
                    break;
            }
        }

        //don't add log here which will make the running slower
        //LOG.i(TAG, "[" + name + "] " + "Retrieve_Events: return TIMEOUT [" + LOG.__FILE__(2) + "]");
        return WAIT_TIMEOUT;	//timeout
    }
}
