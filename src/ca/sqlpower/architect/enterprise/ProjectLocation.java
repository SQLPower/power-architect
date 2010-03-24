package ca.sqlpower.architect.enterprise;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import ca.sqlpower.enterprise.client.SPServerInfo;

@Immutable
public class ProjectLocation {

	private final String uuid;
	private final String name;
	private final SPServerInfo serviceInfo;
	
	public ProjectLocation(
			@Nonnull String uuid,
			@Nonnull String name,
			@Nonnull SPServerInfo serviceInfo) {
		
		this.uuid = uuid;
		this.name = name;
		this.serviceInfo = serviceInfo;
	}
	
	public @Nonnull String getName() {
		return name;
	}

	public @Nonnull String getUUID() {
		return uuid;
	}

	public @Nonnull SPServerInfo getServiceInfo() {
		return serviceInfo;
	}
	
	@Override
	public String toString() {
		return getName();
	}
}
