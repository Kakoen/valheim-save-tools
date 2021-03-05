package net.kakoen.valheim.save.archive;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.kakoen.valheim.save.archive.save.DeadZdo;
import net.kakoen.valheim.save.archive.save.Meta;
import net.kakoen.valheim.save.archive.save.RandomEvent;
import net.kakoen.valheim.save.archive.save.Zdo;
import net.kakoen.valheim.save.archive.save.Zones;
import net.kakoen.valheim.save.parser.ZPackage;

/**
 * Reads the *.db save files
 */
@Data
@NoArgsConstructor
@Slf4j
public class ValheimSaveArchive {

	private final static int MAX_TESTED_WORLD_VERSION = 26;
	
	private Meta meta;
	private long modified;
	
	private long myId;
	private long nextUid;
	
	private Zones zones;
	private RandomEvent randomEvent;
	
	private List<Zdo> zdoList = new ArrayList<>();
	private List<DeadZdo> deadZdos = new ArrayList<>();
	
	public ValheimSaveArchive(File file, ValheimSaveReaderHints hints) throws IOException {
		meta = new Meta();
		modified = file.lastModified();
		meta.setModified(file.lastModified());
		try(ZPackage zPackage = new ZPackage(file)) {
			meta.setWorldVersion(zPackage.readInt32());
			if(meta.getWorldVersion() > MAX_TESTED_WORLD_VERSION) {
				log.warn("WARNING: world version is {}, the maximum tested world version is {}", meta.getWorldVersion(), MAX_TESTED_WORLD_VERSION);
			}
			meta.setNetTime(zPackage.readDouble());
			
			loadZdos(zPackage, meta.getWorldVersion(), hints);
			
			zones = new Zones();
			zones.load(zPackage, meta.getWorldVersion());
			
			randomEvent = new RandomEvent(zPackage);
		}
	}
	
	public void save(File file) throws IOException {
		try(ZPackage zPackage = new ZPackage()) {
			zPackage.writeInt32(meta.getWorldVersion());
			zPackage.writeDouble(meta.getNetTime());
			writeZdos(zPackage);
			zPackage.writeTo(file);
		}
	}
	
	private void writeZdos(ZPackage writer) {
		writer.writeLong(myId);
		writer.writeUInt(nextUid);
		
		writer.writeInt32(zdoList.size());
		zdoList.forEach(zdo -> zdo.save(writer));
		
		writer.writeInt32(deadZdos.size());
		deadZdos.forEach(deadZdo -> deadZdo.save(writer));
		
		zones.save(writer);
		
		randomEvent.save(writer);
	}
	
	private void loadZdos(ZPackage reader, int version, ValheimSaveReaderHints hints) {
		myId = reader.readLong();
		nextUid = reader.readUInt();
		int numberOfZdos = reader.readInt32();
		for(int i = 0; i < numberOfZdos; i++) {
			zdoList.add(new Zdo(reader, version, hints));
		}
		log.info("Loaded {} zdos", zdoList.size());
		
		int deadZdoCount = reader.readInt32();
		for(int i = 0; i < deadZdoCount; i++) {
			deadZdos.add(new DeadZdo(reader));
		}
		log.info("Loaded {} dead zdos", deadZdos.size());
	}
	
}
