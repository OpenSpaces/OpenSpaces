/*
 * Copyright 2011 GigaSpaces Technologies Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at *
 *     
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License."
 */
package org.openspaces.example.alert.logging.snmp;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.ext.SNMPTrapAppender;
import org.apache.log4j.ext.SnmpTrapSenderFacade;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.Snmp;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;

/**
 * An SnmpTrapSenderFacade implementation class. 
 *
 * <p>Activated by the snmpTrapAppender package to create and 
 * transmit a trap message to the SNMP server.
 *
 * <p>The server details, mainly IP and port are configured in
 * log4j.properties.
 *
 * @author giladh
 * @since 8.0
 */
public class SnmpTrapSender implements SnmpTrapSenderFacade {

	private static final String SNMP_XAP_COMMUNITY = "XAP-Events";	
	private static final String SNMP_XAP_ALERT_MSG_OID = "1.2.3.3.25.29.3";

	private static IpAddress localAddr;
	private final Queue<String> trapQueue = new ConcurrentLinkedQueue<String>();
	private String snmpServerIP;
	private int snmpServerPort;

	/**
	 * Called by the snmpTrapAppender to handle a new trap - 
	 * created in response to a new detected alert.
	 * 
	 * Writes the trap data to the trapQueue and immediately 
	 * releases the calling thread
	 */
	public void addTrapMessageVariable(String trapOID, String trapValue) {
		trapQueue.add(trapValue);
	}


	/**
	 * Called once to initialize an instance of the SnmpTrapSender  
	 */
	public void initialize(SNMPTrapAppender arg0) {
		trapQueue.clear();
		loadRunParams();
	}

	/**
	 * Loads running parameters - namely SNMP server's IP and port - 
	 * from the log4j.properties configuration file
	 */
	private void loadRunParams() {
		Properties prop;
		try {
			prop = loadProps("log4j.properties");        	
			snmpServerIP = prop.getProperty("log4j.appender.TRAP_LOG.ManagementHost");
			snmpServerPort = Integer.parseInt(prop.getProperty("log4j.appender.TRAP_LOG.ManagementHostTrapListenPort"));
		} 
		catch (IOException e) {
			throw new RuntimeException("Failed to load AlertLoggingGateway execution params.", e);
		}             	
	}


	private Properties loadProps(String fileName) throws IOException {
		ClassLoader cl = this.getClass().getClassLoader();
		Properties prop = new Properties();
		InputStream resourceAsStream = cl.getResourceAsStream(fileName);
		//URL log4jprops = cl.getResource(fileName);
		if (resourceAsStream != null) {
			//String _path = log4jprops.getFile();
			//prop.load(new FileInputStream(new File(_path)));
			prop.load(resourceAsStream); 
			return prop;
		}
		return null;
	}


	/**
	 * Send next-in-line trap from queue to to SNMP server
	 */
	public void sendTrap() {
		
		String trapVal = trapQueue.poll();
		if (trapVal == null) {
			return;
		}

		try {
			PDUv1 trapPdu = createTrapPDU(trapVal);	    	
			DefaultUdpTransportMapping tm = new DefaultUdpTransportMapping();
			Snmp snmp = new Snmp(tm);
			tm.listen();
			OctetString community = new OctetString(SNMP_XAP_COMMUNITY);
			Address add = GenericAddress.parse("udp" + ":" + snmpServerIP + "/" + snmpServerPort);
			CommunityTarget target = new CommunityTarget(add, community);
			target.setVersion(SnmpConstants.version1);
			target.setRetries(0);
			target.setTimeout(5000);		
			snmp.send(trapPdu, target);  
		} 
		catch (IOException e) {
			e.printStackTrace();
		} 
	}

	private static PDUv1 createTrapPDU(String trapData) throws UnknownHostException{
		PDUv1 trapPdu = (PDUv1)DefaultPDUFactory.createPDU(SnmpConstants.version1);
		trapPdu.setType(PDU.V1TRAP);    	

		VariableBinding vbm = new VariableBinding();
		vbm.setOid(new OID(SNMP_XAP_ALERT_MSG_OID));
		vbm.setVariable(new OctetString(trapData));
		trapPdu.add(vbm);

		trapPdu.setAgentAddress(getLocalAddress());    	    	
		return trapPdu;
	}


	/**
	 * Cache local address 
	 */
	private static IpAddress getLocalAddress() throws UnknownHostException{
		if (localAddr == null) {
			localAddr = new IpAddress(InetAddress.getLocalHost());
		}
		return localAddr;
	}
}
