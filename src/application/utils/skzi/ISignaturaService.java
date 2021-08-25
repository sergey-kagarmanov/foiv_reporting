package application.utils.skzi;

import java.util.Collection;

import application.models.ErrorFile;
import application.models.FileTransforming;
import application.models.Key;
import javafx.collections.ObservableList;

public interface ISignaturaService {

	int initSignatura(Key key);
	ObservableList<ErrorFile> encrypt(Collection<FileTransforming> files);
	ObservableList<ErrorFile> sign(Collection<FileTransforming> files);
	ObservableList<ErrorFile> decrypt(Collection<FileTransforming> files);
	ObservableList<ErrorFile> unsign(Collection<FileTransforming> files);
	void unload();
}
