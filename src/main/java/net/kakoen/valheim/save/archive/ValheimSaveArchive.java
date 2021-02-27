package net.kakoen.valheim.save.archive;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.kakoen.valheim.save.archive.save.DeadZdo;
import net.kakoen.valheim.save.archive.save.Meta;
import net.kakoen.valheim.save.archive.save.RandomEvent;
import net.kakoen.valheim.save.parser.ZPackage;
import net.kakoen.valheim.save.archive.save.Zdo;
import net.kakoen.valheim.save.struct.ZdoId;
import net.kakoen.valheim.save.archive.save.Zones;

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
			
			randomEvent = loadRandomEvent(zPackage, meta.getWorldVersion());
		}
	}
	
	private void loadZdos(ZPackage reader, int version, ValheimSaveReaderHints hints) {
		reader.readLong();
		long someCount = reader.readUInt();
		int numberOfZdos = reader.readInt32();
		for(int i = 0; i < numberOfZdos; i++) {
			ZdoId zdoId = new ZdoId(reader);
			int count = reader.readInt32();
			Zdo zdo = reader.readFixedSizeObject(count, (zPackage) -> new Zdo(zdoId, zPackage, version, hints));
			zdo.setUid(zdoId);
			zdoList.add(zdo);
		}
		log.info("Loaded {} zdos", zdoList.size());
		
		int deadZdoCount = reader.readInt32();
		for(int i = 0; i < deadZdoCount; i++) {
			ZdoId zdoId = new ZdoId(reader);
			DeadZdo deadZdo = new DeadZdo(zdoId, reader.readLong());
			deadZdos.add(deadZdo);
		}
		log.info("Loaded {} dead zdos", deadZdos.size());
	}
	
	private RandomEvent loadRandomEvent(ZPackage reader, int version) {
		RandomEvent randomEvent = new RandomEvent();
		randomEvent.setEventTimer(reader.readSingle());
		randomEvent.setName(reader.readString());
		randomEvent.setNum(reader.readSingle());
		randomEvent.setPosition(reader.readVector3());
		return randomEvent;
	}
	
}
