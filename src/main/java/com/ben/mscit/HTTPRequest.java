package com.ben.mscit;

import java.io.Serializable;

/**
 *
 * @author Benjamin
 */
public class HTTPRequest implements Serializable {

    private String sourceIP;
    private long requestDateTime;
    private String methodType;
    private String urlCall;
    private String httpType;
    private String httpRespCode;
    private String contentLength;
    private String clientType;
    private String upstreamServerIP;
    private String processingTime;
    private String upstreamConnectTime;
    private String upstreamResponseTime;
    private String module;
    private String fileName;

    public HTTPRequest() {
    }

    public String getMethodType() {
        return methodType;
    }

    public void setMethodType(String methodType) {
        this.methodType = methodType;
    }

    public String getUrlCall() {
        return urlCall;
    }

    public void setUrlCall(String urlCall) {
        this.urlCall = urlCall;
    }

    public String getHttpType() {
        return httpType;
    }

    public void setHttpType(String httpType) {
        this.httpType = httpType;
    }

    public String getHttpRespCode() {
        return httpRespCode;
    }

    public void setHttpRespCode(String httpRespCode) {
        this.httpRespCode = httpRespCode;
    }

    public String getContentLength() {
        return contentLength;
    }

    public void setContentLength(String contentLength) {
        this.contentLength = contentLength;
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public String getUpstreamServerIP() {
        return upstreamServerIP;
    }

    public void setUpstreamServerIP(String upstreamServerIP) {
        this.upstreamServerIP = upstreamServerIP;
    }

    public double getProcessingTime() {
        try {
            return Double.parseDouble(processingTime);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void setProcessingTime(String processingTime) {
        this.processingTime = processingTime;
    }

    public String getUpstreamConnectTime() {
        return upstreamConnectTime;
    }

    public void setUpstreamConnectTime(String upstreamConnectTime) {
        this.upstreamConnectTime = upstreamConnectTime;
    }

    public String getUpstreamResponseTime() {
        return upstreamResponseTime;
    }

    public void setUpstreamResponseTime(String upstreamResponseTime) {
        this.upstreamResponseTime = upstreamResponseTime;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSourceIP() {
        return sourceIP;
    }

    public void setSourceIP(String sourceIP) {
        this.sourceIP = sourceIP;
    }

    public long getRequestDateTime() {
        return requestDateTime;
    }

    public void setRequestDateTime(long requestDateTime) {
        this.requestDateTime = requestDateTime;
    }

    @Override
    public String toString() {
        return "HTTPTransaction {" + "module=" + module + ", requestDateTime=" + requestDateTime + ", sourceIP=" + sourceIP + ", upstreamServerIP=" + upstreamServerIP + ", httpRespCode=" + httpRespCode + ", urlcall="+urlCall+"}";
    }

}
