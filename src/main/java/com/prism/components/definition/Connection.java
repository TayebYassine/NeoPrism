package com.prism.components.definition;

import java.util.UUID;

public class Connection {
	private UUID id;
	private String name;
	private String host;
	private String port;
	private String user;
	private int provider;

	public Connection() { }

	public Connection(UUID id, String name, String host, String port, String user, int provider) {
		this.id = id;
		this.name = name;
		this.host = host;
		this.port = port;
		this.user = user;
		this.provider = provider;
	}

	public Connection(String name, String host, String port, String user, int provider) {
		this.id = UUID.randomUUID();
		this.name = name;
		this.host = host;
		this.port = port;
		this.user = user;
		this.provider = provider;
	}

	public UUID getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getHost() {
		return host;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getPort() {
		return port;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getUser() {
		return user;
	}

	public int getProvider() {
		return provider;
	}
}
