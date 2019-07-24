package com.test.sparkui;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.ServiceUnavailableException;
import javax.naming.TimeLimitExceededException;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.log4j.Logger;


import com.scb.clairvoyant.util.SPARKUIConstant;

public class LDAPUserAuthentication {
	Logger log = Logger.getLogger(LDAPUserAuthentication.class);

	public String authenticateUser(String username, String password) {
		String strUrl = "success";
		String name = "";
		System.setProperty("javax.net.ssl.trustStore",
				ClairvoyantConstant.LDAP_CERTIFICATION_PATH);
		Hashtable env = new Hashtable(11);
		boolean b = false;
		int count = 0;
		boolean responseOk = false;
		while (count < 5 && !responseOk) {
			String Securityprinciple = "";
			env.put(Context.INITIAL_CONTEXT_FACTORY,
					"com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.PROVIDER_URL, SPARKUIConstant.PROVIDER_URL);
			// env.put(Context.SECURITY_AUTHENTICATION, "simple");
			// env.put(Context.SECURITY_PRINCIPAL, "uid=1535141");
			env.put(Context.SECURITY_PRINCIPAL, "cn=" + username
					+ ",ou=users,o=standardchartered");
			env.put(Context.SECURITY_CREDENTIALS, password);
			env.put("com.sun.jndi.ldap.connect.timeout", "8000");
			try {
				// Create initial context
				LdapContext ctx = new InitialLdapContext(env, null);
				System.out.println("connected !!!!!!");
				// Close the context when we're done
				b = true;
				SearchControls searchControls = getSearchControls();
				name = getUserName(username, ctx, searchControls);
				System.out.println("name is ldap:" + name + "-"
						+ String.valueOf(name.length()));
				if (name.isEmpty()) {
					name = "name:" + username;
				}
				ctx.close();
				responseOk = true;
			} catch (TimeLimitExceededException e) {
				b = false;
				e.printStackTrace();
				System.out.println("LDAP Authentication Exception ---> " + e);
				strUrl = "Login Failed! Due to Timeout: Please Contact System Admin!";
			} catch (ServiceUnavailableException e) {
				b = false;
				e.printStackTrace();
				System.out.println("LDAP Authentication Exception ---> " + e);
				strUrl = "Login Failed! Service Failure: Please Contact System Admin!";
			} catch (CommunicationException e) {
				b = false;
				e.printStackTrace();
				System.out.println("LDAP Authentication Exception ---> " + e);
				strUrl = "Login Failed! Service Hand Shake Failure: Please Contact System Admin!";

			} catch (AuthenticationException e) {
				b = false;
				e.printStackTrace();
				System.out.println("LDAP Authentication Exception ---> " + e);
				responseOk = true;
				strUrl = "Invalid Login!Please supply valid credentials.";
			} catch (NamingException e) {
				b = false;
				e.printStackTrace();
				System.out.println("LDAP Authentication Exception ---> " + e);
				strUrl = "Login Failed! Service error: Please Contact System Admin!";
			} catch (Exception e) {
				b = false;
				e.printStackTrace();
				System.out.println("LDAP Authentication Exception ---> " + e);
				strUrl = "Login Failed! System  issues : Please Contact System Admin!";
			} finally {
				if (b) {
					strUrl = "success:" + name.split(":")[1];

				}
	
				System.out.println(strUrl);
			}
			count++;
		}
		return strUrl;
	}

	private String getUserName(String userID, LdapContext ctx,
			SearchControls searchControls) {
		System.out.println("userID: " + userID);
		String baseDN = "ou=users,o=standardchartered";
		String name = "";
		try {
			NamingEnumeration<SearchResult> answer;
			answer = ctx.search(baseDN, "cn=" + userID, searchControls);

			if (answer.hasMoreElements()) { 
				Attributes attrs = answer.next().getAttributes();
				name = attrs.get("fullName")==null?"":attrs.get("fullName").toString();
			} else {
				System.out.println("user not found.");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// return user;
		return name;
	}

	private static SearchControls getSearchControls() {
		SearchControls cons = new SearchControls();
		cons.setSearchScope(SearchControls.SUBTREE_SCOPE);
		String[] attrIDs = { "fullName" };
		cons.setReturningAttributes(attrIDs);
		return cons;
	}

	
}
