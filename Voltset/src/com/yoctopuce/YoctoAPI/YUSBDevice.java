package com.yoctopuce.YoctoAPI;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import com.yoctopuce.YoctoAPI.YAPI;
import com.yoctopuce.YoctoAPI.YUSBPkt.StreamHead;
import com.yoctopuce.YoctoAPI.YUSBRawDevice.PKTHandler;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

public class YUSBDevice implements PKTHandler {
	
	//USB communication data
	private int _lastpktno;
	private YUSBRawDevice _rawDev;
	
	// internal whites pages updated form notifications
	private final HashMap<String, WPEntry> _usbWP= new  HashMap<String, WPEntry>();
	// internal yellowpage pages updated form notifications
	private final HashMap<String, YPEntry> _usbYP= new  HashMap<String, YPEntry>();
		
	// state of the device 
	private final Object _stateLock = new Object();
	private State  _state=State.Detected;
	private enum State {
		Detected,ResetOk,ConfStarted,TCPReady,TCPWriting,TCPWaiting,NotWorking
	}
	private final StringBuffer _currentRequest=new StringBuffer(256);
	
	// mapping for ydx<serial> of potential subdevice for this USB device
	private ArrayList<String> _usbIdx2Serial= new ArrayList<String>();
	
	String getSerialFromYdx(int ydx)
	{
		return _usbIdx2Serial.get(ydx);
	}
	
	int getYdxFormSerial(String serial)
	{
		return _usbIdx2Serial.indexOf(serial);
	}
	
	
	
	
	private HashMap<String,String> _usbIdx2Funcid= new HashMap<String,String>();
	public String getFuncidFromYdx(String serial, int i)
	{
		String res = _usbIdx2Funcid.get(serial+i);
		return res;
	}

	
	
	private YPEntry getYPEntryForNotification(YUSBPkt.Notification not)
	{
		YPEntry yp;
		synchronized (_usbYP) {  
			if(!_usbYP.containsKey(not.getLongFunctionID())) {
				yp= new YPEntry(not.getSerial(),not.getShortFunctionID());
				_usbYP.put(not.getLongFunctionID(), yp);
			}else{
				 yp= _usbYP.get(not.getLongFunctionID());
			}		
		}
		return yp;
	}
	
	
	/*
	 * Notification handler
	 */
	private void handleNotifcation(StreamHead s) throws YAPI_Exception
	{
		YUSBPkt.Notification not = new YUSBPkt.Notification(s,this);
		//YAPI.Log(not.dump());
		WPEntry wp;
		YPEntry yp;
		synchronized (_usbWP) {
			if(!_usbWP.containsKey(not.getSerial())) {
				wp = new WPEntry(_usbWP.size(),not.getSerial(),"");
				_usbWP.put(not.getSerial(), wp);
			}else{
				wp = _usbWP.get(not.getSerial());
			}			
		}
			
		switch(not.getNotType()) 
		{
		case CHILD:
			_usbIdx2Serial.add(not.getDevydy(), not.getChildserial());
			break;
		case FIRMWARE:
			wp.setProductId(not.getDeviceid());
			break;
		case FUNCNAME:
			yp= getYPEntryForNotification(not);
			yp.setLogicalName(not.getFuncname());
			break;
		case FUNCNAMEYDX:
			_usbIdx2Funcid.put(not.getSerial()+not.getFunydx(), not.getShortFunctionID());
			yp = getYPEntryForNotification(not);
			yp.setLogicalName(not.getFuncname());
			yp.setIndex(not.getFunydx());
			break;
		case FUNCVAL:
			yp= getYPEntryForNotification(not);
			yp.setAdvertisedValue(not.getFuncval());
			break;
		case LOG:
			break;
		case NAME:
			wp.setLogicalName(not.getLogicalname());
			wp.setBeacon(not.getBeacon());
			break;
		case PRODNAME:
			wp.setProductName(not.getProduct());
			break;
		case STREAMREADY:			
			wp.validate();
			if(getSerial().equals(not.getSerial())){
				synchronized (_stateLock) {
					if(_state==State.ConfStarted){
						_state=State.TCPReady;
						_stateLock.notify();
					}else{
						YAPI.Log("Streamready to early! :"+_state );
					}
				}
			}
			break;
		}
	}
	
