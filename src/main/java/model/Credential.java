package model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Comparator;

import javafx.beans.property.SimpleStringProperty;

public class Credential implements Serializable, Comparator<Credential> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private transient SimpleStringProperty site;
	private transient SimpleStringProperty username;
	private transient SimpleStringProperty password;
	private transient SimpleStringProperty category;

	public Credential (String site, String username, String password, String category) {
		this.site = new SimpleStringProperty(site);
		this.username = new SimpleStringProperty(username);
		this.password = new SimpleStringProperty(password);
		this.category = new SimpleStringProperty(category);
	}
	
	public Credential() {
		this.site = null;
		this.username = null;
		this.password = null;
		this.category = null;
	}

	public String getSite() {
		return this.site.get();
	}

	public String getUsername() {
		return this.username.get();
	}

	public String getPassword() {
		return this.password.get();
	}

	public String getCategory() {
		return this.category.get();
	}

	public void setSite (String site) {
		this.site = new SimpleStringProperty(site);
	}

	public void setUsername (String username) {
		this.username = new SimpleStringProperty(username);
	}

	public void setPassword (String password) {
		this.password = new SimpleStringProperty(password);
	}

	public void setCategory(String category) {
		this.category = new SimpleStringProperty(category);
	}

	public void print(PrintStream ps) {;
		ps.print(toString());
	}

	public String toString() {
		String s = getSite() + ";" + getUsername() + ";" + getPassword() + ";" + getCategory() + ";\r\n";
		return s;
	}

	@Override
	public int compare(Credential o1, Credential o2) {
		return o1.getSite().compareTo(o2.getSite());
	}

	// Makes the object serializable
	private void writeObject(ObjectOutputStream s) throws IOException {
		s.defaultWriteObject();
		s.writeUTF(this.site.getValueSafe()); // can't be null so use getValueSafe that returns empty string if it's null
		s.writeUTF(this.username.getValueSafe());
		s.writeUTF(this.password.getValueSafe());
		s.writeUTF(this.category.getValueSafe());
	}

	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
		setSite(s.readUTF());
		setUsername(s.readUTF());
		setPassword(s.readUTF());
		setCategory(s.readUTF());
	}

}
