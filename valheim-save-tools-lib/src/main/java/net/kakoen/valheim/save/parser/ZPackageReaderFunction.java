package net.kakoen.valheim.save.parser;

import net.kakoen.valheim.save.exception.ValheimArchiveUnsupportedVersionException;

@FunctionalInterface
public interface ZPackageReaderFunction<T, R> {
	R apply (T t) throws ValheimArchiveUnsupportedVersionException;
}