	/*
	 * new packet handler
	 */
	public void newPKT(ByteBuffer rawpkt)
	{
		try {
			YUSBPkt pkt = new YUSBPkt(rawpkt);
			pkt.parse();
			if(pkt.isConfPkt()){
				if(pkt.isConfPktReset()) {
					YUSBPkt.ConfPktReset reset = pkt.getConfPktReset();
					YUSBPkt.isCompatibe(reset.api, _rawDev.getSerial());
					setNewState(State.ResetOk);
				}else if(pkt.isConfPktStart()) {
					synchronized (_stateLock) {
						if(_state==State.ResetOk){
							_lastpktno = pkt.getPktno();
							_state=State.ConfStarted;
							_stateLock.notify();
						}else {
							YAPI.Log("Drop late confpkt:"+pkt.dumpToString());
						}
					}
				}else {
					YAPI.Log("Unknown configuration packet received:"+pkt.dumpToString());
					return;
				}
			} else {
				int expectedPktno= (_lastpktno+1)&7;
				if(pkt.getPktno()!=expectedPktno){
					YAPI.Log("Missing packet (look of pkt "+expectedPktno+" but get "+pkt.getPktno()+")\n");
			     }
				_lastpktno=pkt.getPktno();
				int nbstreams =pkt.getNbStreams();
				for(int i=0;i<nbstreams;i++) {
					StreamHead s = pkt.getStream(i);
					switch(s.getStreamType())
					{
					case YUSBPkt.YSTREAM_NOTICE:
						handleNotifcation(s);
						break;
					case YUSBPkt.YSTREAM_TCP:
						_currentRequest.append(s.getDataAsString());
						break;
					case YUSBPkt.YSTREAM_TCP_CLOSE:
						_currentRequest.append(s.getDataAsString());
						setNewState(State.TCPReady);
						break;
					case YUSBPkt.YSTREAM_EMPTY:
						break;
					}
				}
			}
		} catch (YAPI_Exception e) {
			YAPI.Log("Invalid packet received:"+e.getStackTraceToString());
		} catch (Exception e) {
	        Writer writer = new StringWriter();
	        PrintWriter printWriter = new PrintWriter(writer);
	        e.printStackTrace(printWriter);
		}
	}
	
	

	
	YUSBDevice(UsbDevice device, UsbManager manager)
	{
		super();
		_rawDev=new YUSBRawDevice(device, manager,this);
	}

	void release()
	{
		_rawDev.release();
	}
	
	private void setNewState(State newstate) 
	{
		synchronized (_stateLock) {
			_state=newstate;
			_stateLock.notify();
		}
	}
	
	
	private void waitFrorState(State wanted,State next,long mswait,String message) throws YAPI_Exception
	{
		long timeout = YAPI.GetTickCount()+mswait;
		synchronized (_stateLock) {
			while(_state != State.NotWorking && _state!=wanted && timeout>YAPI.GetTickCount()){
				long millis=timeout -YAPI.GetTickCount();
				try {
					_stateLock.wait(millis);
				} catch (InterruptedException e) {
					throw new YAPI_Exception(YAPI.TIMEOUT, "Device "+_rawDev.getSerial()+" "+message,e);
				}
			}
			if(_state == State.NotWorking) {
				throw new YAPI_Exception(YAPI.IO_ERROR, "Device "+_rawDev.getSerial()+" "+message+" (io error)");
			}
			if(_state != wanted) {
				throw new YAPI_Exception(YAPI.TIMEOUT, "Device "+_rawDev.getSerial()+" "+message+" ("+_state+")");
			}
			if(next != null){
				_state=next;
				_stateLock.notify();
			}
		}
	}
	
	public void reset() throws YAPI_Exception 
	{
		// ensure that the device is started
		_rawDev.start();
		//send reset packet
		YUSBPkt ypkt =new YUSBPkt();
		ypkt.formatResetPkt();
		_rawDev.sendPkt(ypkt);
		//wait ack
		waitFrorState(State.ResetOk,null,	5000, " did not respond to reset pkt");
		//send start pkt
		ypkt.clear();
		ypkt.formatStartPkt();
		_rawDev.sendPkt(ypkt);
		//wait ack
		waitFrorState(State.ConfStarted,null,	5000, "unable to start connection to device");
		//wait notification for devices ok
		waitFrorState(State.TCPReady,null,	5000, "unable to start device");
	}
	
	
	public synchronized String sendRequest(String request,boolean async) throws YAPI_Exception
	{
		String result="";
		// finish the request
		request +=" \r\n\r\n";
		YUSBPkt ypkt =new YUSBPkt();
		int pos =0;
		waitFrorState(State.TCPReady,State.TCPWriting, 1000, "device not ready");
		_currentRequest.setLength(0);
		while(pos < request.length()){
			String tmp =request.substring(pos);
			pos+=ypkt.pushTCP(tmp);
			ypkt.finishPkt();
			_rawDev.sendPkt(ypkt);	
			ypkt.clear();
		}
		if(!async){
			waitFrorState(State.TCPReady,null, 50*1000, "request did not finish in 50s");
			int contentstart =_currentRequest.indexOf("\r\n\r\n");
			if(contentstart>0){
				result = _currentRequest.substring(contentstart+4);
			}else {
				result= _currentRequest.toString();
			}
		}
		return result;
	}




	public String getSerial()
	{
		return _rawDev.getSerial();
	}


	public  void updateWhitesPages(ArrayList<WPEntry> publicWP)
	{
		synchronized (_usbWP) {
			for(WPEntry wp: _usbWP.values()){
				if(wp.isValid())
					publicWP.add(wp);
			}	
		}
	}

	public void updateYellowPages(HashMap<String, ArrayList<YPEntry>> publicYP)
	{
		synchronized (_usbYP) {
			for(YPEntry yp:_usbYP.values()) {
				if(!publicYP.containsKey(yp.getCateg()))
					publicYP.put(yp.getCateg(), new  ArrayList<YPEntry>());
				publicYP.get(yp.getCateg()).add(yp);
			}
		}
	}

	public void ioError()
	{
		setNewState(State.NotWorking);
	}
	
}
