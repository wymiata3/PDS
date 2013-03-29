/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoctopuce.YoctoAPI;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;


class yHTTPRequest implements Runnable{




    private enum State {
        AVAIL, IN_REQUEST,STOPED
    }

    public static final long MAX_REQUEST_MS = 60*1000;

    private YHTTPHub       _hub;
    private String         _request;
    private Socket         _socket  = null;
    private PrintWriter    _out     = null;
    private BufferedReader _in      = null;
    private State          _state= State.AVAIL;
    private boolean        _eof;
    @SuppressWarnings("unused")
	private String         _dbglabel;
    private String         _header;

    private final StringBuilder _result = new StringBuilder(1024);

    yHTTPRequest(YHTTPHub hub,String dbglabel)
    {
        _hub = hub;
        _dbglabel =dbglabel;
    }


    synchronized void _requestReserve() throws YAPI_Exception
    {
        long timeout = YAPI.GetTickCount()+MAX_REQUEST_MS;
        while (timeout>YAPI.GetTickCount() &&  _state != State.AVAIL) {
            try {
                long toWait = timeout - YAPI.GetTickCount();
                wait(toWait);
            } catch(InterruptedException ie) {
                throw new YAPI_Exception(YAPI.TIMEOUT, "Last Http request did not finished");
            }
        }
        if(_state != State.AVAIL)
            throw new YAPI_Exception(YAPI.TIMEOUT, "Last Http request did not finished");
        _state   = State.IN_REQUEST;
    }

    synchronized void _requestRelease()
    {
        _state   = State.AVAIL;
        notify();
    }



    void _requestStart(String request) throws YAPI_Exception
    {
        int pos=request.indexOf('\r');
        if (pos>0) request = request.substring(0, pos);
        _request = request;

        request += "\r\n"+_hub.getAuthorization(request)+"Connection: close\r\n\r\n";
        try {
            _socket  = new Socket(_hub.getHttpHost(), _hub.getHttpPort());
            _socket.setTcpNoDelay(true);
            _out     = new PrintWriter(_socket.getOutputStream(), true);
            _in      = new BufferedReader(new InputStreamReader(
                                        _socket.getInputStream()));
            _result.setLength(0);
            _header=null;
            _eof=false;
	    _out.print(request);
            _out.flush();
        }
        catch (UnknownHostException e)
        {
            _requestStop();
            throw new YAPI_Exception(YAPI.INVALID_ARGUMENT,"Unknown host("+_hub.getHttpHost()+")");
        }
        catch (IOException e)
        {
            _requestStop();
            throw new YAPI_Exception(YAPI.IO_ERROR,e.getLocalizedMessage());
        }
    }


    void _requestStop()
    {
        try {
            if(_out!=null){
                _out.close();
                _out = null;
            }
            if(_in!=null){
                _in.close();
                _in = null;
            }
            if(_socket!= null){
                _socket.close();
                _socket = null;
            }
        } catch (IOException e) {
        }
    }

    void _requestReset() throws YAPI_Exception
    {
        _requestStop();
        _requestStart(_request);
    }


     int _requestProcesss() throws YAPI_Exception
    {
        boolean retry;
        int read;

        if(_eof)
            return -1;

        do {
            retry=false;
            char[] buffer = new char[1024];
            try {
                read = _in.read(buffer,0,buffer.length);
            } catch (IOException e) {
                throw new YAPI_Exception(YAPI.IO_ERROR,e.getLocalizedMessage());
            }
            if(read<0){
                // end of connection
                _eof=true;
            }else if(read>0){
                synchronized(_result) {
                    _result.append(buffer, 0, read);
                    if( _header == null ) {
                        int pos =_result.indexOf("\r\n\r\n");
                        if( pos > 0 ) {
                            String header_str = _result.substring(0,pos+4);
                            if ( !header_str.startsWith("OK\r\n") ) {
                                int lpos = header_str.indexOf("\r\n");
                                if(!header_str.startsWith("HTTP/1.1 "))
                                    throw new YAPI_Exception(YAPI.IO_ERROR,"Invalid HTTP response header");

                                String parts[] = header_str.substring(9, lpos).split(" ");
                                if(parts[0].equals("401")){

                                    if(!_hub.needRetryWithAuth()) {
                                        // No credential provided, give up immediately
                                        throw new YAPI_Exception(YAPI.UNAUTHORIZED,"Authentication required");
                                    } else {
                                        _hub.parseWWWAuthenticate(header_str);
                                        _requestReset();
                                        retry=true;
                                        break;
                                    }

                                }
                                if(!parts[0].equals("200") && !parts[0].equals("304")){
                                    throw new YAPI_Exception(YAPI.IO_ERROR,"Received HTTP status "+parts[0]+" ("+parts[1]+")");
                                }
                            }
                            _hub.authSucceded();
                            _header = header_str;
                            _result.delete(0, pos+4);
                        }
                    }

                }
            }
        }while(retry);
        return read;
    }


    String requestReadLine() throws YAPI_Exception
    {
        String res="";
        synchronized(_result) {
            if(_header==null)
                return "";
            if(_result.length()==0){
                if(_eof)
                    throw new YAPI_Exception(YAPI.NO_MORE_DATA,"end of file reached");
                return "";
            }

            int pos    = _result.indexOf("\n");
            if( pos>0){
                res = _result.substring(0, pos+1);
                _result.delete(0, pos+1);
            }else if(_eof){
                res = _result.substring(0);
                _result.setLength(0);
            }
        }
        return res;
    }



    String RequestSync(String str_request) throws YAPI_Exception
    {
        String res;
        _requestReserve();
        try {
           _requestStart(str_request);
            int read;
            do {
                read = _requestProcesss();
            }while (read>=0);
            synchronized(_result){
                res=_result.toString();
                _result.setLength(0);
            }
        } catch (YAPI_Exception ex) {
            _requestStop();
            _requestRelease();
            throw  ex;
        }
        _requestStop();
        _requestRelease();
        return res;
    }




    @Override
    public void run()
    {
        try {
           _requestProcesss();
            int read;
            do {
                read = _requestProcesss();
            }while (read>=0);
        } catch (YAPI_Exception ex) {
        }
        _requestStop();
        _requestRelease();
    }


    void RequestAsync(String str_request) throws YAPI_Exception
    {
        _requestReserve();
        try {
           _requestStart(str_request);
           Thread t = new Thread(this);
           t.start();
        } catch (YAPI_Exception ex) {
            _requestRelease();
            throw  ex;
        }
    }

    synchronized void WaitRequestEnd()
    {
        long timeout = YAPI.GetTickCount()+MAX_REQUEST_MS;
        while (timeout>YAPI.GetTickCount() &&  _state == State.IN_REQUEST) {
            try {
                long toWait = timeout - YAPI.GetTickCount();
                wait(toWait);
            } catch(InterruptedException ie) {
                break;
            }
        }
        if(_state == State.IN_REQUEST)
            YAPI.Log("WARNING: Last Http request did not finished");
        _state   = State.STOPED;
    }



}
