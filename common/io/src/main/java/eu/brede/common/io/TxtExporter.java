package eu.brede.common.io;

public interface TxtExporter {
	public void exportToFile(TxtExportable<? extends Stringifiable> object, String fileName);
}
