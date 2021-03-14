package net.kakoen.valheim.save.exception;

public class ValheimArchiveUnsupportedVersionException extends Throwable {
	public ValheimArchiveUnsupportedVersionException(Class<?> archiveClass, String what, int version, int supportedVersion) {
		super(archiveClass.getSimpleName() + " encountered a " + what + " version " + version + " that is higher than the last tested version " + supportedVersion);
	}
}
