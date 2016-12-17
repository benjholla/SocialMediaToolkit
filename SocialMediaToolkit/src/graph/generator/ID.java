package graph.generator;

import java.util.Collection;
import java.util.LinkedList;

public class ID {
	
	public Collection<String> emails = new LinkedList<String>();
	public Collection<String> usernames = new LinkedList<String>();
	public String id = "";
	public String firstName = "";
	public String lastName = "";
	
	public ID(String id, String firstName, String lastName, Collection<String> emails, Collection<String> usernames){
		this.emails = emails;
		this.usernames = usernames;
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
	}
	
	public String toString(){
		String usernameList = "";
		for(String username : usernames){
			usernameList += username + "\n";
		}
		String emailList = "";
		for(String email : emails){
			emailList += email + "\n";
		}
		return id + "\n" + firstName + " " + lastName + "\n" + usernameList + emailList;
	}
	
}