package net.kakoen.valheim.save.archive;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.kakoen.valheim.save.archive.hints.ValheimSaveReaderHints;
import net.kakoen.valheim.save.archive.save.DeadZdo;
import net.kakoen.valheim.save.archive.save.Meta;
import net.kakoen.valheim.save.archive.save.RandomEvent;
import net.kakoen.valheim.save.archive.save.Zdo;
import net.kakoen.valheim.save.archive.save.Zones;
import net.kakoen.valheim.save.exception.ValheimArchiveUnsupportedVersionException;
import net.kakoen.valheim.save.parser.ZPackage;

/**
 * Reads the *.db save files
 */
@Data
@NoArgsConstructor
@Slf4j
public class ValheimSaveArchive implements ValheimArchive {

	public static final int MAX_SUPPORTED_WORLD_VERSION = 34;
	
	private Meta meta;
	private long modified;
	
	private long myId;
	private long nextUid;
	
	private Zones zones;
	private RandomEvent randomEvent;
	
	private List<Zdo> zdoList = new ArrayList<>();

	public ValheimSaveArchive(File file, ValheimSaveReaderHints hints) throws IOException, ValheimArchiveUnsupportedVersionException {
		meta = new Meta();
		modified = file.lastModified();
		meta.setModified(file.lastModified());
		try(ZPackage zPackage = new ZPackage(file)) {
			meta.setWorldVersion(zPackage.readInt32());
			if(meta.getWorldVersion() > MAX_SUPPORTED_WORLD_VERSION) {
				if(hints.isFailOnUnsupportedVersion()) {
					throw new ValheimArchiveUnsupportedVersionException(ValheimSaveArchive.class, "world", meta.getWorldVersion(), MAX_SUPPORTED_WORLD_VERSION);
				}
				log.warn("WARNING: world version is {}, the maximum tested world version is {}", meta.getWorldVersion(), MAX_SUPPORTED_WORLD_VERSION);
			} else {
				log.info("World version: {}", meta.getWorldVersion());
			}
			meta.setNetTime(zPackage.readDouble());
			
			loadZdos(zPackage, meta.getWorldVersion(), hints);
			
			zones = new Zones();
			zones.load(zPackage, meta.getWorldVersion());
			
			randomEvent = new RandomEvent(zPackage);
		}
	}
	
	@Override
	public void save(File file) throws IOException {
		try(ZPackage zPackage = new ZPackage()) {
			zPackage.writeInt32(MAX_SUPPORTED_WORLD_VERSION);
			zPackage.writeDouble(meta.getNetTime());
			writeZdos(zPackage);
			zPackage.writeTo(file);
		}
	}
	
	@Override
	public ValheimArchiveType getType() {
		return ValheimArchiveType.DB;
	}
	
	private void writeZdos(ZPackage writer) {
		writer.writeLong(myId);
		writer.writeUInt(nextUid);
		
		writer.writeInt32(zdoList.size());
		zdoList.forEach(zdo -> zdo.save(writer));

		zones.save(writer);
		
		randomEvent.save(writer);
	}
	
	private void loadZdos(ZPackage reader, int version, ValheimSaveReaderHints hints) throws ValheimArchiveUnsupportedVersionException {
		myId = reader.readLong();
		nextUid = reader.readUInt();
		int numberOfZdos = reader.readInt32();
		for(int i = 0; i < numberOfZdos; i++) {
			Zdo zdo = new Zdo(reader, version, hints);
			zdoList.add(zdo);
		}
		log.info("Loaded {} zdos", zdoList.size());
	}
	
}
