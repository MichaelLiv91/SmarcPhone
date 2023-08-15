package UdpHandler;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;


import GpsManager.GPSTracker;

public class UdpManager {

    final private int UTC_MESSAGE_SIZE = 14;
    final private int PP_MESSAGE_SIZE = 84;
    final private int HEADER_SIZE = 4;
    private int UTCport;
    private int PPport;
    private DatagramSocket datagramSocket;
    static UdpManager udpManager = null;
    InetAddress address;
    Thread thread;
    long currentTime;
    long timezoneOffset;
    Timer timer;

    private UdpManager()
    {
        try {
            datagramSocket = new DatagramSocket();
            address = null;
            UTCport = 10030;
            PPport = 11000;
            timer = new Timer();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void connect(String ip) throws UnknownHostException {

        if (address == null) {
            address = InetAddress.getByName(ip);
            TimerTask myTimerTask = new TimerTask() {

                @RequiresApi(api = Build.VERSION_CODES.N)
                public void run() {
                    if(datagramSocket != null)
                    {
                        TimeZone tz = TimeZone.getDefault();
                        Calendar cal = GregorianCalendar.getInstance(tz);
                        timezoneOffset = tz.getOffset(cal.getTimeInMillis());
                        currentTime = System.currentTimeMillis();
                        sendUTCTime();
                        sendPresentPosition();
                    }
                }
            };
            timer.schedule(myTimerTask,0,1000);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void sendPresentPosition() {

        byte[] header = new byte[HEADER_SIZE];
        ByteBuffer Message = ByteBuffer.allocate(PP_MESSAGE_SIZE);
        ByteBuffer msgID = ByteBuffer.allocate(Integer.BYTES);
        msgID.order(ByteOrder.LITTLE_ENDIAN);
        msgID.putInt(0x20);
        Message.put(msgID.array(),0,HEADER_SIZE);

        ByteBuffer msgLen = ByteBuffer.allocate(Integer.BYTES);
        msgLen.order(ByteOrder.LITTLE_ENDIAN);
        msgLen.putInt(PP_MESSAGE_SIZE);
        Message.put(msgLen.array(),0,Integer.BYTES);

        ByteBuffer timeValid = ByteBuffer.allocate(Integer.BYTES);
        timeValid.order(ByteOrder.LITTLE_ENDIAN);
        timeValid.putInt(1);
        Message.put(timeValid.array(),0,Integer.BYTES);

        ByteBuffer year = ByteBuffer.allocate(Integer.BYTES);
        year.order(ByteOrder.LITTLE_ENDIAN);
        year.putInt(2023);
        Message.put(year.array(),0,Integer.BYTES);

        ByteBuffer dayOfyear = ByteBuffer.allocate(Integer.BYTES);
        dayOfyear.order(ByteOrder.LITTLE_ENDIAN);
        dayOfyear.putInt(200);
        Message.put(dayOfyear.array(),0,Integer.BYTES);

        ByteBuffer secondsofDay = ByteBuffer.allocate(Integer.BYTES);
        secondsofDay.order(ByteOrder.LITTLE_ENDIAN);
        secondsofDay.putInt(10000);
        Message.put(secondsofDay.array(),0,Integer.BYTES);

        ByteBuffer ms = ByteBuffer.allocate(Integer.BYTES);
        ms.order(ByteOrder.LITTLE_ENDIAN);
        ms.putInt(0);
        Message.put(ms.array(),0,Integer.BYTES);

        ByteBuffer latValidity = ByteBuffer.allocate(Integer.BYTES);
        latValidity.order(ByteOrder.LITTLE_ENDIAN);
        latValidity.putInt(1);
        Message.put(latValidity.array(),0,Integer.BYTES);

        ByteBuffer lat = ByteBuffer.allocate(Float.BYTES);
        lat.order(ByteOrder.LITTLE_ENDIAN);
        lat.putFloat((float) GPSTracker.getInstance().getLatitude());
        Message.put(lat.array(),0,Float.BYTES);

        ByteBuffer lonValidity = ByteBuffer.allocate(Integer.BYTES);
        lonValidity.order(ByteOrder.LITTLE_ENDIAN);
        lonValidity.putInt(1);
        Message.put(lonValidity.array(),0,Integer.BYTES);

        ByteBuffer lon = ByteBuffer.allocate(Float.BYTES);
        lon.order(ByteOrder.LITTLE_ENDIAN);
        lon.putFloat((float) GPSTracker.getInstance().getLongitude());
        Message.put(lon.array(),0,Float.BYTES);

        ByteBuffer altValidity = ByteBuffer.allocate(Integer.BYTES);
        altValidity.order(ByteOrder.LITTLE_ENDIAN);
        altValidity.putInt(1);
        Message.put(altValidity.array(),0,Integer.BYTES);

        ByteBuffer alt = ByteBuffer.allocate(Float.BYTES);
        alt.order(ByteOrder.LITTLE_ENDIAN);
        alt.putFloat((float) GPSTracker.getInstance().getAltitude());
        Message.put(alt.array(),0,Float.BYTES);

        ByteBuffer velXValidity = ByteBuffer.allocate(Integer.BYTES);
        velXValidity.order(ByteOrder.LITTLE_ENDIAN);
        velXValidity.putInt(1);
        Message.put(velXValidity.array(),0,Integer.BYTES);

        ByteBuffer velX = ByteBuffer.allocate(Float.BYTES);
        velX.order(ByteOrder.LITTLE_ENDIAN);
        velX.putFloat(0);
        Message.put(velX.array(),0,Float.BYTES);

        ByteBuffer velYValidity = ByteBuffer.allocate(Integer.BYTES);
        velYValidity.order(ByteOrder.LITTLE_ENDIAN);
        velYValidity.putInt(1);
        Message.put(velYValidity.array(),0,Integer.BYTES);

        ByteBuffer velY = ByteBuffer.allocate(Float.BYTES);
        velY.order(ByteOrder.LITTLE_ENDIAN);
        velY.putFloat(0);
        Message.put(velY.array(),0,Float.BYTES);

        ByteBuffer velZValidity = ByteBuffer.allocate(Integer.BYTES);
        velZValidity.order(ByteOrder.LITTLE_ENDIAN);
        velZValidity.putInt(1);
        Message.put(velZValidity.array(),0,Integer.BYTES);

        ByteBuffer velZ = ByteBuffer.allocate(Float.BYTES);
        velZ.order(ByteOrder.LITTLE_ENDIAN);
        velZ.putFloat(0);
        Message.put(velZ.array(),0,Float.BYTES);

        ByteBuffer headingValidity = ByteBuffer.allocate(Integer.BYTES);
        headingValidity.order(ByteOrder.LITTLE_ENDIAN);
        headingValidity.putInt(1);
        Message.put(headingValidity.array(),0,Integer.BYTES);

        ByteBuffer heading = ByteBuffer.allocate(Float.BYTES);
        heading.order(ByteOrder.LITTLE_ENDIAN);
        heading.putFloat((float) GPSTracker.getInstance().getBearing());
        Message.put(heading.array(),0,Float.BYTES);

        send(Message.array(), PP_MESSAGE_SIZE,PPport);
    }

    private void sendUTCTime()
    {

        Date date = new Date(currentTime - timezoneOffset);
        Calendar c = Calendar.getInstance();
        c.setTime(date);

        byte[] header = new byte[HEADER_SIZE];
        ByteBuffer seconds = ByteBuffer.allocate(4);
        ByteBuffer year = ByteBuffer.allocate(2);
        ByteBuffer dayOfYear = ByteBuffer.allocate(2);

        header[0] = 0;
        header[1] = UTC_MESSAGE_SIZE - 2;
        header[2] = 0x50;
        header[3] = 0x50;

        ByteBuffer Message = ByteBuffer.allocate(UTC_MESSAGE_SIZE);

        Message.put(header,0,HEADER_SIZE);
        year.putShort((short) c.get(Calendar.YEAR));
        dayOfYear.putShort((short) c.get(Calendar.DAY_OF_YEAR));

        int secondOfDay = c.get(Calendar.HOUR_OF_DAY)*60*60 + c.get(Calendar.MINUTE)*60 +c.get(Calendar.SECOND);
        seconds.putInt(secondOfDay);

        Message.put(year.array(),0,2);
        Message.put(dayOfYear.array(),0 ,2);
        Message.put(seconds.array(),0,4);

        send(Message.array(),UTC_MESSAGE_SIZE,UTCport);
    }



    private void send( byte[] message, int size, int port) {
        DatagramPacket request = new DatagramPacket(message, size, address, port);
        try {
            datagramSocket.send(request);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(String.valueOf(Log.DEBUG),"Cannot send message");
        }
    }

    public void receive()
    {

    }

    static public UdpManager getInstance()
    {
        if(udpManager == null) {
            udpManager = new UdpManager();
        }
        return udpManager;
    }

}
