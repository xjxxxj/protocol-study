package xjx.study.demos.snmp;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;

public class SNMPManager {

    Snmp snmp = null;

    String address = null;

    public SNMPManager(String address) {
        this.address = address;
    }

    /**
     * start the Snmp session. If you forget the listen() method you will not get
     * any answers because the communication is asynchronous
     * and the listen() method listens for answers.
     * @throws IOException
     */
    private void start() throws IOException {
        DefaultUdpTransportMapping transportMapping = new DefaultUdpTransportMapping();
        this.snmp = new Snmp(transportMapping);
        transportMapping.listen();
    }

    private String getAsString(OID oid) throws IOException {
        ResponseEvent event = get(new OID[]{oid});
        return event.getResponse().get(0).getVariable().toString();
    }

    /**
     * this method is capable of handling multiple OIDs
     * @param oids
     * @return
     * @throws IOException
     */
    private ResponseEvent get(OID oids[]) throws IOException {
        PDU pdu = new PDU();
        for (OID oid : oids) {
            pdu.add(new VariableBinding(oid));
        }
        pdu.setType(PDU.GET);
        ResponseEvent event = snmp.send(pdu, getTarget());
        if (event != null){
            return event;
        }
        throw new RuntimeException("GET time out");
    }

    /**
     * this method returns a Target, which contains information about
     * where tha data should be fetch and how.
     * @return
     */
    private Target getTarget() {
        Address targetAddress = GenericAddress.parse(address);
        CommunityTarget target = new CommunityTarget();
        target.setAddress(targetAddress);
        target.setCommunity(new OctetString("test"));
        target.setRetries(2);
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version2c);
        return target;
    }

    public static void main(String[] args) throws IOException {
        //Port 161 is used for Read and Other operation
        // Port 162 is used for the trap generation
        SNMPManager client = new SNMPManager("udp:127.0.0.1/161");
        client.start();
        /**
         * OID - .1.3.6.1.2.1.1.1.0 => SysDec
         * OID - .1.3.6.1.2.1.1.5.0 => SysName
         */
        String sysDescr = client.getAsString(new OID(".1.3.6.1.2.1.1.1.0"));
        String sysName = client.getAsString(new OID(".1.3.6.1.2.1.1.5.0"));
        System.out.println(sysName);
    }
}
