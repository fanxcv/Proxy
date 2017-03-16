package com.fan.proxy;

public class Configer {

	private String mode;
	private String listen_port;
	private String https_connect;

	private String http_ip;
	private String http_port;
	private String[] http_del;
	private String http_first;

	private String https_ip;
	private String https_port;
	private String[] https_del;
	private String https_first;

	private String dns_tcp;
	private String dns_listen_port;
	private String dns_url;

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getListen_port() {
		return listen_port;
	}

	public void setListen_port(String listen_port) {
		this.listen_port = listen_port;
	}

	public String getHttps_connect() {
		return https_connect;
	}

	public void setHttps_connect(String https_connect) {
		this.https_connect = https_connect;
	}

	public String getHttp_ip() {
		return http_ip;
	}

	public void setHttp_ip(String http_ip) {
		this.http_ip = http_ip;
	}

	public String getHttp_port() {
		return http_port;
	}

	public void setHttp_port(String http_port) {
		this.http_port = http_port;
	}

	public String[] getHttp_del() {
		return http_del;
	}

	public void setHttp_del(String[] http_del) {
		this.http_del = http_del;
	}

	public String getHttp_first() {
		return http_first;
	}

	public void setHttp_first(String http_first) {
		this.http_first = http_first;
	}

	public String getHttps_ip() {
		return https_ip;
	}

	public void setHttps_ip(String https_ip) {
		this.https_ip = https_ip;
	}

	public String getHttps_port() {
		return https_port;
	}

	public void setHttps_port(String https_port) {
		this.https_port = https_port;
	}

	public String[] getHttps_del() {
		return https_del;
	}

	public void setHttps_del(String[] https_del) {
		this.https_del = https_del;
	}

	public String getHttps_first() {
		return https_first;
	}

	public void setHttps_first(String https_first) {
		this.https_first = https_first;
	}

	public String getDns_tcp() {
		return dns_tcp;
	}

	public void setDns_tcp(String dns_tcp) {
		this.dns_tcp = dns_tcp;
	}

	public String getDns_listen_port() {
		return dns_listen_port;
	}

	public void setDns_listen_port(String dns_listen_port) {
		this.dns_listen_port = dns_listen_port;
	}

	public String getDns_url() {
		return dns_url;
	}

	public void setDns_url(String dns_url) {
		this.dns_url = dns_url;
	}

	private Configer() {
	}

	private static final Configer instance = new Configer();

	public final static Configer getInstance() {
		return instance;
	}
}
