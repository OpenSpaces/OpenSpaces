package org.openspaces.example.alert.logging.snmp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Properties;

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



public class SnmpTrapSender implements SnmpTrapSenderFacade {
			
	public void addTrapMessageVariable(String trapOID, String trapValue) {
		trapQueue.add(trapValue);
		loadRunParams();
	}

	public void initialize(SNMPTrapAppender arg0) {
		trapQueue.clear();
	}
	
	private static final String SNMP_XAP_COMMUNITY = "XAP-Events";	
	private static final String SNMP_XAP_ALERT_MSG_OID = "1.2.3.3.25.29.3";
	
	private String snmpServerIP;
	private int snmpServerPort;


    private void loadRunParams() {
        Properties prop;
        try {
        	prop = loadProps("log4j.properties");        	
        	snmpServerIP = prop.getProperty("log4j.appender.TRAP_LOG.ManagementHost");
        	snmpServerPort = Integer.parseInt(prop.getProperty("log4j.appender.TRAP_LOG.ManagementHostTrapListenPort"));
        } 
        catch (IOException e) {
        	throw new RuntimeException("Failed to load SnmpTrapTransmitter execution params. Error: " + e);
        }             	
	}
    
    
    private Properties loadProps(String fileName) throws IOException {
    	ClassLoader cl = this.getClass().getClassLoader();
    	Properties prop = new Properties();
    	URL log4jprops = cl.getResource(fileName);
    	if (log4jprops != null) {
    		String _path = log4jprops.getFile();
    		prop.load(new FileInputStream(new File(_path)));
    		return prop;
    	}
    	return null;
	}

	
	public void sendTrap() {
		String trapVal = trapQueue.removeFirst();
		
		String XapCommunity = SNMP_XAP_COMMUNITY;
		
		try {
			// send a XAP trap to SNMP manager
	    	System.out.println("About to send trap data: " + trapVal);
	    	    	    
	    	PDUv1 trapPdu = createTrapPDU(trapVal);
	    	
			DefaultUdpTransportMapping tm = new DefaultUdpTransportMapping();
			Snmp snmp = new Snmp(tm);
			tm.listen();
			OctetString community = new OctetString(XapCommunity);
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
	
	private static PDUv1 createTrapPDU(String trapData) {
    	PDUv1 trapPdu = (PDUv1)DefaultPDUFactory.createPDU(SnmpConstants.version1);
    	trapPdu.setType(PDU.V1TRAP);    	

    	VariableBinding vbm = new VariableBinding();
    	String _oid = SNMP_XAP_ALERT_MSG_OID;
    	vbm.setOid(new OID(_oid));
    	vbm.setVariable(new OctetString(trapData));
    	trapPdu.add(vbm);
    	     	
    	trapPdu.setAgentAddress(getLocalAddress());    	    	
    	return trapPdu;
	}
	
		
	private static IpAddress getLocalAddress() {
		if (localAddr == null) {
	    	try {
	    		localAddr = new IpAddress(InetAddress.getLocalHost());
			} 
	    	catch (UnknownHostException e) {
				e.printStackTrace();
				throw new RuntimeException("Failed on: " + e.getMessage());
			}
		}
		return localAddr;
	}
	
	private static IpAddress localAddr;

	private LinkedList<String> trapQueue = new LinkedList<String>();
		
}
